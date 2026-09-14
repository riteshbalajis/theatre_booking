package com.movie_booking.exception;

public class MovieNotFoundException
        extends ResourceNotFoundException {

    public MovieNotFoundException(String message) {
        super(message);
    }
}