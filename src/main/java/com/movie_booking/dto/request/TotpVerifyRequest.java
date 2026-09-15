package com.movie_booking.dto.request;

public class TotpVerifyRequest {

    private String code;

    public TotpVerifyRequest() {
    }

    public TotpVerifyRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}