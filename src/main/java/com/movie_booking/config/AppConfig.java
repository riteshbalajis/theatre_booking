package com.movie_booking.config;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    /**
     * Gets a configuration value from the process environment, JVM properties,
     * then the local root .env file.
     */
    public static String get(String key) {
        String val = System.getenv(key);
        if (val != null && !val.isBlank()) {
            return val;
        }

        val = System.getProperty(key);
        if (val != null && !val.isBlank()) {
            return val;
        }

        val = DOTENV.get(key);
        return val == null || val.isBlank() ? null : val;
    }

    /**
     * Gets a required configuration value, throwing an explicit error if missing.
     */
    public static String getRequired(String key) {
        String val = get(key);
        if (val == null || val.isBlank()) {
                throw new IllegalStateException(
                    "Missing required configuration value '" + key
                        + "'. Set it as an environment variable or in the local root .env file.");
        }
        return val;
    }

    /**
     * Gets the poster storage path from configuration.
     */
    public static String getPosterStoragePath() {
        return getRequired("POSTER_STORAGE_PATH");
    }
}
