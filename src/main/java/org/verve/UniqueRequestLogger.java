package org.verve;

import org.verve.logger.LogToFile;
import org.verve.service.Counter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UniqueRequestLogger {

    private static ScheduledExecutorService scheduler;

    public static void run() {
        LogToFile.log(null, "info", "SchedulerContextListener initialized");
        scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            Counter counter = Counter.getInstance();
            int count = counter.getCurrCountAndReset();
            LogToFile.log(null, "info", "Count: " + count);
        };

        long initialDelay = computeInitialDelay();
        long period = 60L; // Run every 60 seconds

        // Schedule the task to run at the end of each minute
        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
    }

    public static void contextDestroyed() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private static long computeInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMinute = now.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
        return now.until(nextMinute, ChronoUnit.SECONDS);
    }
}