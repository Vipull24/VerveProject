package org.verve;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class VerveApplication {

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8090/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startApplication() {
        // Create a resource config that scans for JAX-RS resources and providers in the package
        final ResourceConfig rc = new ResourceConfig().packages("org.verve.service");

        // Create and start a new instance of the Grizzly HTTP server
        // Exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        // Start the server
        final HttpServer server = startApplication();
        System.out.println(String.format("Jersey app started at %s", BASE_URI));
        UniqueRequestLogger.run();
        System.out.println("Hit enter to stop it...");
        System.in.read();
        UniqueRequestLogger.contextDestroyed();
        server.shutdownNow();
    }
}
