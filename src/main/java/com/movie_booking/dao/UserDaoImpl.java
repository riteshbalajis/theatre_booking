package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import com.movie_booking.util.DBConnection;

public class UserDaoImpl implements UserDao {

    private static final String BASE_SELECT = "SELECT user_id, name, email, password_hash, google_sub, phone, "
            + "role, status, created_at FROM users";

    @Override
    public int createUser(User user) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return createUser(connection, user);
        }
    }

    @Override
    public int createUser(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO users "
                + "(name, email, password_hash, google_sub, phone, role, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getGoogleSub());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getRole() == null
                    ? UserRole.CUSTOMER.name() : user.getRole().name());
            statement.setString(7, user.getStatus() == null
                    ? UserStatus.ACTIVE.name() : user.getStatus().name());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Creating user failed: no ID was generated.");
    }

    @Override
    public User findById(int userId) throws SQLException {
        String sql = BASE_SELECT + " WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        String sql = BASE_SELECT + " WHERE email = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    @Override
    public User findByGoogleSub(String googleSub) throws SQLException {
        String sql = BASE_SELECT + " WHERE google_sub = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, googleSub);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    @Override
    public void updateGoogleSub(int userId, String googleSub) throws SQLException {
        String sql = "UPDATE users SET google_sub = ? WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, googleSub);
            statement.setInt(2, userId);

            statement.executeUpdate();
        }
    }

    //totp methods 
    @Override
    public boolean isTotpEnabled(int userId) throws SQLException {

        String sql
                = "SELECT totp_enabled "
                + "FROM users "
                + "WHERE user_id = ?";

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getBoolean("totp_enabled");
                }

                return false;
            }
        }
    }

    @Override
    public String getTotpSecret(int userId) throws SQLException {

        String sql
                = "SELECT totp_secret "
                + "FROM users "
                + "WHERE user_id = ?";

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("totp_secret");
                }

                return null;
            }
        }
    }

    @Override
    public boolean saveTotpSecret(int userId, String secret
    ) throws SQLException {

        String sql
                = "UPDATE users "
                + "SET totp_secret = ? "
                + "WHERE user_id = ? "
                + "AND totp_enabled = FALSE";

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(1, secret);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean regenerateTotpSecret(
            int userId,
            String newSecret
    ) throws SQLException {

        String sql
                = "UPDATE users "
                + "SET totp_secret = ?, "
                + "    totp_enabled = FALSE "
                + "WHERE user_id = ? "
                + "AND totp_enabled = TRUE "
                + "AND totp_secret IS NOT NULL";

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(1, newSecret);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean enableTotp(int userId) throws SQLException {

        String sql
                = "UPDATE users "
                + "SET totp_enabled = TRUE "
                + "WHERE user_id = ? "
                + "AND totp_secret IS NOT NULL "
                + "AND totp_enabled = FALSE";

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean disableTotp(int userId) throws SQLException {

        String sql
                = "UPDATE users "
                + "SET totp_enabled = FALSE, "
                + "    totp_secret = NULL "
                + "WHERE user_id = ? "
                + "AND totp_enabled = TRUE";

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, role = ? "
                + "WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getRole().name());
            statement.setInt(5, user.getUserId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(int userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(Connection connection, int userId, String passwordHash) throws SQLException {

        String sql
                = "UPDATE users "
                + "SET password_hash = ? "
                + "WHERE user_id = ?";

        try (PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deactivateUser(int userId) throws SQLException {
        return updateStatus(userId, UserStatus.INACTIVE);
    }

    @Override
    public boolean activateUser(int userId) throws SQLException {
        return updateStatus(userId, UserStatus.ACTIVE);
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = BASE_SELECT + " ORDER BY user_id";
        return findUsers(sql);
    }

    @Override
    public List<User> findAllByRole(UserRole role) throws SQLException {
        String sql = BASE_SELECT + " WHERE role = ? ORDER BY user_id";
        return findUsersByEnum(sql, role.name());
    }

    @Override
    public List<User> findAllByStatus(UserStatus status) throws SQLException {
        String sql = BASE_SELECT + " WHERE status = ? ORDER BY user_id";
        return findUsersByEnum(sql, status.name());
    }

    @Override
    public List<User> findByRoleAndStatus(UserRole role, UserStatus status) throws SQLException {
        String sql = BASE_SELECT + " WHERE role = ? AND status = ? ORDER BY user_id";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            statement.setString(2, status.name());
            return readUsers(statement);
        }
    }

    private boolean updateStatus(int userId, UserStatus status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private List<User> findUsers(String sql) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            return readUsers(statement);
        }
    }

    private List<User> findUsersByEnum(String sql, String value) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            return readUsers(statement);
        }
    }

    private List<User> readUsers(PreparedStatement statement) throws SQLException {
        List<User> users = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setGoogleSub(resultSet.getString("google_sub"));
        user.setPhone(resultSet.getString("phone"));
        user.setRole(UserRole.valueOf(resultSet.getString("role")));
        user.setStatus(UserStatus.valueOf(resultSet.getString("status")));
        user.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return user;
    }
}
