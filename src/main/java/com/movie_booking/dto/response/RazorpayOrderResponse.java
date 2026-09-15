package com.movie_booking.dto.response;

import java.math.BigDecimal;

public class RazorpayOrderResponse {

    private int bookingId;
    private String orderId;
    private String keyId;
    private BigDecimal amount;
    private String currency;

    public RazorpayOrderResponse() {
    }

    public RazorpayOrderResponse(
            int bookingId,
            String orderId,
            String keyId,
            BigDecimal amount,
            String currency) {

        this.bookingId = bookingId;
        this.orderId = orderId;
        this.keyId = keyId;
        this.amount = amount;
        this.currency = currency;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}