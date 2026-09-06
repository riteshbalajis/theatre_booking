package com.movie_booking.dto.response;

import java.math.BigDecimal;

public class BookingSeatResponse {
    private int bookingSeatId;
    private int bookingId;
    private int showSeatId;
    private String rowLabel;
    private int seatNumber;
    private BigDecimal price;

    public BookingSeatResponse() { }

    public int getBookingSeatId() { return bookingSeatId; }
    public void setBookingSeatId(int bookingSeatId) { this.bookingSeatId = bookingSeatId; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getShowSeatId() { return showSeatId; }
    public void setShowSeatId(int showSeatId) { this.showSeatId = showSeatId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
