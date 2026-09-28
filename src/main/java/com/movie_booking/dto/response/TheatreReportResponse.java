package com.movie_booking.dto.response;

import java.math.BigDecimal;

public class TheatreReportResponse {

    private int theatreId;
    private String theatreName;
    private int totalBookings;
    private int seatsSold;
    private BigDecimal totalRevenue;

 

    public TheatreReportResponse(
            int theatreId,
            String theatreName,
            int totalBookings,
            int seatsSold,
            BigDecimal totalRevenue) {

        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.totalBookings = totalBookings;
        this.seatsSold = seatsSold;
        this.totalRevenue = totalRevenue;
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