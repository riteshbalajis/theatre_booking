package com.movie_booking.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.movie_booking.service.BookingService;
import com.movie_booking.service.BookingServiceImpl;

@WebListener
public class BookingCleanupListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private BookingService bookingService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        bookingService = new BookingServiceImpl();
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Runs automatically every 15 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                bookingService.cleanupExpiredBookingsAndHolds();
            } catch (Exception e) {
                sce.getServletContext().log("Error during booking cleanup task", e);
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
