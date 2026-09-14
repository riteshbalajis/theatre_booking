package com.movie_booking.model;

import java.time.LocalDateTime;

public class PasswordResetToken {

    private int resetId;
    private int userId;

    private String otpHash;
    private LocalDateTime expiresAt;

    private boolean used;
    private LocalDateTime createdAt;

    private String resetTokenHash;
    private LocalDateTime resetTokenExpiresAt;
    private boolean resetVerified;

    public PasswordResetToken() {
    }

    public int getResetId() {
        return resetId;
    }

    public void setResetId(int resetId) {
        this.resetId = resetId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getResetTokenHash() {
        return resetTokenHash;
    }

    public void setResetTokenHash(String resetTokenHash) {
        this.resetTokenHash = resetTokenHash;
    }

    public LocalDateTime getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public boolean isResetVerified() {
        return resetVerified;
    }

    public void setResetVerified(boolean resetVerified) {
        this.resetVerified = resetVerified;
    }

    
    
}