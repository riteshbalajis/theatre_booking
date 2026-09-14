package com.movie_booking.service;

import com.movie_booking.dao.SeatDao;
import com.movie_booking.dao.SeatDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.model.Seat;
import com.movie_booking.model.SeatStatus;
import com.movie_booking.model.SeatType;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import java.sql.SQLException;
import java.util.List;

public class SeatServiceImpl implements SeatService {
    private final SeatDao seatDao;
    private final UserDao userDao;

    public SeatServiceImpl() {
        this(new SeatDaoImpl(), new UserDaoImpl());
    }

    public SeatServiceImpl(SeatDao seatDao) {
        this(seatDao, new UserDaoImpl());
    }

    public SeatServiceImpl(SeatDao seatDao, UserDao userDao) {
        if (seatDao == null || userDao == null) {
            throw new IllegalArgumentException("Seat and user DAOs cannot be null.");
        }
        this.seatDao = seatDao;
        this.userDao = userDao;
    }

    @Override
    public int addSeat(Seat seat, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateSeat(seat);
        if (seatDao.existsByPosition(seat.getScreenId(), seat.getRowLabel().trim(),
                seat.getSeatNumber())) {
            throw new IllegalArgumentException("Seat position already exists on this screen.");
        }
        seat.setRowLabel(seat.getRowLabel().trim().toUpperCase());
        if (seat.getSeatType() == null) {
            seat.setSeatType(SeatType.REGULAR);
        }
        if (seat.getStatus() == null) {
            seat.setStatus(SeatStatus.ACTIVE);
        }
        return seatDao.createSeat(seat);
    }

    @Override
    public Seat getSeatById(int seatId) throws SQLException {
        requirePositiveId(seatId, "Seat ID");
        return seatDao.findById(seatId);
    }

    @Override
    public List<Seat> getSeatsByScreenId(int screenId) throws SQLException {
        requirePositiveId(screenId, "Screen ID");
        return seatDao.findByScreenId(screenId);
    }

    @Override
    public List<Seat> getActiveSeatsByScreenId(int screenId) throws SQLException {
        requirePositiveId(screenId, "Screen ID");
        return seatDao.findActiveSeatsByScreenId(screenId);
    }

    @Override
    public List<Seat> getSeatsByRow(int screenId, String rowLabel) throws SQLException {
        requirePositiveId(screenId, "Screen ID");
        requireText(rowLabel, "Row label");
        return seatDao.findByRow(screenId, rowLabel.trim().toUpperCase());
    }

    @Override
    public Seat getSeatByPosition(int screenId, String rowLabel, int seatNumber)
            throws SQLException {
        requirePositiveId(screenId, "Screen ID");
        validatePosition(rowLabel, seatNumber);
        return seatDao.findByPosition(screenId, rowLabel.trim().toUpperCase(), seatNumber);
    }

    @Override
    public List<Seat> getAllSeats() throws SQLException {
        return seatDao.findAll();
    }

    @Override
    public boolean updateSeat(Seat seat, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateSeatForUpdate(seat);
        String rowLabel = seat.getRowLabel().trim().toUpperCase();
        Seat existing = seatDao.findByPosition(seat.getScreenId(), rowLabel, seat.getSeatNumber());
        if (existing != null && existing.getSeatId() != seat.getSeatId()) {
            throw new IllegalArgumentException("Seat position already exists on this screen.");
        }
        seat.setRowLabel(rowLabel);
        if (seat.getSeatType() == null) {
            throw new IllegalArgumentException("Seat type cannot be null.");
        }
        return seatDao.updateSeat(seat);
    }

    @Override
    public boolean activateSeat(int seatId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(seatId, "Seat ID");
        return seatDao.activateSeat(seatId);
    }

    @Override
    public boolean deactivateSeat(int seatId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(seatId, "Seat ID");
        return seatDao.deactivateSeat(seatId);
    }

    @Override
    public boolean seatExists(int seatId) throws SQLException {
        requirePositiveId(seatId, "Seat ID");
        return seatDao.existsById(seatId);
    }

    @Override
    public boolean seatPositionExists(int screenId, String rowLabel, int seatNumber)
            throws SQLException {
        requirePositiveId(screenId, "Screen ID");
        validatePosition(rowLabel, seatNumber);
        return seatDao.existsByPosition(screenId, rowLabel.trim().toUpperCase(), seatNumber);
    }

    private static void validateSeat(Seat seat) {
        if (seat == null) {
            throw new IllegalArgumentException("Seat cannot be null.");
        }
        requirePositiveId(seat.getScreenId(), "Screen ID");
        validatePosition(seat.getRowLabel(), seat.getSeatNumber());
    }

    private static void validateSeatForUpdate(Seat seat) {
        if (seat == null || seat.getSeatId() <= 0) {
            throw new IllegalArgumentException("A valid seat is required.");
        }
        validateSeat(seat);
    }

    private static void validatePosition(String rowLabel, int seatNumber) {
        requireText(rowLabel, "Row label");
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("Seat number must be positive.");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
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
}
