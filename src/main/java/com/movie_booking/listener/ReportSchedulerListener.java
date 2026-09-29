package com.movie_booking.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.movie_booking.scheduler.ReportScheduler;

@WebListener
public class ReportSchedulerListener implements ServletContextListener {

    private ReportScheduler reportScheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        reportScheduler = new ReportScheduler();
        reportScheduler.start();

        System.out.println("Report scheduler initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

        if (reportScheduler != null) {
            reportScheduler.stop();
        }

        System.out.println("Report scheduler stopped.");
    }
}