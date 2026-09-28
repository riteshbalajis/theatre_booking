package com.movie_booking.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.movie_booking.dto.response.MovieReportResponse;
import com.movie_booking.dto.response.MovieTheatreReportResponse;
import com.movie_booking.dto.response.OverallReportResponse;
import com.movie_booking.dto.response.TheatreReportResponse;
import com.movie_booking.util.DBConnection;

public class ReportDaoImpl implements ReportDao {

    private static final String THEATRE_REPORT_SQL
            = "SELECT "
            + "    t.theatre_id, "
            + "    t.name AS theatre_name, "
            + "    COUNT(*) AS total_bookings, "
            + "    SUM(x.seats_sold) AS seats_sold, "
            + "    SUM(x.booking_amount) AS total_revenue "
            + "FROM ( "
            + "    SELECT "
            + "        b.booking_id, "
            + "        b.show_id, "
            + "        b.total_amount AS booking_amount, "
            + "        COUNT(bs.booking_seat_id) AS seats_sold "
            + "    FROM bookings b "
            + "    JOIN booking_seats bs "
            + "        ON b.booking_id = bs.booking_id "
            + "    WHERE b.status = 'CONFIRMED' "
            + "      AND DATE(b.booked_at) = ? "
            + "    GROUP BY "
            + "        b.booking_id, "
            + "        b.show_id, "
            + "        b.total_amount "
            + ") x "
            + "JOIN shows s "
            + "    ON x.show_id = s.show_id "
            + "JOIN screens sc "
            + "    ON s.screen_id = sc.screen_id "
            + "JOIN theatres t "
            + "    ON sc.theatre_id = t.theatre_id "
            + "GROUP BY "
            + "    t.theatre_id, "
            + "    t.name";

    @Override
    public List<TheatreReportResponse> getTheatreReport(LocalDate date)
            throws SQLException {

        List<TheatreReportResponse> reports = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement statement
                = connection.prepareStatement(THEATRE_REPORT_SQL)) {

            statement.setDate(
                    1,
                    java.sql.Date.valueOf(date)
            );

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    TheatreReportResponse report
                            = new TheatreReportResponse(
                                    resultSet.getInt("theatre_id"),
                                    resultSet.getString("theatre_name"),
                                    resultSet.getInt("total_bookings"),
                                    resultSet.getInt("seats_sold"),
                                    resultSet.getBigDecimal("total_revenue")
                            );

                    reports.add(report);
                }
            }
        }

        return reports;
    }

    @Override
    public List<MovieReportResponse> getMovieReport(LocalDate date) throws SQLException {

        String sql
                = "SELECT "
                + "    m.movie_id, "
                + "    m.title AS movie_title, "
                + "    COUNT(DISTINCT b.booking_id) AS total_bookings, "
                + "    COUNT(bs.booking_seat_id) AS seats_sold, "
                + "    SUM(bs.price) AS total_revenue "
                + "FROM bookings b "
                + "JOIN booking_seats bs ON b.booking_id = bs.booking_id "
                + "JOIN shows s ON b.show_id = s.show_id "
                + "JOIN movies m ON s.movie_id = m.movie_id "
                + "WHERE b.status = 'CONFIRMED' "
                + "AND DATE(b.booked_at) = ? "
                + "GROUP BY m.movie_id, m.title "
                + "ORDER BY total_revenue DESC";

        List<MovieReportResponse> reports = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    MovieReportResponse response
                            = new MovieReportResponse();

                    response.setMovieId(rs.getInt("movie_id"));
                    response.setMovieTitle(rs.getString("movie_title"));
                    response.setTotalBookings(
                            rs.getInt("total_bookings"));
                    response.setSeatsSold(
                            rs.getInt("seats_sold"));
                    response.setTotalRevenue(
                            rs.getBigDecimal("total_revenue"));

                    reports.add(response);
                }
            }
        }

        return reports;
    }

    @Override
    public OverallReportResponse getOverallReport(LocalDate date)
            throws SQLException {

        String sql
                = "SELECT "
                + "COUNT(DISTINCT b.booking_id) AS total_bookings, "
                + "COUNT(bs.booking_seat_id) AS seats_sold, "
                + "COALESCE(SUM(bs.price), 0) AS total_revenue "
                + "FROM bookings b "
                + "JOIN booking_seats bs ON b.booking_id = bs.booking_id "
                + "WHERE DATE(b.booked_at) = ? "
                + "AND b.status = 'CONFIRMED'";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    return new OverallReportResponse(
                            date,
                            rs.getInt("total_bookings"),
                            rs.getInt("seats_sold"),
                            rs.getBigDecimal("total_revenue")
                    );
                }
            }
        }

        return new OverallReportResponse(
                date,
                0,
                0,
                BigDecimal.ZERO
        );
    }

    @Override
    public List<MovieTheatreReportResponse> getMovieTheatreReport(LocalDate date) throws SQLException {


        String sql
        = "SELECT "
        + "m.movie_id, "
        + "m.title AS movie_title, "
        + "t.theatre_id, "
        + "t.name AS theatre_name, "
        + "COUNT(DISTINCT b.booking_id) AS total_bookings, "
        + "COUNT(bs.booking_seat_id) AS seats_sold, "
        + "COALESCE(SUM(bs.price), 0) AS total_revenue "
        + "FROM bookings b "
        + "JOIN booking_seats bs "
        + "ON b.booking_id = bs.booking_id "
        + "JOIN shows s "
        + "ON b.show_id = s.show_id "
        + "JOIN movies m "
        + "ON s.movie_id = m.movie_id "
        + "JOIN screens sc "
        + "ON s.screen_id = sc.screen_id "
        + "JOIN theatres t "
        + "ON sc.theatre_id = t.theatre_id "
        + "WHERE DATE(b.booked_at) = ? "
        + "AND b.status = 'CONFIRMED' "
        + "GROUP BY "
        + "m.movie_id, "
        + "m.title, "
        + "t.theatre_id, "
        + "t.name "
        + "ORDER BY m.title, t.name";

        List<MovieTheatreReportResponse> reports = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection(); 
              PreparedStatement statement= connection.prepareStatement(sql)) {

            statement.setDate(
                    1,
                    java.sql.Date.valueOf(date)
            );

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    MovieTheatreReportResponse response = new MovieTheatreReportResponse(
                                    rs.getInt("movie_id"),
                                    rs.getString("movie_title"),
                                    rs.getInt("seats_sold"),
                                    rs.getInt("theatre_id"),
                                    rs.getString("theatre_name"),
                                    rs.getInt("total_bookings"),
                                    rs.getBigDecimal("total_revenue"));

                    reports.add(response);
                }
            }
        }

        return reports;
    }
}
