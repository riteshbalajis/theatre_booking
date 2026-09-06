package com.movie_booking.service;

public class AuthorizationException extends IllegalArgumentException {
    public AuthorizationException() {
        super("Administrator access is required.");
    }
}
