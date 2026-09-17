package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.movie_booking.model.PendingRegistration;
import com.movie_booking.util.DBConnection;

public class RegistrationVerificationTokenDaoImpl
        implements RegistrationVerificationTokenDao {

    @Override
    public long create(PendingRegistration registration) throws SQLException {
        validateRegistration(registration);

        String sql = "INSERT INTO registration_verification_tokens "
                + "(name, email, password_hash, phone, otp_hash, expires_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, registration.getName());
            statement.setString(2, registration.getEmail());
            statement.setString(3, registration.getPasswordHash());
            statement.setString(4, registration.getPhone());
            statement.setString(5, registration.getOtpHash());
            statement.setTimestamp(6, Timestamp.valueOf(registration.getExpiresAt()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating registration verification token failed: no ID was generated.");
    }

    @Override
    public void invalidateByEmail(String email) throws SQLException {
        requireEmail(email);

        String sql = "UPDATE registration_verification_tokens "
                + "SET used = TRUE "
                + "WHERE email = ? AND used = FALSE";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            statement.executeUpdate();
        }
    }

    @Override
    public PendingRegistration findActiveById(long registrationId)
            throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findActiveById(connection, registrationId);
        }
    }

    @Override
    public PendingRegistration findActiveById(
            Connection connection,
            long registrationId
    ) throws SQLException {
        if (registrationId <= 0) {
            throw new IllegalArgumentException("Registration ID must be positive.");
        }

        String sql = "SELECT registration_id, name, email, password_hash, phone, "
                + "otp_hash, expires_at, attempt_count, used, created_at "
                + "FROM registration_verification_tokens "
                + "WHERE registration_id = ? "
                + "AND used = FALSE "
                + "AND expires_at > CURRENT_TIMESTAMP";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, registrationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRegistration(resultSet) : null;
            }
        }
    }

    @Override
    public boolean incrementAttemptCount(long registrationId)
            throws SQLException {
        String sql = "UPDATE registration_verification_tokens "
                + "SET attempt_count = attempt_count + 1 "
                + "WHERE registration_id = ? AND used = FALSE";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, registrationId);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean markAsUsed(
            Connection connection,
            long registrationId
    ) throws SQLException {
        String sql = "UPDATE registration_verification_tokens "
                + "SET used = TRUE "
                + "WHERE registration_id = ? "
                + "AND used = FALSE "
                + "AND expires_at > CURRENT_TIMESTAMP";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, registrationId);
            return statement.executeUpdate() == 1;
        }
    }

    private PendingRegistration mapRegistration(ResultSet resultSet)
            throws SQLException {
        PendingRegistration registration = new PendingRegistration();
        registration.setRegistrationId(resultSet.getLong("registration_id"));
        registration.setName(resultSet.getString("name"));
        registration.setEmail(resultSet.getString("email"));
        registration.setPasswordHash(resultSet.getString("password_hash"));
        registration.setPhone(resultSet.getString("phone"));
        registration.setOtpHash(resultSet.getString("otp_hash"));
        registration.setExpiresAt(
                resultSet.getTimestamp("expires_at").toLocalDateTime());
        registration.setAttemptCount(resultSet.getInt("attempt_count"));
        registration.setUsed(resultSet.getBoolean("used"));
        registration.setCreatedAt(
                resultSet.getTimestamp("created_at").toLocalDateTime());
        return registration;
    }

    private static void validateRegistration(PendingRegistration registration) {
        if (registration == null) {
            throw new IllegalArgumentException("Registration data is required.");
        }
        requireText(registration.getName(), "Name");
        requireEmail(registration.getEmail());
        requireText(registration.getPasswordHash(), "Password hash");
        requireText(registration.getOtpHash(), "OTP hash");
        if (registration.getExpiresAt() == null) {
            throw new IllegalArgumentException("Registration expiry is required.");
        }
    }

    private static void requireEmail(String email) {
        requireText(email, "Email");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }
}
