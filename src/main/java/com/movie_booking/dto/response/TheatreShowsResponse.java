package com.movie_booking.dto.response;

import java.util.ArrayList;
import java.util.List;

public class TheatreShowsResponse {
    private int theatreId;
    private String theatreName;
    private String theatreLocation;
    private List<ShowSlotResponse> shows = new ArrayList<>();

    public TheatreShowsResponse() {
    }

    public TheatreShowsResponse(int theatreId, String theatreName, String theatreLocation) {
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.theatreLocation = theatreLocation;
        this.shows = new ArrayList<>();
    }

    public int getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(int theatreId) {
        this.theatreId = theatreId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getTheatreLocation() {
        return theatreLocation;
    }

    public void setLocation(String theatreLocation) {
        this.theatreLocation = theatreLocation;
    }

    public void setTheatreLocation(String theatreLocation) {
        this.theatreLocation = theatreLocation;
    }

    public List<ShowSlotResponse> getShows() {
        return shows;
    }

    public void setShows(List<ShowSlotResponse> shows) {
        this.shows = shows != null ? shows : new ArrayList<>();
    }

    public void addShow(ShowSlotResponse show) {
        if (this.shows == null) {
            this.shows = new ArrayList<>();
        }
        this.shows.add(show);
    }
}
