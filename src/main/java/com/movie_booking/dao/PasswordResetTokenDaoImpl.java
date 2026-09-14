package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.movie_booking.model.PasswordResetToken;
import com.movie_booking.util.DBConnection;

public class PasswordResetTokenDaoImpl implements PasswordResetTokenDao {

    @Override
    public int create(PasswordResetToken token) throws SQLException {

        String sql = "INSERT INTO password_reset_tokens "
                + "(user_id, otp_hash, expires_at) "
                + "VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, token.getUserId());
            statement.setString(2, token.getOtpHash());
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(token.getExpiresAt())
            );

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Creating password reset token failed: no ID was generated."
        );
    }

    @Override
    public PasswordResetToken findLatestByUserId(int userId)
            throws SQLException {

        String sql = "SELECT reset_id, user_id, otp_hash, "
                + "expires_at, used, created_at, "
                + "reset_token_hash, reset_token_expires_at, "
                + "reset_verified "
                + "FROM password_reset_tokens "
                + "WHERE user_id = ? "
                + "ORDER BY created_at DESC "
                + "LIMIT 1";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapToken(resultSet);
                }

                return null;
            }
        }
    }

    @Override
    public boolean markAsUsed(int resetId) throws SQLException {

        String sql = "UPDATE password_reset_tokens "
                + "SET used = TRUE "
                + "WHERE reset_id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, resetId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public void deleteByUserId(int userId) throws SQLException {

        String sql = "DELETE FROM password_reset_tokens "
                + "WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            statement.executeUpdate();
        }
    }

    @Override
    public boolean markAsVerified(
            int resetId,
            String resetTokenHash,
            LocalDateTime resetTokenExpiresAt
    ) throws SQLException {

        String sql = "UPDATE password_reset_tokens "
                + "SET used = TRUE, "
                + "reset_token_hash = ?, "
                + "reset_token_expires_at = ?, "
                + "reset_verified = TRUE "
                + "WHERE reset_id = ? "
                + "AND used = FALSE "
                + "AND reset_verified = FALSE "
                + "AND expires_at > CURRENT_TIMESTAMP";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(1, resetTokenHash);

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(resetTokenExpiresAt)
            );

            statement.setInt(3, resetId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean consumeResetToken(int resetId) throws SQLException {

        String sql
                = "UPDATE password_reset_tokens "
                + "SET reset_verified = FALSE "
                + "WHERE reset_id = ? "
                + "AND reset_verified = TRUE "
                + "AND reset_token_expires_at > CURRENT_TIMESTAMP";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, resetId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean consumeResetToken(
            Connection connection,
            int resetId
    ) throws SQLException {

        String sql
                = "UPDATE password_reset_tokens "
                + "SET reset_verified = FALSE "
                + "WHERE reset_id = ? "
                + "AND reset_verified = TRUE "
                + "AND reset_token_expires_at > CURRENT_TIMESTAMP";

        try (PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, resetId);

            return statement.executeUpdate() > 0;
        }
    }

    private PasswordResetToken mapToken(ResultSet resultSet)
            throws SQLException {

        PasswordResetToken token = new PasswordResetToken();

        token.setResetId(resultSet.getInt("reset_id"));

        token.setUserId(resultSet.getInt("user_id"));

        token.setOtpHash(
                resultSet.getString("otp_hash")
        );

        Timestamp expiresAt
                = resultSet.getTimestamp("expires_at");

        token.setExpiresAt(
                expiresAt == null
                        ? null
                        : expiresAt.toLocalDateTime()
        );

        token.setUsed(
                resultSet.getBoolean("used")
        );

        Timestamp createdAt
                = resultSet.getTimestamp("created_at");

        token.setCreatedAt(
                createdAt == null
                        ? null
                        : createdAt.toLocalDateTime()
        );

        token.setResetTokenHash(
                resultSet.getString("reset_token_hash")
        );

        Timestamp resetTokenExpiresAt
                = resultSet.getTimestamp("reset_token_expires_at");

        token.setResetTokenExpiresAt(
                resetTokenExpiresAt == null
                        ? null
                        : resetTokenExpiresAt.toLocalDateTime()
        );

        token.setResetVerified(
                resultSet.getBoolean("reset_verified")
        );

        return token;
    }
}
