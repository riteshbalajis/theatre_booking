package com.movie_booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OverallReportResponse {

    private LocalDate reportDate;
    private int totalBookings;
    private int seatsSold;
    private BigDecimal totalRevenue;

    public OverallReportResponse(
            LocalDate reportDate,
            int totalBookings,
            int seatsSold,
            BigDecimal totalRevenue) {

        this.reportDate = reportDate;
        this.totalBookings = totalBookings;
        this.seatsSold = seatsSold;
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public int getSeatsSold() {
        return seatsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}