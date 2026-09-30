package com.movie_booking.service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.movie_booking.dao.TheatreDao;
import com.movie_booking.dao.TheatreDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.model.Theatre;
import com.movie_booking.model.TheatreStatus;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;

public class TheatreServiceImpl implements TheatreService {
    private final TheatreDao theatreDao;
    private final UserDao userDao;
    private static final String ALL_THEATRES_CACHE_KEY = "all";
    private static final Cache<Integer, Theatre> theatreCache
        = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();
    private static final Cache<String, List<Theatre>> theatreListCache
        = Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    public TheatreServiceImpl() {
        this(new TheatreDaoImpl(), new UserDaoImpl());
    }

    public TheatreServiceImpl(TheatreDao theatreDao) {
        this(theatreDao, new UserDaoImpl());
    }

    public TheatreServiceImpl(TheatreDao theatreDao, UserDao userDao) {
        if (theatreDao == null || userDao == null) {
            throw new IllegalArgumentException("Theatre and user DAOs cannot be null.");
        }
        this.theatreDao = theatreDao;
        this.userDao = userDao;
    }

    @Override
    public int addTheatre(Theatre theatre, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateTheatre(theatre);
        if (theatreDao.findByName(theatre.getName().trim()) != null) {
            throw new IllegalArgumentException("Theatre name already exists.");
        }
        theatre.setName(theatre.getName().trim());
        theatre.setLocation(theatre.getLocation().trim());
        if (theatre.getStatus() == null) {
            theatre.setStatus(TheatreStatus.ACTIVE);
        }
        int theatreId = theatreDao.createTheatre(theatre);
        invalidateTheatreCaches(theatreId);
        return theatreId;
    }

    @Override
    public Theatre getTheatreById(int theatreId) throws SQLException {
        requirePositiveId(theatreId);

        Theatre cachedTheatre = theatreCache.getIfPresent(theatreId);
        if (cachedTheatre != null) {
            System.out.println("Theatre cache hit for ID: " + theatreId);
            return cachedTheatre;
        }

        System.out.println("Theatre cache miss for ID: " + theatreId);
        Theatre theatre = theatreDao.findById(theatreId);
        if (theatre != null) {
            theatreCache.put(theatreId, theatre);
        }
        return theatre;
    }

    @Override
    public Theatre getTheatreByName(String name) throws SQLException {
        requireText(name, "Name");
        return theatreDao.findByName(name.trim());
    }

    @Override
    public List<Theatre> getAllTheatres() throws SQLException {
        List<Theatre> cachedTheatres = theatreListCache.getIfPresent(ALL_THEATRES_CACHE_KEY);
        if (cachedTheatres != null) {
            System.out.println("Theatre list cache hit");
            return cachedTheatres;
        }

        System.out.println("Theatre list cache miss");
        List<Theatre> theatres = theatreDao.findAll();
        if (theatres != null) {
            List<Theatre> snapshot = List.copyOf(theatres);
            theatreListCache.put(ALL_THEATRES_CACHE_KEY, snapshot);
            return snapshot;
        }
        return null;
    }

    @Override
    public List<Theatre> getActiveTheatres() throws SQLException {
        return theatreDao.findAllActive();
    }

    @Override
    public List<Theatre> searchTheatres(String keyword) throws SQLException {
        requireText(keyword, "Search keyword");
        return theatreDao.searchByName(keyword.trim());
    }

    @Override
    public List<Theatre> getTheatresByLocation(String location) throws SQLException {
        requireText(location, "Location");
        return theatreDao.findByLocation(location.trim());
    }

    @Override
    public boolean updateTheatre(Theatre theatre, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateTheatreForUpdate(theatre);
        Theatre existing = theatreDao.findByName(theatre.getName().trim());
        if (existing != null && existing.getTheatreId() != theatre.getTheatreId()) {
            throw new IllegalArgumentException("Theatre name already exists.");
        }
        theatre.setName(theatre.getName().trim());
        theatre.setLocation(theatre.getLocation().trim());
        boolean updated = theatreDao.updateTheatre(theatre);
        if (updated) {
            invalidateTheatreCaches(theatre.getTheatreId());
        }
        return updated;
    }

    @Override
    public boolean activateTheatre(int theatreId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(theatreId);
        boolean activated = theatreDao.activateTheatre(theatreId);
        if (activated) {
            invalidateTheatreCaches(theatreId);
        }
        return activated;
    }

    @Override
    public boolean deactivateTheatre(int theatreId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(theatreId);
        boolean deactivated = theatreDao.deactivateTheatre(theatreId);
        if (deactivated) {
            invalidateTheatreCaches(theatreId);
        }
        return deactivated;
    }

    @Override
    public boolean theatreExists(int theatreId) throws SQLException {
        requirePositiveId(theatreId);
        return theatreDao.existsById(theatreId);
    }

    private static void invalidateTheatreCaches(int theatreId) {
        theatreCache.invalidate(theatreId);
        theatreListCache.invalidate(ALL_THEATRES_CACHE_KEY);
    }

    private static void validateTheatre(Theatre theatre) {
        if (theatre == null) {
            throw new IllegalArgumentException("Theatre cannot be null.");
        }
        requireText(theatre.getName(), "Name");
        requireText(theatre.getLocation(), "Location");
    }

    private static void validateTheatreForUpdate(Theatre theatre) {
        if (theatre == null || theatre.getTheatreId() <= 0) {
            throw new IllegalArgumentException("A valid theatre is required.");
        }
        validateTheatre(theatre);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    private static void requirePositiveId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Theatre ID must be positive.");
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
