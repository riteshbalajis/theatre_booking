package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.movie_booking.model.TotpRecoveryMethod;
import com.movie_booking.model.TotpRecoveryToken;
import com.movie_booking.util.DBConnection;

public class TotpRecoveryTokenDaoImpl implements TotpRecoveryTokenDao {

    @Override
    public long create(TotpRecoveryToken token) throws SQLException {
        if (token == null || token.getRecoveryMethod() == null
                || token.getOtpHash() == null || token.getExpiresAt() == null) {
            throw new IllegalArgumentException("Recovery token data is incomplete.");
        }

        String sql = "INSERT INTO totp_recovery_tokens "
                + "(user_id, recovery_method, otp_hash, expires_at) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, token.getUserId());
            statement.setString(2, token.getRecoveryMethod().name());
            statement.setString(3, token.getOtpHash());
            statement.setTimestamp(4, Timestamp.valueOf(token.getExpiresAt()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Creating TOTP recovery token failed: no ID was generated.");
    }

    @Override
    public boolean invalidateActiveTokens(
            int userId,
            TotpRecoveryMethod recoveryMethod
    ) throws SQLException {

        if (recoveryMethod == null) {
            throw new IllegalArgumentException("Recovery method is required.");
        }

        String sql = "UPDATE totp_recovery_tokens "
                + "SET used = TRUE "
                + "WHERE user_id = ? "
                + "AND recovery_method = ? "
                + "AND used = FALSE";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, recoveryMethod.name());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public TotpRecoveryToken findLatestActiveToken(
            int userId,
            TotpRecoveryMethod recoveryMethod
    ) throws SQLException {

        if (recoveryMethod == null) {
            throw new IllegalArgumentException("Recovery method is required.");
        }

        String sql = "SELECT recovery_id, user_id, recovery_method, otp_hash, "
                + "expires_at, used, attempt_count, created_at "
                + "FROM totp_recovery_tokens "
                + "WHERE user_id = ? "
                + "AND recovery_method = ? "
                + "AND used = FALSE "
                + "AND expires_at > CURRENT_TIMESTAMP "
                + "ORDER BY created_at DESC, recovery_id DESC "
                + "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, recoveryMethod.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToken(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public boolean incrementAttemptCount(long recoveryId) throws SQLException {
        String sql = "UPDATE totp_recovery_tokens "
                + "SET attempt_count = attempt_count + 1 "
                + "WHERE recovery_id = ? "
                + "AND used = FALSE";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, recoveryId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean markAsUsed(long recoveryId) throws SQLException {
        String sql = "UPDATE totp_recovery_tokens "
                + "SET used = TRUE "
                + "WHERE recovery_id = ? "
                + "AND used = FALSE "
                + "AND expires_at > CURRENT_TIMESTAMP";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, recoveryId);
            return statement.executeUpdate() > 0;
        }
    }

    private TotpRecoveryToken mapToken(ResultSet resultSet) throws SQLException {
        TotpRecoveryToken token = new TotpRecoveryToken();
        token.setRecoveryId(resultSet.getLong("recovery_id"));
        token.setUserId(resultSet.getInt("user_id"));
        token.setRecoveryMethod(TotpRecoveryMethod.valueOf(
                resultSet.getString("recovery_method")));
        token.setOtpHash(resultSet.getString("otp_hash"));
        token.setExpiresAt(resultSet.getTimestamp("expires_at").toLocalDateTime());
        token.setUsed(resultSet.getBoolean("used"));
        token.setAttemptCount(resultSet.getInt("attempt_count"));
        token.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return token;
    }
}
