package com.movie_booking.model;

import java.time.LocalDateTime;

public class SecurityLog {
    private Long logId;
    private LocalDateTime timestamp;
    private String eventType;
    private Integer userId;
    private String email;
    private String ipAddress;
    private String sessionId;
    private String details;

    // Constructors, getters, setters
    public SecurityLog() {}

    public SecurityLog(String eventType, Integer userId, String email, 
                       String ipAddress, String sessionId, String details) {
        this.eventType = eventType;
        this.userId = userId;
        this.email = email;
        this.ipAddress = ipAddress;
        this.sessionId = sessionId;
        this.details = details;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Getters and setters...
}