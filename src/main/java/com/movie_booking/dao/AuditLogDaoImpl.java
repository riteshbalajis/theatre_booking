package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.movie_booking.model.AuditLog;
import com.movie_booking.util.DBConnection;

public class AuditLogDaoImpl implements AuditLogDao {

    @Override
    public void logEvent(AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_logs "
                   + "(event_type, user_id, entity_type, entity_id, details, ip_address) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

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
}