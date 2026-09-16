package com.movie_booking.dao;

import java.sql.SQLException;

import com.movie_booking.model.TotpRecoveryMethod;
import com.movie_booking.model.TotpRecoveryToken;

public interface TotpRecoveryTokenDao {

    long create(TotpRecoveryToken token) throws SQLException;

    boolean invalidateActiveTokens(int userId,TotpRecoveryMethod recoveryMethod) throws SQLException;

    TotpRecoveryToken findLatestActiveToken(
            int userId,
            TotpRecoveryMethod recoveryMethod
    ) throws SQLException;

    boolean incrementAttemptCount(long recoveryId) throws SQLException;

    boolean markAsUsed(long recoveryId) throws SQLException;
}
