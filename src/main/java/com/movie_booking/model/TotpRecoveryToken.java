package com.movie_booking.model;

import java.time.LocalDateTime;

public class TotpRecoveryToken {
    private long recoveryId;
    private int userId;
    private TotpRecoveryMethod recoveryMethod;
    private String otpHash;
    private LocalDateTime expiresAt;
    private boolean used;
    private int attemptCount;
    private LocalDateTime createdAt;

    public TotpRecoveryToken() {
    }

    public long getRecoveryId() {
        return recoveryId;
    }

    public void setRecoveryId(long recoveryId) {
        this.recoveryId = recoveryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public TotpRecoveryMethod getRecoveryMethod() {
        return recoveryMethod;
    }

    public void setRecoveryMethod(TotpRecoveryMethod recoveryMethod) {
        this.recoveryMethod = recoveryMethod;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
