package com.movie_booking.dto.response;

public class TheatreCreateResponse {

    private int theatreId;
    private String message;

    public TheatreCreateResponse(int theatreId, String message) {
        this.theatreId = theatreId;
        this.message = message;
    }

    public int getTheatreId() {
        return theatreId;
    }

    public String getMessage() {
        return message;
    }
}