package com.movie_booking.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ShowCreateRequest {
    private int movieId;
    private int screenId;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal regularPrice;
    private BigDecimal premiumPrice;
    private BigDecimal reclinerPrice;

    public ShowCreateRequest() { }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public BigDecimal getRegularPrice() { return regularPrice; }
    public void setRegularPrice(BigDecimal regularPrice) { this.regularPrice = regularPrice; }
    public BigDecimal getPremiumPrice() { return premiumPrice; }
    public void setPremiumPrice(BigDecimal premiumPrice) { this.premiumPrice = premiumPrice; }
    public BigDecimal getReclinerPrice() { return reclinerPrice; }
    public void setReclinerPrice(BigDecimal reclinerPrice) { this.reclinerPrice = reclinerPrice; }
}
