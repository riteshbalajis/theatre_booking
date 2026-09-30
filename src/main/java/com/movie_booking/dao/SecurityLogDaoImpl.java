package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movie_booking.model.SecurityLog;
import com.movie_booking.util.DBConnection;

public class SecurityLogDaoImpl implements SecurityLogDao {

    @Override
    public void logEvent(SecurityLog log) throws SQLException {
        String sql = "INSERT INTO security_logs "
                + "(event_type, user_id, email, ip_address, session_id, details) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, log.getEventType());
            if (log.getUserId() != null) {
                statement.setInt(2, log.getUserId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            statement.setString(3, log.getEmail());
            statement.setString(4, log.getIpAddress());
            statement.setString(5, log.getSessionId());
            statement.setString(6, log.getDetails());

            statement.executeUpdate();
        }
    }

    @Override
    public List<SecurityLog> findLast20() throws SQLException {
        List<SecurityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 20";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapResultSetToSecurityLog(rs));
            }
        }
        return logs;
    }

    private SecurityLog mapResultSetToSecurityLog(ResultSet rs) throws SQLException {
        SecurityLog log = new SecurityLog();
        log.setLogId(rs.getLong("log_id"));
        log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        log.setEventType(rs.getString("event_type"));
        log.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        log.setEmail(rs.getString("email"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setSessionId(rs.getString("session_id"));
        log.setDetails(rs.getString("details"));
        return log;
    }
}
