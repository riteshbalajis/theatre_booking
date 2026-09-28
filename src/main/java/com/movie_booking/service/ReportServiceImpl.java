package com.movie_booking.service;

import com.movie_booking.dao.ReportDao;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.ReportDaoImpl;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.dto.response.TheatreReportResponse;
import com.movie_booking.dto.response.MovieReportResponse;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportServiceImpl implements ReportService {

    private final ReportDao reportDao;
    private final UserDao userDao;

    public ReportServiceImpl() {
        this(new ReportDaoImpl(), new UserDaoImpl());
    }

    public ReportServiceImpl(ReportDao reportDao, UserDao userDao) {
        if (reportDao == null || userDao == null) {
            throw new IllegalArgumentException(
                    "Report and user DAOs cannot be null."
            );
        }

        this.reportDao = reportDao;
        this.userDao = userDao;
    }

    @Override
    public List<TheatreReportResponse> getTheatreReport(
            LocalDate date,
            int authenticatedUserId) throws SQLException {

        requireAdmin(authenticatedUserId);

        if (date == null) {
            throw new IllegalArgumentException(
                    "Report date is required."
            );
        }

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Report date cannot be in the future."
            );
        }

        return reportDao.getTheatreReport(date);
    }

    @Override
    public List<MovieReportResponse> getMovieReport(
            LocalDate date,
            int authenticatedUserId) throws SQLException {

        requireAdmin(authenticatedUserId);

        if (date == null) {
            throw new IllegalArgumentException(
                    "Report date is required."
            );
        }

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Report date cannot be in the future."
            );
        }

        return reportDao.getMovieReport(date);
    }

    private void requireAdmin(int authenticatedUserId)
            throws SQLException {

        if (authenticatedUserId <= 0) {
            throw new AuthenticationRequiredException();
        }

        User user = userDao.findById(authenticatedUserId);

        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationRequiredException();
        }

        if (user.getRole() != UserRole.ADMIN) {
            throw new AuthorizationException();
        }
    }

    
}

    