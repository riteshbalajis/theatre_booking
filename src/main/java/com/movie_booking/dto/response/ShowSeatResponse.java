package com.movie_booking.dto.response;

import com.movie_booking.model.ShowSeatStatus;
import java.math.BigDecimal;

public class ShowSeatResponse {
    private int showSeatId;
    private int showId;
    private int seatId;
    private String rowLabel;
    private int seatNumber;
    private String seatType;
    private ShowSeatStatus status;
    private BigDecimal price;

    public ShowSeatResponse() { }

    public int getShowSeatId() { return showSeatId; }
    public void setShowSeatId(int showSeatId) { this.showSeatId = showSeatId; }
    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
    public ShowSeatStatus getStatus() { return status; }
    public void setStatus(ShowSeatStatus status) { this.status = status; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
