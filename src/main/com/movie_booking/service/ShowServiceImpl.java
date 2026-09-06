package com.movie_booking.service;

import com.movie_booking.dao.ShowDao;
import com.movie_booking.dao.ShowDaoImpl;
import com.movie_booking.dao.ShowSeatDao;
import com.movie_booking.dao.ShowSeatDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.model.Show;
import com.movie_booking.model.ShowStatus;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import com.movie_booking.util.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ShowServiceImpl implements ShowService {
    private final ShowDao showDao;
    private final ShowSeatDao showSeatDao;
    private final UserDao userDao;

    public ShowServiceImpl() {
        this(new ShowDaoImpl(), new ShowSeatDaoImpl(), new UserDaoImpl());
    }

    public ShowServiceImpl(ShowDao showDao) {
        this(showDao, new ShowSeatDaoImpl(), new UserDaoImpl());
    }

    public ShowServiceImpl(ShowDao showDao, ShowSeatDao showSeatDao) {
        this(showDao, showSeatDao, new UserDaoImpl());
    }

    public ShowServiceImpl(ShowDao showDao, ShowSeatDao showSeatDao, UserDao userDao) {
        if (showDao == null || showSeatDao == null || userDao == null) {
            throw new IllegalArgumentException("Show and user DAOs cannot be null.");
        }
        this.showDao = showDao;
        this.showSeatDao = showSeatDao;
        this.userDao = userDao;
    }

    @Override
    public int addShow(Show show, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateShow(show);
        validateNotPast(show);
        ensureNoOverlap(show, 0);
        validateSeatPrices(show);
        if (show.getStatus() == null) {
            show.setStatus(ShowStatus.SCHEDULED);
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int showId = showDao.createShow(connection, show);
                showSeatDao.createShowSeatsForShow(connection, showId, show.getScreenId(),
                        show.getRegularPrice(), show.getPremiumPrice(), show.getReclinerPrice());
                connection.commit();
                return showId;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public Show getShowById(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return showDao.findById(showId);
    }

    @Override
    public List<Show> getAllShows() throws SQLException {
        return showDao.findAll();
    }

    @Override
    public List<Show> getShowsByMovieId(int movieId) throws SQLException {
        requirePositiveId(movieId, "Movie ID");
        return showDao.findShowsByMovieId(movieId);
    }

    @Override
    public List<Show> getShowsByScreenId(int screenId) throws SQLException {
        requirePositiveId(screenId, "Screen ID");
        return showDao.findShowsByScreenId(screenId);
    }

    @Override
    public List<Show> getShowsByDate(LocalDate showDate) throws SQLException {
        requireDate(showDate);
        return showDao.findShowsByDate(showDate);
    }

    @Override
    public List<Show> getShowsByMovieAndDate(int movieId, LocalDate showDate)
            throws SQLException {
        requirePositiveId(movieId, "Movie ID");
        requireDate(showDate);
        return showDao.findShowsByMovieAndDate(movieId, showDate);
    }

    @Override
    public List<Show> getShowsByScreenAndDate(int screenId, LocalDate showDate)
            throws SQLException {
        requirePositiveId(screenId, "Screen ID");
        requireDate(showDate);
        return showDao.findShowsByScreenAndDate(screenId, showDate);
    }

    @Override
    public List<Show> getShowsByTheatreAndDate(int theatreId, LocalDate showDate)
            throws SQLException {
        requirePositiveId(theatreId, "Theatre ID");
        requireDate(showDate);
        return showDao.findShowsByTheatreAndDate(theatreId, showDate);
    }

    @Override
    public List<Show> getUpcomingShows() throws SQLException {
        return showDao.findUpcomingShows();
    }

    @Override
    public List<Show> getActiveShowsByMovieId(int movieId) throws SQLException {
        requirePositiveId(movieId, "Movie ID");
        return showDao.findActiveShowsByMovieId(movieId);
    }

    @Override
    public boolean updateShow(Show show, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateShowForUpdate(show);
        validateNotPast(show);
        ensureNoOverlap(show, show.getShowId());
        return showDao.updateShow(show);
    }

    @Override
    public boolean updateShowStatus(int showId, ShowStatus status, int authenticatedUserId)
            throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(showId, "Show ID");
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        return showDao.updateShowStatus(showId, status);
    }

    @Override
    public boolean cancelShow(int showId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(showId, "Show ID");
        return showDao.cancelShow(showId);
    }

    @Override
    public boolean completeShow(int showId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(showId, "Show ID");
        return showDao.completeShow(showId);
    }

    @Override
    public boolean showExists(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return showDao.existsById(showId);
    }

    private void ensureNoOverlap(Show candidate, int excludedShowId) throws SQLException {
        List<Show> existingShows = showDao.findShowsByScreenAndDate(
                candidate.getScreenId(), candidate.getShowDate());
        for (Show existing : existingShows) {
            if (existing.getShowId() != excludedShowId
                    && existing.getStatus() != ShowStatus.CANCELLED
                    && overlaps(candidate, existing)) {
                throw new IllegalArgumentException("Show time overlaps another show on this screen.");
            }
        }
    }

    private static boolean overlaps(Show first, Show second) {
        return first.getStartTime().isBefore(second.getEndTime())
                && second.getStartTime().isBefore(first.getEndTime());
    }

    private static void validateShow(Show show) {
        if (show == null) {
            throw new IllegalArgumentException("Show cannot be null.");
        }
        requirePositiveId(show.getMovieId(), "Movie ID");
        requirePositiveId(show.getScreenId(), "Screen ID");
        requireDate(show.getShowDate());
        if (show.getStartTime() == null || show.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required.");
        }
        if (!show.getEndTime().isAfter(show.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
    }

    private static void validateShowForUpdate(Show show) {
        if (show == null || show.getShowId() <= 0) {
            throw new IllegalArgumentException("A valid show is required.");
        }
        validateShow(show);
    }

    private static void validateSeatPrices(Show show) {
        validatePrice(show.getRegularPrice(), "Regular price");
        validatePrice(show.getPremiumPrice(), "Premium price");
        validatePrice(show.getReclinerPrice(), "Recliner price");
    }

    private static void validatePrice(BigDecimal price, String fieldName) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be null or negative.");
        }
    }

    private static void validateNotPast(Show show) {
        if (show.getShowDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Show date cannot be in the past.");
        }
    }

    private static void requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Show date cannot be null.");
        }
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

    private static void rollback(Connection connection, Exception originalException)
            throws SQLException {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
