package com.movie_booking.service;

import com.movie_booking.dao.ScreenDao;
import com.movie_booking.dao.ScreenDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.model.Screen;
import com.movie_booking.model.ScreenStatus;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import java.sql.SQLException;
import java.util.List;

public class ScreenServiceImpl implements ScreenService {
    private final ScreenDao screenDao;
    private final UserDao userDao;

    public ScreenServiceImpl() {
        this(new ScreenDaoImpl(), new UserDaoImpl());
    }

    public ScreenServiceImpl(ScreenDao screenDao) {
        this(screenDao, new UserDaoImpl());
    }

    public ScreenServiceImpl(ScreenDao screenDao, UserDao userDao) {
        if (screenDao == null || userDao == null) {
            throw new IllegalArgumentException("Screen and user DAOs cannot be null.");
        }
        this.screenDao = screenDao;
        this.userDao = userDao;
    }

    @Override
    public int addScreen(Screen screen, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateScreen(screen);
        if (screenDao.existsByName(screen.getTheatreId(), screen.getName().trim())) {
            throw new IllegalArgumentException("Screen name already exists in this theatre.");
        }
        screen.setName(screen.getName().trim());
        if (screen.getStatus() == null) {
            screen.setStatus(ScreenStatus.ACTIVE);
        }
        return screenDao.createScreen(screen);
    }

    @Override
    public Screen getScreenById(int screenId) throws SQLException {
        requirePositiveId(screenId);
        return screenDao.findById(screenId);
    }

    @Override
    public Screen getScreenByName(int theatreId, String name) throws SQLException {
        requirePositiveId(theatreId, "Theatre ID");
        requireText(name, "Name");
        return screenDao.findByName(theatreId, name.trim());
    }

    @Override
    public List<Screen> getAllScreens() throws SQLException {
        return screenDao.findAll();
    }

    @Override
    public List<Screen> getScreensByTheatreId(int theatreId) throws SQLException {
        requirePositiveId(theatreId, "Theatre ID");
        return screenDao.findScreensByTheatreId(theatreId);
    }

    @Override
    public List<Screen> getActiveScreensByTheatreId(int theatreId) throws SQLException {
        requirePositiveId(theatreId, "Theatre ID");
        return screenDao.findActiveScreensByTheatreId(theatreId);
    }

    @Override
    public boolean updateScreen(Screen screen, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateScreenForUpdate(screen);
        Screen existing = screenDao.findByName(screen.getTheatreId(), screen.getName().trim());
        if (existing != null && existing.getScreenId() != screen.getScreenId()) {
            throw new IllegalArgumentException("Screen name already exists in this theatre.");
        }
        screen.setName(screen.getName().trim());
        return screenDao.updateScreen(screen);
    }

    @Override
    public boolean activateScreen(int screenId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(screenId);
        return screenDao.activateScreen(screenId);
    }

    @Override
    public boolean deactivateScreen(int screenId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(screenId);
        return screenDao.deactivateScreen(screenId);
    }

    @Override
    public boolean screenExists(int screenId) throws SQLException {
        requirePositiveId(screenId);
        return screenDao.existsById(screenId);
    }

    private static void validateScreen(Screen screen) {
        if (screen == null) {
            throw new IllegalArgumentException("Screen cannot be null.");
        }
        requirePositiveId(screen.getTheatreId(), "Theatre ID");
        requireText(screen.getName(), "Name");
        validateCapacity(screen.getCapacity());
    }

    private static void validateScreenForUpdate(Screen screen) {
        if (screen == null || screen.getScreenId() <= 0) {
            throw new IllegalArgumentException("A valid screen is required.");
        }
        validateScreen(screen);
    }

    private static void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    private static void requirePositiveId(int id) {
        requirePositiveId(id, "ID");
    }

    private static void requirePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }

    private void requireAdmin(int authenticatedUserId) throws SQLException {
        if (authenticatedUserId <= 0) {
            throw new AuthenticationRequiredException();
        }
        User user = userDao.findById(authenticatedUserId);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationRequiredException();
        }
        if (user.getRole() != UserRole.ADMIN) {
            throw new AuthorizationException();
        }
    }
}
