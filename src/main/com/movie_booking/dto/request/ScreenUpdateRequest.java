package com.movie_booking.dto.request;

public class ScreenUpdateRequest {
    private int screenId;
    private String name;
    private int capacity;

    public ScreenUpdateRequest() { }

    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
