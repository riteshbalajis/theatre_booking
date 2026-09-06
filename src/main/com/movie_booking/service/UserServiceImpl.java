package com.movie_booking.service;

import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.dto.request.ChangePasswordRequest;
import com.movie_booking.dto.request.LoginRequest;
import com.movie_booking.dto.request.RegisterRequest;
import com.movie_booking.dto.request.UserUpdateRequest;
import com.movie_booking.dto.response.LoginResponse;
import com.movie_booking.dto.response.UserResponse;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UserServiceImpl implements UserService {
    private static final int HASH_ITERATIONS = 120_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserDao userDao;

    public UserServiceImpl() {
        this(new UserDaoImpl());
    }

    public UserServiceImpl(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException("User DAO cannot be null.");
        }
        this.userDao = userDao;
    }

    @Override
    public int registerUser(RegisterRequest request) throws SQLException {
        User user = toUser(request);
        validateRegistration(user);
        if (userDao.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        user.setPasswordHash(hashPassword(user.getPasswordHash()));
        if (user.getRole() == null) {
            user.setRole(UserRole.CUSTOMER);
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }
        return userDao.createUser(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null.");
        }
        requireText(request.getEmail(), "Email");
        requireText(request.getPassword(), "Password");
        User user = userDao.findByEmail(request.getEmail().trim());
        if (user == null || user.getStatus() != UserStatus.ACTIVE
                || !matchesPassword(request.getPassword(), user.getPasswordHash())) {
            return null;
        }
        return new LoginResponse(toUserResponse(user), "Login successful.");
    }

    @Override
    public UserResponse getUserById(int userId) throws SQLException {
        return toUserResponse(userDao.findById(userId));
    }

    @Override
    public UserResponse getUserByEmail(String email) throws SQLException {
        requireText(email, "Email");
        return toUserResponse(userDao.findByEmail(email.trim()));
    }

    @Override
    public boolean updateProfile(UserUpdateRequest request) throws SQLException {
        User user = toUser(request);
        if (user == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("A valid user is required.");
        }
        requireText(user.getName(), "Name");
        requireText(user.getEmail(), "Email");
        User existing = userDao.findByEmail(user.getEmail().trim());
        if (existing != null && existing.getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        user.setEmail(user.getEmail().trim());
        return userDao.updateUser(user);
    }

    @Override
    public boolean changePassword(ChangePasswordRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Change-password request cannot be null.");
        }
        requirePositiveId(request.getUserId());
        requireText(request.getCurrentPassword(), "Current password");
        validatePassword(request.getNewPassword());
        User user = userDao.findById(request.getUserId());
        if (user == null || !matchesPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            return false;
        }
        return userDao.updatePassword(request.getUserId(), hashPassword(request.getNewPassword()));
    }

    @Override
    public boolean activateUser(int userId) throws SQLException {
        requirePositiveId(userId);
        return userDao.activateUser(userId);
    }

    @Override
    public boolean deactivateUser(int userId) throws SQLException {
        requirePositiveId(userId);
        return userDao.deactivateUser(userId);
    }

    @Override
    public List<UserResponse> getAllUsers() throws SQLException {
        return toUserResponses(userDao.findAll());
    }

    @Override
    public List<UserResponse> getUsersByRole(UserRole role) throws SQLException {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        return toUserResponses(userDao.findAllByRole(role));
    }

    @Override
    public List<UserResponse> getUsersByStatus(UserStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        return toUserResponses(userDao.findAllByStatus(status));
    }

    @Override
    public List<UserResponse> getUsersByRoleAndStatus(UserRole role, UserStatus status)
            throws SQLException {
        if (role == null || status == null) {
            throw new IllegalArgumentException("Role and status cannot be null.");
        }
        return toUserResponses(userDao.findByRoleAndStatus(role, status));
    }

    @Override
    public boolean emailExists(String email) throws SQLException {
        requireText(email, "Email");
        return userDao.existsByEmail(email.trim());
    }

    private static User toUser(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null.");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setPhone(request.getPhone());
        return user;
    }

    private static User toUser(UserUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("User update request cannot be null.");
        }
        User user = new User();
        user.setUserId(request.getUserId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return user;
    }

    private static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private static List<UserResponse> toUserResponses(List<User> users) {
        return users.stream().map(UserServiceImpl::toUserResponse).collect(Collectors.toList());
    }

    private void validateRegistration(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        requireText(user.getName(), "Name");
        requireText(user.getEmail(), "Email");
        validatePassword(user.getPasswordHash());
        user.setEmail(user.getEmail().trim());
    }

    private static void validatePassword(String password) {
        requireText(password, "Password");
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    private static void requirePositiveId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive.");
        }
    }

    private static String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password.toCharArray(), salt, HASH_ITERATIONS);
        return HASH_ITERATIONS + ":" + encode(salt) + ":" + encode(hash);
    }

    private static boolean matchesPassword(String password, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split(":", -1);
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            byte[] actualHash = deriveKey(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] deriveKey(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(keySpec).getEncoded();
            } finally {
                keySpec.clearPassword();
            }
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        }
    }

    private static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }
}
