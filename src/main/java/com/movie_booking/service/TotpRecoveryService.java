package com.movie_booking.service;

import java.sql.SQLException;

public interface TotpRecoveryService {

    boolean requestEmailRecovery(int userId) throws SQLException;

    boolean verifyEmailRecovery(int userId, String otp) throws SQLException;
}
