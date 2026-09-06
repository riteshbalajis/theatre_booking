package com.movie_booking.dto.response;

import com.movie_booking.model.TheatreStatus;
import java.time.LocalDateTime;

public class TheatreResponse {
    private int theatreId;
    private String name;
    private String location;
    private TheatreStatus status;
    private LocalDateTime createdAt;

    public TheatreResponse() { }

    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public TheatreStatus getStatus() { return status; }
    public void setStatus(TheatreStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
