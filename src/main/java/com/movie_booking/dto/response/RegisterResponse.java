package com.movie_booking.dto.response;

public class RegisterResponse {
    private int userId;
    private String message;

    public RegisterResponse() { }

    public RegisterResponse(int userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { 
        this.userId = userId; 
    }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
}
