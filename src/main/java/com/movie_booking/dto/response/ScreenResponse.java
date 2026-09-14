package com.movie_booking.dto.response;

import com.movie_booking.model.ScreenStatus;

public class ScreenResponse {
    private int screenId;
    private int theatreId;
    private String name;
    private int capacity;
    private ScreenStatus status;

    public ScreenResponse() { }

    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }
    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public ScreenStatus getStatus() { return status; }
    public void setStatus(ScreenStatus status) { this.status = status; }
}
