package com.movie_booking.service;

public class BookingAccessException extends IllegalArgumentException {
    public BookingAccessException(String message) {
        super(message);
    }
}
