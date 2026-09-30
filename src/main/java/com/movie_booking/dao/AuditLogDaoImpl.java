package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movie_booking.model.AuditLog;
import com.movie_booking.util.DBConnection;

public class AuditLogDaoImpl implements AuditLogDao {

    @Override
    public void logEvent(AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_logs "
                + "(event_type, user_id, entity_type, entity_id, details, ip_address) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, log.getEventType());
            statement.setInt(2, log.getUserId());
            statement.setString(3, log.getEntityType());
            if (log.getEntityId() != null) {
                statement.setInt(4, log.getEntityId());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            statement.setString(5, log.getDetails());
            statement.setString(6, log.getIpAddress());

            statement.executeUpdate();
        }
    }

    @Override
    public List<AuditLog> findLast20() throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 20";

        try (Connection conn = DBConnection.getConnection(); 
        PreparedStatement stmt = conn.prepareStatement(sql); 
        ResultSet rs = stmt.executeQuery()) 
        {

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }
        return logs;
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getLong("log_id"));
        log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        log.setEventType(rs.getString("event_type"));
        log.setUserId(rs.getInt("user_id"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getObject("entity_id") != null ? rs.getInt("entity_id") : null);
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));
        return log;
    }
}
