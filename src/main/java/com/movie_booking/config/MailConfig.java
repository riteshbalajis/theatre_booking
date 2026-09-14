package com.movie_booking.config;

import io.github.cdimascio.dotenv.Dotenv;

public class MailConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("D:/movie_booking")
            .load();

    public static String getUsername() {
        return dotenv.get("MAIL_USERNAME");
    }

    public static String getPassword() {
        return dotenv.get("MAIL_PASSWORD");
    }
}