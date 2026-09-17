package com.movie_booking.dto.request;

public class RegistrationVerifyRequest {
    private long registrationId;
    private String otp;

    public RegistrationVerifyRequest() {
    }

    public RegistrationVerifyRequest(long registrationId, String otp) {
        this.registrationId = registrationId;
        this.otp = otp;
    }

    public long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(long registrationId) {
        this.registrationId = registrationId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
