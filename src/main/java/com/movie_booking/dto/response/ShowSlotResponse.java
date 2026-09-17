package com.movie_booking.dto.response;

import com.movie_booking.model.ShowStatus;
import java.math.BigDecimal;
import java.time.LocalTime;

public class ShowSlotResponse {
    private int showId;
    private int screenId;
    private String screenName;
    private LocalTime startTime;
    private LocalTime endTime;
    private ShowStatus status;
    private BigDecimal regularPrice;
    private BigDecimal premiumPrice;
    private BigDecimal reclinerPrice;

    public ShowSlotResponse() {
    }

    public ShowSlotResponse(int showId, int screenId, String screenName, LocalTime startTime,
            LocalTime endTime, ShowStatus status) {
        this.showId = showId;
        this.screenId = screenId;
        this.screenName = screenName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public int getScreenId() {
        return screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ShowStatus getStatus() {
        return status;
    }

    public void setStatus(ShowStatus status) {
        this.status = status;
    }

    public BigDecimal getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(BigDecimal regularPrice) {
        this.regularPrice = regularPrice;
    }

    public BigDecimal getPremiumPrice() {
        return premiumPrice;
    }

    public void setPremiumPrice(BigDecimal premiumPrice) {
        this.premiumPrice = premiumPrice;
    }

    public BigDecimal getReclinerPrice() {
        return reclinerPrice;
    }

    public void setReclinerPrice(BigDecimal reclinerPrice) {
        this.reclinerPrice = reclinerPrice;
    }
}
