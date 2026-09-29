package com.movie_booking.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.movie_booking.dao.ReportDao;
import com.movie_booking.dao.ReportDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.dto.response.MovieReportResponse;
import com.movie_booking.dto.response.MovieTheatreReportResponse;
import com.movie_booking.dto.response.OverallReportResponse;
import com.movie_booking.dto.response.TheatreReportResponse;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import com.movie_booking.util.PdfReportGenerator;


public class ReportServiceImpl implements ReportService {

    private final ReportDao ReportDao;
    private final UserDao userDao;

    public ReportServiceImpl() {
        this(new ReportDaoImpl(), new UserDaoImpl());
    }

    public ReportServiceImpl(ReportDao ReportDao, UserDao userDao) {
        if (ReportDao == null || userDao == null) {
            throw new IllegalArgumentException(
                    "Report and user DAOs cannot be null."
            );
        }

        this.ReportDao = ReportDao;
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

        return ReportDao.getTheatreReport(date);
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

        return ReportDao.getMovieReport(date);
    }

    @Override
    public OverallReportResponse getOverallReport(
            LocalDate date,
            int authenticatedUserId)
            throws SQLException {

        requireAdmin(authenticatedUserId);

        if (date == null) {
            throw new IllegalArgumentException("Report date cannot be null.");
        }

        return ReportDao.getOverallReport(date);
    }

    @Override
    public List<MovieTheatreReportResponse> getMovieTheatreReport(
            LocalDate date,
            int authenticatedUserId) throws SQLException {

        requireAdmin(authenticatedUserId);

        if (date == null) {
            throw new IllegalArgumentException(
                    "Report date cannot be null."
            );
        }

        return ReportDao.getMovieTheatreReport(date);
    }

    @Override
    public void generateDailyReportPdf(
            LocalDate date,
            int authenticatedUserId,
            String filePath) throws Exception {

        // Authorization
        requireAdmin(authenticatedUserId);

        if (date == null) {
            throw new IllegalArgumentException(
                    "Report date cannot be null."
            );
        }
        generateDailyReportPdf(date, filePath);
    }

    @Override
    public void generateDailyReportPdf(LocalDate date,String filePath) throws Exception {

        //requireAdmin(authenticatedUserId);

        System.out.println("Generating report for: " + date);

        OverallReportResponse overallReport
                = ReportDao.getOverallReport(date);

        List<MovieReportResponse> movieReports
                = ReportDao.getMovieReport(date);

        List<TheatreReportResponse> theatreReports
                = ReportDao.getTheatreReport(date);

        List<MovieTheatreReportResponse> movieTheatreReports
                = ReportDao.getMovieTheatreReport(date);


        PdfReportGenerator.generateDailyReportPdf(
                filePath,
                date,
                overallReport,
                movieReports,
                theatreReports,
                movieTheatreReports
        );
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
