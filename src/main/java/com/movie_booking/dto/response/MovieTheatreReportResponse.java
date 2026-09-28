package com.movie_booking.dto.response;

import java.math.BigDecimal;

public class MovieTheatreReportResponse {

    private int movieId;
    private String movieTitle;
    private int theatreId;
    private String theatreName;
    private int totalBookings;
    private int seatsSold;
    private BigDecimal totalRevenue;


    public MovieTheatreReportResponse(int movieId, String movieTitle, int seatsSold, int theatreId, String theatreName, int totalBookings, BigDecimal totalRevenue) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.seatsSold = seatsSold;
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.totalBookings = totalBookings;
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

    public int getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(int theatreId) {
        this.theatreId = theatreId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
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
