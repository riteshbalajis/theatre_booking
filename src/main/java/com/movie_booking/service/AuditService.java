package com.movie_booking.service;

import com.movie_booking.dto.response.AuditLogResponse;
import com.movie_booking.dto.response.SecurityLogResponse;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.movie_booking.dao.AuditLogDao;
import com.movie_booking.dao.AuditLogDaoImpl;
import com.movie_booking.dao.SecurityLogDao;
import com.movie_booking.dao.SecurityLogDaoImpl;
import com.movie_booking.model.AuditLog;
import com.movie_booking.model.SecurityLog;

public class AuditService {

    private final SecurityLogDao securityLogDao;
    private final AuditLogDao auditLogDao;

    public AuditService() {
        this.securityLogDao = new SecurityLogDaoImpl();
        this.auditLogDao = new AuditLogDaoImpl();
    }

    // ==================== SECURITY EVENTS ====================
    public void logLoginSuccess(int userId, String email, String ipAddress, String sessionId) {
        SecurityLog log = new SecurityLog(
                "LOGIN_SUCCESS", userId, email, ipAddress, sessionId, null);
        saveSecurityLog(log);
    }

    public void logLoginFailed(String email, String ipAddress, String reason) {
        SecurityLog log = new SecurityLog(
                "LOGIN_FAILED", null, email, ipAddress, null, reason);
        saveSecurityLog(log);
    }

    public void logLogout(int userId, String ipAddress, String sessionId) {
        SecurityLog log = new SecurityLog(
                "LOGOUT", userId, null, ipAddress, sessionId, null);
        saveSecurityLog(log);
    }

    public void logUnauthorizedAccess(int userId, String path, String ipAddress) {
        SecurityLog log = new SecurityLog(
                "UNAUTHORIZED_ACCESS", userId, null, ipAddress, null,
                "Attempted to access: " + path);
        saveSecurityLog(log);
    }

    public void logPasswordResetRequest(String email, String ipAddress) {
        SecurityLog log = new SecurityLog(
                "PASSWORD_RESET_REQUEST", null, email, ipAddress, null, null);
        saveSecurityLog(log);
    }

    public void logPasswordResetSuccess(String email, String ipAddress) {
        SecurityLog log = new SecurityLog(
                "PASSWORD_RESET_SUCCESS", null, email, ipAddress, null, null);
        saveSecurityLog(log);
    }

    // ==================== AUDIT EVENTS ====================
    public void logMovieCreated(int movieId, String title, int adminId, String ipAddress) {
        AuditLog log = new AuditLog(
                "MOVIE_CREATED", adminId, "movie", movieId,
                "Title: " + title, ipAddress);
        saveAuditLog(log);
    }

    public void logMovieUpdated(int movieId, String title, int adminId, String ipAddress) {
        AuditLog log = new AuditLog(
                "MOVIE_UPDATED", adminId, "movie", movieId,
                "Title: " + title, ipAddress);
        saveAuditLog(log);
    }

    public void logMovieDeleted(int movieId, String title, int adminId, String ipAddress) {
        AuditLog log = new AuditLog(
                "MOVIE_DELETED", adminId, "movie", movieId,
                "Title: " + title, ipAddress);
        saveAuditLog(log);
    }

    public void logBookingCreated(int bookingId, int userId, int showId,
            double amount, String ipAddress) {
        AuditLog log = new AuditLog(
                "BOOKING_CREATED", userId, "booking", bookingId,
                "ShowId: " + showId + ", Amount: " + amount, ipAddress);
        saveAuditLog(log);
    }

    public void logBookingCancelled(int bookingId, int userId, String reason, String ipAddress) {
        AuditLog log = new AuditLog(
                "BOOKING_CANCELLED", userId, "booking", bookingId,
                "Reason: " + reason, ipAddress);
        saveAuditLog(log);
    }

    public void logPaymentReceived(int bookingId, int userId, double amount,
            String method, String ipAddress) {
        AuditLog log = new AuditLog(
                "PAYMENT_RECEIVED", userId, "booking", bookingId,
                "Amount: " + amount + ", Method: " + method, ipAddress);
        saveAuditLog(log);
    }

    public List<AuditLogResponse> getLast20AuditLogs() {
        try {
            List<AuditLog> logs = auditLogDao.findLast20();
            return logs.stream()
                    .map(this::toAuditLogResponse)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Failed to fetch audit logs: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<SecurityLogResponse> getLast20SecurityLogs() {
        try {
            List<SecurityLog> logs = securityLogDao.findLast20();
            return logs.stream()
                    .map(this::toSecurityLogResponse)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Failed to fetch security logs: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private AuditLogResponse toAuditLogResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getLogId(),
                log.getTimestamp(),
                log.getEventType(),
                log.getUserId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress()
        );
    }

    private SecurityLogResponse toSecurityLogResponse(SecurityLog log) {
        return new SecurityLogResponse(
                log.getLogId(),
                log.getTimestamp(),
                log.getEventType(),
                log.getUserId(),
                log.getEmail(),
                log.getIpAddress(),
                log.getSessionId(),
                log.getDetails()
        );
    }

    // ==================== HELPER METHODS ====================
    private void saveSecurityLog(SecurityLog log) {
        try {
            securityLogDao.logEvent(log);
        } catch (SQLException e) {
            // Don't let logging failure break the application
            System.err.println("Failed to save security log: " + e.getMessage());
        }
    }

    private void saveAuditLog(AuditLog log) {
        try {
            auditLogDao.logEvent(log);
        } catch (SQLException e) {
            System.err.println("Failed to save audit log: " + e.getMessage());
        }
    }
}
