package com.movie_booking.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.movie_booking.dao.TotpRecoveryTokenDao;
import com.movie_booking.dao.TotpRecoveryTokenDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.model.TotpRecoveryMethod;
import com.movie_booking.model.TotpRecoveryToken;
import com.movie_booking.model.User;
import com.movie_booking.model.UserStatus;

public class TotpRecoveryServiceImpl implements TotpRecoveryService {

    private static final int HASH_ITERATIONS = 120_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserDao userDao;
    private final TotpRecoveryTokenDao recoveryTokenDao;
    private final EmailService emailService;

    public TotpRecoveryServiceImpl() {
        this(new UserDaoImpl(), new TotpRecoveryTokenDaoImpl(), new EmailService());
    }

    public TotpRecoveryServiceImpl(
            UserDao userDao,
            TotpRecoveryTokenDao recoveryTokenDao,
            EmailService emailService) {
        if (userDao == null || recoveryTokenDao == null || emailService == null) {
            throw new IllegalArgumentException(
                    "TOTP recovery dependencies cannot be null.");
        }
        this.userDao = userDao;
        this.recoveryTokenDao = recoveryTokenDao;
        this.emailService = emailService;
    }

    @Override
    public boolean requestEmailRecovery(int userId) throws SQLException {
        requirePositiveId(userId);

        User user = userDao.findById(userId);
        validateRecoveryUser(user);

        String otp = generateOtp();
        TotpRecoveryMethod method = TotpRecoveryMethod.EMAIL;

        recoveryTokenDao.invalidateActiveTokens(userId, method);

        TotpRecoveryToken token = new TotpRecoveryToken();
        token.setUserId(userId);
        token.setRecoveryMethod(method);
        token.setOtpHash(hashOtp(otp));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        token.setUsed(false);
        token.setAttemptCount(0);

        recoveryTokenDao.create(token);
        emailService.sendTotpRecoveryOtp(user.getEmail(), otp);
        return true;
    }

    @Override
    public boolean verifyEmailRecovery(int userId, String otp) throws SQLException {
        requirePositiveId(userId);
        requireOtp(otp);

        TotpRecoveryToken token = recoveryTokenDao.findLatestActiveToken(
                userId,
                TotpRecoveryMethod.EMAIL);

        if (token == null) {
            throw new IllegalStateException(
                    "No active email recovery request found.");
        }

        if (token.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalStateException(
                    "Too many invalid OTP attempts.");
        }

        if (!matchesOtp(otp, token.getOtpHash())) {
            if (!recoveryTokenDao.incrementAttemptCount(token.getRecoveryId())) {
                throw new SQLException("Unable to record invalid OTP attempt.");
            }
            return false;
        }

        if (!recoveryTokenDao.markAsUsed(token.getRecoveryId())) {
            throw new IllegalStateException(
                    "Recovery OTP could not be consumed.");
        }

        return true;
    }

    private void validateRecoveryUser(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User account is not active.");
        }
        if (!userDao.isTotpEnabled(user.getUserId())) {
            throw new IllegalStateException("TOTP is not enabled.");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalStateException("User email is not available.");
        }
    }

    private static String generateOtp() {
        return String.format("%0" + OTP_LENGTH + "d",
                SECURE_RANDOM.nextInt(1_000_000));
    }

    private static void requireOtp(String otp) {
        if (otp == null || !otp.trim().matches("\\d{" + OTP_LENGTH + "}")) {
            throw new IllegalArgumentException("OTP must contain exactly 6 digits.");
        }
    }

    private static String hashOtp(String otp) {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(otp.toCharArray(), salt, HASH_ITERATIONS);
        return HASH_ITERATIONS + ":" + encode(salt) + ":" + encode(hash);
    }

    private static boolean matchesOtp(String otp, String storedHash) {
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
            byte[] actualHash = deriveKey(otp.trim().toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] deriveKey(char[] value, byte[] salt, int iterations) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(value, salt, iterations, KEY_LENGTH);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(keySpec)
                        .getEncoded();
            } finally {
                keySpec.clearPassword();
            }
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("OTP hashing is unavailable.", exception);
        }
    }

    private static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private static void requirePositiveId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
    }
}
