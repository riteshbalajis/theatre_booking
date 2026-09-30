package com.movie_booking.scheduler;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.movie_booking.service.EmailService;
import com.movie_booking.service.ReportService;
import com.movie_booking.service.ReportServiceImpl;
import com.movie_booking.service.UserService;
import com.movie_booking.service.UserServiceImpl;

public class ReportScheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final ReportService reportService =
            new ReportServiceImpl();

    private final UserService userService =
            new UserServiceImpl();

    private final EmailService emailService =
            new EmailService();

    public void start() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nextRun = LocalDateTime.of(
                now.toLocalDate().plusDays(1),
                LocalTime.of(0, 5)
        );

        /*LocalDateTime nextRun = LocalDateTime.of(
        LocalDate.now(),
        LocalTime.of(17, 21)
);*/

        long initialDelay =
                Duration.between(now, nextRun).getSeconds();

        long oneDay = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(
                this::generateAndSendReport,
                initialDelay,
                oneDay,
                TimeUnit.SECONDS
        );

        System.out.println("Report scheduler started.");
        System.out.println("Next report run: " + nextRun);
    }

    private void generateAndSendReport() {

        LocalDate reportDate = LocalDate.now().minusDays(1);

        String filePath =
                "D:/movie_booking/daily-report-" + reportDate + ".pdf";

        File pdfFile = new File(filePath);

        try {

            System.out.println("Generating report for: " + reportDate);

            // Generate ONE consolidated PDF
            reportService.generateDailyReportPdf(
                    reportDate,
                    filePath
            );

            // Get all active admin emails
            List<String> adminEmails =
                    userService.findAdminEmails();

            if (adminEmails.isEmpty()) {
                System.out.println("No active admins found.");
                return;
            }

            // Send the same PDF to every admin
            for (String email : adminEmails) {

                emailService.sendDailyReportEmail(
                        email,
                        reportDate,
                        filePath
                );
            }

            System.out.println("Daily report sent successfully.");

        } catch (Exception e) {

            System.err.println("Failed to generate/send daily report.");
            e.printStackTrace();

        } finally {

            // Delete temporary PDF
            if (pdfFile.exists()) {

                boolean deleted = pdfFile.delete();

                System.out.println(
                        "Temporary PDF deleted: " + deleted
                );
            }
        }
    }

    public void stop() {

        scheduler.shutdown();

        System.out.println("Report scheduler stopped.");
    }
}