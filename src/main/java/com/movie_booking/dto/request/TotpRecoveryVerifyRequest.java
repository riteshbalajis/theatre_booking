package com.movie_booking.dto.request;

public class TotpRecoveryVerifyRequest {
    private String otp;

    public TotpRecoveryVerifyRequest() {
    }

    public TotpRecoveryVerifyRequest(String otp) {
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
