package com.movie_booking.dto.response;

import com.movie_booking.model.TicketStatus;

public class TicketCheckInResponse {
    private boolean valid;
    private String code;
    private String message;
    private Integer bookingId;
    private String ticketCode;
    private TicketStatus ticketStatus;

    public TicketCheckInResponse() {
    }

    public TicketCheckInResponse(
            boolean valid,
            String code,
            String message,
            Integer bookingId,
            String ticketCode,
            TicketStatus ticketStatus) {
        this.valid = valid;
        this.code = code;
        this.message = message;
        this.bookingId = bookingId;
        this.ticketCode = ticketCode;
        this.ticketStatus = ticketStatus;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }
}
