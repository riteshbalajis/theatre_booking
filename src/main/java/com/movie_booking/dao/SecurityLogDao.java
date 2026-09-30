package com.movie_booking.dao;

import java.sql.SQLException;
import java.util.List;

import com.movie_booking.model.SecurityLog;

public interface SecurityLogDao {
    void logEvent(SecurityLog log) throws SQLException;

    public List<SecurityLog> findLast20() throws SQLException;
}