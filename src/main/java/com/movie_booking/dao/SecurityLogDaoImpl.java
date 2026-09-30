package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.movie_booking.model.SecurityLog;
import com.movie_booking.util.DBConnection;

public class SecurityLogDaoImpl implements SecurityLogDao {

    @Override
    public void logEvent(SecurityLog log) throws SQLException {
        String sql = "INSERT INTO security_logs "
                   + "(event_type, user_id, email, ip_address, session_id, details) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

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
}