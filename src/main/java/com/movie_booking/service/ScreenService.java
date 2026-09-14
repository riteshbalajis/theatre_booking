package com.movie_booking.service;

import com.movie_booking.model.Screen;
import java.sql.SQLException;
import java.util.List;

public interface ScreenService {
    int addScreen(Screen screen, int authenticatedUserId) throws SQLException;

    Screen getScreenById(int screenId) throws SQLException;

    Screen getScreenByName(int theatreId, String name) throws SQLException;

    List<Screen> getAllScreens() throws SQLException;

    List<Screen> getScreensByTheatreId(int theatreId) throws SQLException;

    List<Screen> getActiveScreensByTheatreId(int theatreId) throws SQLException;

    boolean updateScreen(Screen screen, int authenticatedUserId) throws SQLException;

    boolean activateScreen(int screenId, int authenticatedUserId) throws SQLException;

    boolean deactivateScreen(int screenId, int authenticatedUserId) throws SQLException;

    boolean screenExists(int screenId) throws SQLException;
}
