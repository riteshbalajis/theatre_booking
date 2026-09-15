package com.movie_booking.dto.response;

public class LoginResponse {

    private UserResponse user;
    private String message;
    private boolean totpRequired;

    public LoginResponse() {
    }

    public LoginResponse(
            UserResponse user,
            String message
    ) {
        this.user = user;
        this.message = message;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isTotpRequired() {
        return totpRequired;
    }

    public void setTotpRequired(boolean totpRequired) {
        this.totpRequired = totpRequired;
    }
}