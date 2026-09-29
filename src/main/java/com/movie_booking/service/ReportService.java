package com.movie_booking.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.movie_booking.dto.response.MovieReportResponse;
import com.movie_booking.dto.response.MovieTheatreReportResponse;
import com.movie_booking.dto.response.OverallReportResponse;
import com.movie_booking.dto.response.TheatreReportResponse;


public interface ReportService {

    List<TheatreReportResponse> getTheatreReport(LocalDate date, int userId)
            throws SQLException;

    List<MovieReportResponse> getMovieReport(LocalDate date,int authenticatedUserId) throws SQLException;

    OverallReportResponse getOverallReport(LocalDate date,int authenticatedUserId) throws SQLException;

    List<MovieTheatreReportResponse> getMovieTheatreReport(LocalDate date,int authenticatedUserId) throws SQLException;

    public void generateDailyReportPdf(LocalDate date,int authenticatedUserId,String filePath) throws Exception;

    public void generateDailyReportPdf(LocalDate date,String filePath) throws Exception;

 
}