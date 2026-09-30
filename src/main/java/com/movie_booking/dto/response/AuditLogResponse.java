package com.movie_booking.dto.response;

import java.time.LocalDateTime;

public class AuditLogResponse {
    private Long logId;
    private LocalDateTime timestamp;
    private String eventType;
    private Integer userId;
    private String entityType;
    private Integer entityId;
    private String details;
    private String ipAddress;

    // Constructors, getters, setters
    public AuditLogResponse() {}

    public AuditLogResponse(Long logId, LocalDateTime timestamp, String eventType,
                            Integer userId, String entityType, Integer entityId,
                            String details, String ipAddress) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
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

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    
}