package com.movie_booking.dao;

import java.sql.SQLException;
import com.movie_booking.model.AuditLog;

public interface AuditLogDao {
    void logEvent(AuditLog log) throws SQLException;
}