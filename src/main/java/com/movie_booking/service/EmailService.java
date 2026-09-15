package com.movie_booking.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

    public void sendTestEmail(String recipient) {

        String subject = "Movie Booking - Test Email";

        String body
                = "Hello!\n\n"
                + "This is a test email from the Movie Booking application.\n\n"
                + "If you received this email, SMTP is working correctly.";

        sendEmail(recipient, subject, body);
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
}
