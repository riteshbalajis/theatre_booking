package com.movie_booking.service;

import java.sql.SQLException;

import com.movie_booking.dto.request.RegisterRequest;
import com.movie_booking.dto.request.RegistrationVerifyRequest;
import com.movie_booking.dto.response.RegistrationResponse;

public interface RegistrationService {

    RegistrationResponse startRegistration(RegisterRequest request)
            throws SQLException;

    RegistrationResponse verifyRegistration(
            RegistrationVerifyRequest request
    ) throws SQLException;
}
