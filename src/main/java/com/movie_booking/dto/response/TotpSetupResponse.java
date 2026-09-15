package com.movie_booking.dto.response;

public class TotpSetupResponse {

    private String secret;
    private String otpauthUri;

    public TotpSetupResponse() {
    }

    public TotpSetupResponse(
            String secret,
            String otpauthUri
    ) {
        this.secret = secret;
        this.otpauthUri = otpauthUri;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getOtpauthUri() {
        return otpauthUri;
    }

    public void setOtpauthUri(String otpauthUri) {
        this.otpauthUri = otpauthUri;
    }
}