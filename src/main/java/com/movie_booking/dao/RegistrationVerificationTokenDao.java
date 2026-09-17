package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.movie_booking.model.PendingRegistration;

public interface RegistrationVerificationTokenDao {

    long create(PendingRegistration registration) throws SQLException;

    void invalidateByEmail(String email) throws SQLException;

    PendingRegistration findActiveById(long registrationId) throws SQLException;

    PendingRegistration findActiveById(
            Connection connection,
            long registrationId
    ) throws SQLException;

    boolean incrementAttemptCount(long registrationId) throws SQLException;

    boolean markAsUsed(
            Connection connection,
            long registrationId
    ) throws SQLException;
}
