package com.movie_booking.service;

import com.movie_booking.model.Show;
import com.movie_booking.model.ShowStatus;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ShowService {
    int addShow(Show show, int authenticatedUserId) throws SQLException;

    Show getShowById(int showId) throws SQLException;

    List<Show> getAllShows() throws SQLException;

    List<Show> getShowsByMovieId(int movieId) throws SQLException;

    List<Show> getShowsByScreenId(int screenId) throws SQLException;

    List<Show> getShowsByDate(LocalDate showDate) throws SQLException;

    List<Show> getShowsByMovieAndDate(int movieId, LocalDate showDate) throws SQLException;

    List<Show> getShowsByScreenAndDate(int screenId, LocalDate showDate) throws SQLException;

    List<Show> getShowsByTheatreAndDate(int theatreId, LocalDate showDate) throws SQLException;

    List<Show> getUpcomingShows() throws SQLException;

    List<Show> getActiveShowsByMovieId(int movieId) throws SQLException;

    boolean updateShow(Show show, int authenticatedUserId) throws SQLException;

        boolean updateShowStatus(int showId, ShowStatus status, int authenticatedUserId)
            throws SQLException;

    boolean cancelShow(int showId, int authenticatedUserId) throws SQLException;

    boolean completeShow(int showId, int authenticatedUserId) throws SQLException;

    boolean showExists(int showId) throws SQLException;
}
