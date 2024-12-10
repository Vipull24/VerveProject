package org.verve.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.verve.ConfigLoader;
import org.verve.logger.LogToFile;
import redis.clients.jedis.Jedis;
import org.verve.redis.RedisClient;
import redis.clients.jedis.Transaction;

@Path("/api/verve/accept")
public class VerveRESTService {

    private static final Logger logger = Logger.getLogger(VerveRESTService.class.getName());

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            Integer.parseInt(ConfigLoader.getInstance().getProperty("threadpool.size.min", "100")), // Core pool size
            Integer.parseInt(ConfigLoader.getInstance().getProperty("threadpool.size.max", "10000")), // Maximum pool size
            60L, TimeUnit.SECONDS, // Keep-alive time for idle threads
            new SynchronousQueue<>());

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .executor(executorService)
            .build();

    private final Counter counter = Counter.getInstance();

    @GET
    @Produces("text/plain")
    public String handleRequest(@QueryParam("endpoint") String endpoint, @QueryParam("id") String id) {
        // Submit a task to the executor service
        Future<Response> futureResponse = executorService.submit(() -> {
            // Store the ID in Redis with expiry
            int unique;
            try {
                unique = storeIdWithExpiry(id);
            } catch (Exception e) {
                return Response.ok("failed").build();
            }

            if (endpoint == null || endpoint.isEmpty()) {
                return Response.ok("ok").build();
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(unique)))
                    .build();

            CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            responseFuture.thenApply(response -> {
                        int statusCode = response.statusCode();
                        LogToFile.log(null, "info", "Response received from " + endpoint +  " : " + statusCode);
                        return response;
                    })
                    .exceptionally(ex -> null);
            // Return a preliminary response if needed
            return Response.ok("ok").build();
        });

        try {
            // Wait for the task to complete and get the result
            if(futureResponse.get().getEntity() != null) {
                return futureResponse.get().getEntity().toString();
            }
            return "failed";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing request: {0}", e.getMessage());
            return "failed";
        }
    }

    // Method to shut down the executor service
    public static void shutdownExecutor() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Executor did not terminate in the specified time.");
                executorService.shutdownNow();
            }
            System.out.println("Executor terminated.");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private int storeIdWithExpiry(String id) {
        try (Jedis jedis = RedisClient.getJedis()) {
            if (!jedis.exists(id)) {
                long expiry = getCurrExpiryInUnix();

                Transaction transaction = jedis.multi();
                transaction.setnx(id, "1");
                transaction.expireAt(id, expiry);

                List<Object> results = transaction.exec();
                if(results != null && !results.isEmpty() && results.get(0).equals(1L)) {
                    return counter.increment();
                }
            } else {
                return counter.getCurrCount();
            }
        }
        return counter.getCurrCount();
    }

    private long getCurrExpiryInUnix() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMinuteCeiling = now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        ZoneId zoneId = ZoneId.of("Asia/Kolkata"); // Specify the desired time zone
        ZonedDateTime zonedDateTime = nextMinuteCeiling.atZone(zoneId);
        return nextMinuteCeiling.toEpochSecond(zonedDateTime.getOffset());
    }

}
