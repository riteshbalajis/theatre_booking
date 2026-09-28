package com.movie_booking.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.movie_booking.dto.response.TheatreReportResponse;
import com.movie_booking.dto.response.MovieReportResponse;

public interface ReportDao {

    List<TheatreReportResponse> getTheatreReport(LocalDate date) throws SQLException;

    List<MovieReportResponse> getMovieReport(LocalDate date) throws SQLException;
}