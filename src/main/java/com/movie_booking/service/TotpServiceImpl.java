package com.movie_booking.service;

import java.sql.SQLException;

import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.dto.response.TotpSetupResponse;
import com.movie_booking.model.User;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

public class TotpServiceImpl implements TotpService {

    private final UserDao userDao;
    private final SecretGenerator secretGenerator;

    private final CodeVerifier codeVerifier;

    public TotpServiceImpl() {

        this.userDao = new UserDaoImpl();
        this.secretGenerator = new DefaultSecretGenerator();

        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        TimeProvider timeProvider = new SystemTimeProvider();

        this.codeVerifier
                = new DefaultCodeVerifier(
                        codeGenerator,
                        timeProvider
                );
    }

    @Override
    public TotpSetupResponse setupTotp(int userId)
            throws SQLException {

        User user = userDao.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "User not found."
            );
        }

        boolean enabled = userDao.isTotpEnabled(userId);

        if (enabled) {
            throw new IllegalStateException(
                    "TOTP is already enabled."
            );
        }

        String secret = secretGenerator.generate();

        boolean saved
                = userDao.saveTotpSecret(
                        userId,
                        secret
                );

        if (!saved) {
            throw new IllegalStateException(
                    "Unable to save TOTP secret."
            );
        }

        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer("Screenly")
                .build();

        return new TotpSetupResponse(
                secret,
                qrData.getUri()
        );
    }

    @Override
    public boolean verifySetupCode(
            int userId,
            String code
    ) throws SQLException {

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "TOTP code is required."
            );
        }

        code = code.trim();

        if (!code.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "TOTP code must contain exactly 6 digits."
            );
        }

        boolean enabled = userDao.isTotpEnabled(userId);

        if (enabled) {
            throw new IllegalStateException(
                    "TOTP is already enabled."
            );
        }

        String secret = userDao.getTotpSecret(userId);

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "TOTP setup has not been started."
            );
        }

        boolean valid = codeVerifier.isValidCode(
                secret,
                code
        );

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid TOTP code."
            );
        }

        boolean enabledSuccessfully
                = userDao.enableTotp(userId);

        if (!enabledSuccessfully) {
            throw new IllegalStateException(
                    "Unable to enable TOTP."
            );
        }

        return true;
    }

    @Override
    public boolean verifyLoginCode(
            int userId,
            String code
    ) throws SQLException {

        if (code == null || !code.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "Invalid TOTP code."
            );
        }

        boolean enabled = userDao.isTotpEnabled(userId);

        if (!enabled) {
            throw new IllegalStateException(
                    "TOTP is not enabled."
            );
        }

        String secret = userDao.getTotpSecret(userId);

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "TOTP secret not found."
            );
        }

        boolean valid = codeVerifier.isValidCode(
                secret,
                code
        );

        return valid;
    }

    @Override
    public boolean disableTotp(int userId, String code) throws SQLException {

        if (code == null || !code.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "Invalid TOTP code."
            );
        }

        boolean enabled
                = userDao.isTotpEnabled(userId);

        if (!enabled) {
            throw new IllegalStateException(
                    "TOTP is not enabled."
            );
        }

        String secret
                = userDao.getTotpSecret(userId);

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "TOTP secret not found."
            );
        }

        boolean valid
                = codeVerifier.isValidCode(
                        secret,
                        code
                );

        if (!valid) {
            return false;
        }

        return userDao.disableTotp(userId);
    }

    @Override
    public TotpSetupResponse regenerateTotp(int userId) throws SQLException {

        User user = userDao.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "User not found."
            );
        }

        boolean enabled = userDao.isTotpEnabled(userId);

        if (!enabled) {
            throw new IllegalStateException("TOTP is not currently enabled.");
        }

        String newSecret = secretGenerator.generate();

        boolean regenerated = userDao.regenerateTotpSecret(userId, newSecret);

        if (!regenerated) {
            throw new IllegalStateException(
                    "Unable to regenerate TOTP."
            );
        }

        QrData qrData
                = new QrData.Builder()
                        .label(user.getEmail())
                        .secret(newSecret)
                        .issuer("Screenly")
                        .build();

        return new TotpSetupResponse(
                newSecret,
                qrData.getUri()
        );
    }

    @Override
    public boolean isTotpEnabled(int userId) throws SQLException {
        return userDao.isTotpEnabled(userId);
    }

}
