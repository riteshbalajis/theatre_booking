package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.movie_booking.model.PasswordResetToken;

public interface PasswordResetTokenDao {

    int create(PasswordResetToken token) throws SQLException;

    PasswordResetToken findLatestByUserId(int userId) throws SQLException;

    boolean markAsUsed(int resetId) throws SQLException;

    void deleteByUserId(int userId) throws SQLException;

    boolean markAsVerified(int resetId,String resetTokenHash,LocalDateTime resetTokenExpiresAt) throws SQLException;

    boolean consumeResetToken(int resetId) throws SQLException;
    boolean consumeResetToken(Connection connection,int resetId) throws SQLException;
}