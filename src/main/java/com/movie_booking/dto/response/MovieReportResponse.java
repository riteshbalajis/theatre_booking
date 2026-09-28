package com.movie_booking.dto.response;
import java.math.BigDecimal;


public class MovieReportResponse {

    private int movieId;
    private String movieTitle;
    private int totalBookings;
    private int seatsSold;
    private BigDecimal totalRevenue;

    public MovieReportResponse() {
    }

    public MovieReportResponse(
            int movieId,
            String movieTitle,
            int totalBookings,
            int seatsSold,
            BigDecimal totalRevenue) {

        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.totalBookings = totalBookings;
        this.seatsSold = seatsSold;
        this.totalRevenue = totalRevenue;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getSeatsSold() {
        return seatsSold;
    }

    public void setSeatsSold(int seatsSold) {
        this.seatsSold = seatsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}