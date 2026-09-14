package com.movie_booking.dto.request;

public class TheatreUpdateRequest {
    private int theatreId;
    private String name;
    private String location;

    public TheatreUpdateRequest() { }

    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
