package com.movie_booking.dto.response;

public class TotpRecoveryResponse {
    private String message;
    private boolean recoveryVerified;

    public TotpRecoveryResponse() {
    }

    public TotpRecoveryResponse(String message, boolean recoveryVerified) {
        this.message = message;
        this.recoveryVerified = recoveryVerified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRecoveryVerified() {
        return recoveryVerified;
    }

    public void setRecoveryVerified(boolean recoveryVerified) {
        this.recoveryVerified = recoveryVerified;
    }
}
