package com.movie_booking.dto.response;

public class ShowSeatCountResponse {
    private int showId;
    private int availableSeatCount;

    public ShowSeatCountResponse() { }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public int getAvailableSeatCount() { return availableSeatCount; }
    public void setAvailableSeatCount(int availableSeatCount) { this.availableSeatCount = availableSeatCount; }
   
}
