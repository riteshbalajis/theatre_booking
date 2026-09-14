package com.movie_booking.dto.response;

public class CreateResponse {
    private int id;
    private String message;

    public CreateResponse(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
    
}
