package com.movie_booking.dto.response;

import com.movie_booking.model.ShowStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ShowResponse {
    private int showId;
    private int movieId;
    private int screenId;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ShowStatus status;
    private LocalDateTime createdAt;

    public ShowResponse() { }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public int getScreenId() { return screenId; }
    public void setScreenId(int screenId) { this.screenId = screenId; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public ShowStatus getStatus() { return status; }
    public void setStatus(ShowStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
