package com.movie_booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingCreatedResponse {
    private int bookingId;
    private String message;
    private LocalDateTime holdUntil;
    private BigDecimal amount;

    public BookingCreatedResponse(int bookingId, String message, BigDecimal amount, LocalDateTime holdUntil) {
        this.bookingId = bookingId;
        this.message = message;
        this.amount = amount;
        this.holdUntil = holdUntil;
    }

    public int getbookingId() {
        return bookingId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}