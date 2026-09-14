package com.movie_booking.service;

public class AuthenticationRequiredException extends IllegalArgumentException {
    public AuthenticationRequiredException() {
        super("Authentication is required.");
    }
}
