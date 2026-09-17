package com.movie_booking.dto.response;

public class RegistrationResponse {
    private long registrationId;
    private Integer userId;
    private String message;
    private boolean verificationRequired;
    private boolean registered;

    public RegistrationResponse() {
    }

    public RegistrationResponse(
            long registrationId,
            Integer userId,
            String message,
            boolean verificationRequired,
            boolean registered) {
        this.registrationId = registrationId;
        this.userId = userId;
        this.message = message;
        this.verificationRequired = verificationRequired;
        this.registered = registered;
    }

    public long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(long registrationId) {
        this.registrationId = registrationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
