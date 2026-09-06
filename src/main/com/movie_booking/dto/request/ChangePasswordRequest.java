package com.movie_booking.dto.request;

public class ChangePasswordRequest {
    private int userId;
    private String currentPassword;
    private String newPassword;

    public ChangePasswordRequest() { }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
