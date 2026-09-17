package com.movie_booking.dto.response;

public class RazorpayVerificationResponse {

    private int bookingId;
    private String message;

    public RazorpayVerificationResponse(int bookingId, String message) {
        this.bookingId = bookingId;
        this.message = message;
    }

    public int getBookingId() {
        return bookingId;
    }

    public String getMessage() {
        return message;
    }
}