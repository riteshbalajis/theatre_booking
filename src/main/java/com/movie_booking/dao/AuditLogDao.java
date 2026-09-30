package com.movie_booking.dao;

import java.sql.SQLException;
import java.util.List;

import com.movie_booking.model.AuditLog;

public interface AuditLogDao {

    void logEvent(AuditLog log) throws SQLException;

    List<AuditLog> findLast20() throws SQLException;

}
