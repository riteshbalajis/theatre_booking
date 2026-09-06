package com.movie_booking.dto.request;

import com.movie_booking.model.SeatType;

public class SeatCreateRequest {
    private int screenId;
    private String rowLabel;
    private int seatNumber;
    private SeatType seatType;

    public SeatCreateRequest() { }

    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
}
