package com.movie_booking.dao;

import java.sql.SQLException;

import com.movie_booking.model.SecurityLog;

public interface SecurityLogDao {
    void logEvent(SecurityLog log) throws SQLException;
}