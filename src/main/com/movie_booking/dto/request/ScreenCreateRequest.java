package com.movie_booking.dto.request;

public class ScreenCreateRequest {
    private int theatreId;
    private String name;
    private int capacity;

    public ScreenCreateRequest() { }

    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
