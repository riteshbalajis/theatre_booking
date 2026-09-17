package com.movie_booking.config;

import org.glassfish.jersey.server.ResourceConfig;

public class JerseyApplication extends ResourceConfig {

    public JerseyApplication() {
        packages(
            "com.movie_booking.resource",
            "com.movie_booking.exception",
            "com.movie_booking.config"
        );
        register(ObjectMapperContextResolver.class);
    }
}