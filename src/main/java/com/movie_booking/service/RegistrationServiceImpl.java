package com.movie_booking.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.movie_booking.dao.RegistrationVerificationTokenDao;
import com.movie_booking.dao.RegistrationVerificationTokenDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.dto.request.RegisterRequest;
import com.movie_booking.dto.request.RegistrationVerifyRequest;
import com.movie_booking.dto.response.RegistrationResponse;
import com.movie_booking.model.PendingRegistration;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import com.movie_booking.util.DBConnection;

public class RegistrationServiceImpl implements RegistrationService {

    private static final int HASH_ITERATIONS = 120_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserDao userDao;
    private final RegistrationVerificationTokenDao tokenDao;
    private final EmailService emailService;

    public RegistrationServiceImpl() {
        this(new UserDaoImpl(), new RegistrationVerificationTokenDaoImpl(),
                new EmailService());
    }

    public RegistrationServiceImpl(
            UserDao userDao,
            RegistrationVerificationTokenDao tokenDao,
            EmailService emailService) {
        if (userDao == null || tokenDao == null || emailService == null) {
            throw new IllegalArgumentException(
                    "Registration service dependencies cannot be null.");
        }
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.emailService = emailService;
    }

    @Override
    public RegistrationResponse startRegistration(RegisterRequest request)
            throws SQLException {
        validateRequest(request);
        String email = request.getEmail().trim();

        if (userDao.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        String otp = generateOtp();
        PendingRegistration registration = new PendingRegistration();
        registration.setName(request.getName().trim());
        registration.setEmail(email);
        registration.setPasswordHash(hashValue(request.getPassword()));
        registration.setPhone(normalizeOptional(request.getPhone()));
        registration.setOtpHash(hashValue(otp));
        registration.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));

        tokenDao.invalidateByEmail(email);
        long registrationId = tokenDao.create(registration);
        emailService.sendRegistrationVerificationOtp(
                email,
                registration.getName(),
                otp);

        return new RegistrationResponse(
                registrationId,
                null,
                "Verification code sent to your email.",
                true,
                false);
    }

    @Override
    public RegistrationResponse verifyRegistration(
            RegistrationVerifyRequest request
    ) throws SQLException {
        if (request == null || request.getRegistrationId() <= 0) {
            throw new IllegalArgumentException("Registration ID is required.");
        }
        requireOtp(request.getOtp());

        PendingRegistration pending = tokenDao.findActiveById(
                request.getRegistrationId());
        if (pending == null) {
            throw new IllegalStateException(
                    "Registration request is missing or expired.");
        }
        if (pending.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Too many invalid OTP attempts.");
        }

        if (!matchesValue(request.getOtp(), pending.getOtpHash())) {
            if (!tokenDao.incrementAttemptCount(pending.getRegistrationId())) {
                throw new SQLException("Unable to record invalid OTP attempt.");
            }
            throw new IllegalArgumentException("Invalid verification code.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (userDao.findByEmail(pending.getEmail()) != null) {
                    throw new IllegalStateException("Email is already registered.");
                }

                User user = new User();
                user.setName(pending.getName());
                user.setEmail(pending.getEmail());
                user.setPasswordHash(pending.getPasswordHash());
                user.setPhone(pending.getPhone());
                user.setRole(UserRole.CUSTOMER);
                user.setStatus(UserStatus.ACTIVE);

                int userId = userDao.createUser(connection, user);
                if (!tokenDao.markAsUsed(
                        connection,
                        pending.getRegistrationId())) {
                    throw new SQLException(
                            "Registration verification could not be completed.");
                }

                connection.commit();
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());

                return new RegistrationResponse(
                        pending.getRegistrationId(),
                        userId,
                        "Registration completed successfully.",
                        false,
                        true);
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void validateRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request is required.");
        }
        requireText(request.getName(), "Name");
        requireText(request.getEmail(), "Email");
        requireText(request.getPassword(), "Password");
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }
    }

    private static void requireOtp(String otp) {
        if (otp == null || !otp.trim().matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "Verification code must contain exactly 6 digits.");
        }
    }

    private static String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private static String hashValue(String value) {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(value.toCharArray(), salt, HASH_ITERATIONS);
        return HASH_ITERATIONS + ":" + encode(salt) + ":" + encode(hash);
    }

    private static boolean matchesValue(String value, String storedHash) {
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
            byte[] actualHash = deriveKey(
                    value.trim().toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] deriveKey(
            char[] value,
            byte[] salt,
            int iterations) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(
                    value, salt, iterations, KEY_LENGTH);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(keySpec)
                        .getEncoded();
            } finally {
                keySpec.clearPassword();
            }
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Registration hashing is unavailable.", exception);
        }
    }

    private static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private static String normalizeOptional(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private static void rollback(Connection connection, Exception exception) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            exception.addSuppressed(rollbackException);
        }
    }
}
