package com.movie_booking.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.movie_booking.dto.response.MovieReportResponse;
import com.movie_booking.dto.response.MovieTheatreReportResponse;
import com.movie_booking.dto.response.OverallReportResponse;
import com.movie_booking.dto.response.TheatreReportResponse;
public interface ReportDao {

    List<TheatreReportResponse> getTheatreReport(LocalDate date) throws SQLException;

    List<MovieReportResponse> getMovieReport(LocalDate date) throws SQLException;

    OverallReportResponse getOverallReport(LocalDate date) throws SQLException;

    List<MovieTheatreReportResponse> getMovieTheatreReport(LocalDate date) throws SQLException;
}