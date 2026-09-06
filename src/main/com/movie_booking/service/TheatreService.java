package com.movie_booking.service;

import com.movie_booking.model.Theatre;
import java.sql.SQLException;
import java.util.List;

public interface TheatreService {
    int addTheatre(Theatre theatre, int authenticatedUserId) throws SQLException;

    Theatre getTheatreById(int theatreId) throws SQLException;

    Theatre getTheatreByName(String name) throws SQLException;

    List<Theatre> getAllTheatres() throws SQLException;

    List<Theatre> getActiveTheatres() throws SQLException;

    List<Theatre> searchTheatres(String keyword) throws SQLException;

    List<Theatre> getTheatresByLocation(String location) throws SQLException;

    boolean updateTheatre(Theatre theatre, int authenticatedUserId) throws SQLException;

    boolean activateTheatre(int theatreId, int authenticatedUserId) throws SQLException;

    boolean deactivateTheatre(int theatreId, int authenticatedUserId) throws SQLException;

    boolean theatreExists(int theatreId) throws SQLException;
}
