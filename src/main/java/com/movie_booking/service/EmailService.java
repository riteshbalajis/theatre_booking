package com.movie_booking.service;

import java.io.File;
import java.time.LocalDate;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.movie_booking.config.MailConfig;

public class EmailService {

    private final String username;
    private final String password;

    public EmailService() {
        this.username = MailConfig.getUsername();
        this.password = MailConfig.getPassword();
    }

    public void sendPasswordResetOtp(String recipient, String otp) {

        String subject = "Movie Booking - Password Reset OTP";

        String body
                = "Hello,\n\n"
                + "We received a request to reset your Movie Booking account password.\n\n"
                + "Your password reset OTP is: " + otp + "\n\n"
                + "This OTP will expire in 5 minutes.\n\n"
                + "If you did not request a password reset, please ignore this email.\n\n"
                + "Regards,\n"
                + "Movie Booking Team";

        sendEmail(recipient, subject, body);
    }

    public void sendTotpRecoveryOtp(String recipient, String otp) {

        String subject = "Screenly - Authenticator Recovery Code";

        String body
                = "Hello,\n\n"
                + "We received a request to recover access to your Screenly "
                + "authenticator.\n\n"
                + "Your authenticator recovery code is: " + otp + "\n\n"
                + "This code will expire in 5 minutes and can be used only once.\n\n"
                + "If you did not request authenticator recovery, please secure "
                + "your account and contact support.\n\n"
                + "Regards,\n"
                + "Screenly Team";

        sendEmail(recipient, subject, body);
    }

    public void sendRegistrationVerificationOtp(
            String recipient,
            String name,
            String otp) {

        String subject = "Screenly - Verify Your Email";

        String body
                = "Hello " + name + ",\n\n"
                + "Thank you for registering with Screenly.\n\n"
                + "Your email verification code is: " + otp + "\n\n"
                + "This code will expire in 5 minutes and can be used only once.\n\n"
                + "If you did not create this registration request, please ignore "
                + "this email.\n\n"
                + "Regards,\n"
                + "Screenly Team";

        sendEmail(recipient, subject, body);
    }

    public void sendWelcomeEmail(String recipient, String name) {

        String subject = "Welcome to Screenly - Account Created";

        String body
                = "Hello " + name + ",\n\n"
                + "Welcome to Screenly!\n\n"
                + "Your account has been successfully created.\n\n"
                + "You can now log in and book movie tickets.\n\n"
                + "Thank you for joining Screenly.\n\n"
                + "Regards,\n"
                + "Screenly Team";

        sendEmail(recipient, subject, body);
    }

    public void sendTicketConfirmationEmail(
            String recipient,
            String userName,
            int bookingId,
            String movieName,
            String theatreName,
            String screenName,
            String showDate,
            String showTime,
            String seats,
            int seatCount,
            double totalAmount) {

        String subject = "Screenly - Booking Confirmed | Booking #" + bookingId;

        String body
                = "Hello " + userName + ",\n\n"
                + "Your movie ticket booking has been confirmed successfully!\n\n"
                + "----------------------------------------\n"
                + "             BOOKING DETAILS\n"
                + "----------------------------------------\n\n"
                + "Booking ID  : " + bookingId + "\n"
                + "Movie       : " + movieName + "\n"
                + "Theatre     : " + theatreName + "\n"
                + "Screen      : " + screenName + "\n"
                + "Date        : " + showDate + "\n"
                + "Show Time   : " + showTime + "\n"
                + "Seats       : " + seats + "\n"
                + "Seat Count  : " + seatCount + "\n"
                + "Total Amount: ₹" + String.format("%.2f", totalAmount) + "\n\n"
                + "----------------------------------------\n\n"
                + "Please arrive at the theatre a few minutes before "
                + "the show starts.\n\n"
                + "You can view your booking and ticket details "
                + "from the My Bookings section in Screenly.\n\n"
                + "Thank you for booking with Screenly!\n\n"
                + "Enjoy the movie! 🍿\n\n"
                + "Regards,\n"
                + "Screenly Team";

        sendEmail(recipient, subject, body);
    }

    public void sendDailyReportEmail(
            String recipient,
            LocalDate reportDate,
            String pdfFilePath) {

        String subject
                = "Screenly - Daily Booking Report | "
                + reportDate;

        String body
                = "Hello,\n\n"
                + "Please find attached the daily movie booking report "
                + "for " + reportDate + ".\n\n"
                + "The report contains:\n"
                + "- Overall booking summary\n"
                + "- Movie-wise report\n"
                + "- Theatre-wise report\n"
                + "- Movie-theatre-wise report\n\n"
                + "Regards,\n"
                + "Screenly Team";

        sendEmailWithAttachment(
                recipient,
                subject,
                body,
                pdfFilePath
        );
    }

    private void sendEmail(
            String recipient,
            String subject,
            String body) {

        Properties properties = new Properties();

        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(
                properties,
                new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(
                        username,
                        password
                );
            }
        }
        );

        System.out.println(">>> MAIL USERNAME = " + username);
        System.out.println(">>> RECIPIENT = " + recipient);

        try {

            Message message = new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(username)
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient)
            );

            message.setSubject(subject);

            message.setText(body);

            Transport.send(message);

            System.out.println(
                    ">>> EMAIL SENT SUCCESSFULLY"
            );

        } catch (MessagingException e) {

            System.out.println(
                    ">>> EMAIL SENDING FAILED"
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to send email.",
                    e
            );
        }
    }

    private void sendEmailWithAttachment(
            String recipient,
            String subject,
            String body,
            String filePath) {


        System.out.println(">>> MAIL USERNAME = " + username);
        System.out.println(">>> RECIPIENT = " + recipient);
        

        Properties properties = new Properties();

        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(
                properties,
                new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(
                        username,
                        password
                );
            }
        });

        try {

            Message message = new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(username)
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient)
            );

            message.setSubject(subject);

            // ==========================================
            // BODY + ATTACHMENT
            // ==========================================
            MimeBodyPart textPart
                    = new MimeBodyPart();

            textPart.setText(body);

            // ==========================================
            // PDF ATTACHMENT
            // ==========================================
            MimeBodyPart attachmentPart
                    = new MimeBodyPart();

            attachmentPart.attachFile(
                    new File(filePath)
            );

            // ==========================================
            // COMBINE BODY + PDF
            // ==========================================
            Multipart multipart
                    = new MimeMultipart();

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // ==========================================
            // SEND
            // ==========================================
            Transport.send(message);

            System.out.println(
                    ">>> DAILY REPORT EMAIL SENT SUCCESSFULLY"
            );

        } catch (Exception e) {

            System.out.println(
                    ">>> DAILY REPORT EMAIL FAILED"
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to send daily report email.",
                    e
            );
        }
    }
}
