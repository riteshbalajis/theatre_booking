package com.movie_booking.dto.response;

import com.movie_booking.model.SeatStatus;
import com.movie_booking.model.SeatType;

public class SeatResponse {
    private int seatId;
    private int screenId;
    private String rowLabel;
    private int seatNumber;
    private SeatType seatType;
    private SeatStatus status;

    public SeatResponse() { }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
}
