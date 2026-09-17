package com.movie_booking.dto.request;

public class TicketCheckInRequest {
    private String ticketCode;

    public TicketCheckInRequest() {
    }

    public TicketCheckInRequest(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }
}
