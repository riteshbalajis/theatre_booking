package com.movie_booking.config;

public class MailConfig {

    public static String getUsername() {
        return AppConfig.getRequired("MAIL_USERNAME");
    }

    public static String getPassword() {
        return AppConfig.getRequired("MAIL_PASSWORD");
    }
}
