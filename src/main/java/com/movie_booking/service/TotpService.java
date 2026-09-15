package com.movie_booking.service;

import java.sql.SQLException;

import com.movie_booking.dto.response.TotpSetupResponse;

public interface TotpService {

    TotpSetupResponse setupTotp(int userId)
            throws SQLException;

    boolean verifySetupCode(
            int userId,
            String code
    ) throws SQLException;

    boolean verifyLoginCode(
            int userId,
            String code
    ) throws SQLException;

    TotpSetupResponse regenerateTotp(int userId) throws SQLException;

    boolean disableTotp(int userId,String code) throws SQLException;

    boolean isTotpEnabled(int userId) throws SQLException;
}