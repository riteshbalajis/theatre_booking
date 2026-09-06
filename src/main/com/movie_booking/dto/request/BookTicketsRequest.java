package com.movie_booking.dto.request;

import java.util.List;

public class BookTicketsRequest {
    private int userId;
    private int showId;
    private List<Integer> showSeatIds;

    public BookTicketsRequest() { }

    public BookTicketsRequest(int userId, int showId, List<Integer> showSeatIds) {
        this.userId = userId;
        this.showId = showId;
        this.showSeatIds = showSeatIds;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public List<Integer> getShowSeatIds() { return showSeatIds; }
    public void setShowSeatIds(List<Integer> showSeatIds) { this.showSeatIds = showSeatIds; }
}
