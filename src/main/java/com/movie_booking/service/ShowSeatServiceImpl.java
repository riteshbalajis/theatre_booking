package com.movie_booking.service;

import com.movie_booking.dao.ShowSeatDao;
import com.movie_booking.dao.ShowSeatDaoImpl;
import com.movie_booking.model.ShowSeat;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ShowSeatServiceImpl implements ShowSeatService {
    private final ShowSeatDao showSeatDao;

    public ShowSeatServiceImpl() {
        this(new ShowSeatDaoImpl());
    }

    public ShowSeatServiceImpl(ShowSeatDao showSeatDao) {
        if (showSeatDao == null) {
            throw new IllegalArgumentException("Show seat DAO cannot be null.");
        }
        this.showSeatDao = showSeatDao;
    }

    @Override
    public int addShowSeat(ShowSeat showSeat) throws SQLException {
        validateShowSeat(showSeat);
        if (showSeatDao.existsByShowAndSeat(showSeat.getShowId(), showSeat.getSeatId())) {
            throw new IllegalArgumentException("Show seat combination already exists.");
        }
        return showSeatDao.createShowSeat(showSeat);
    }

    @Override
    public int createShowSeatsForShow(int showId, int screenId, BigDecimal regularPrice,
            BigDecimal premiumPrice, BigDecimal reclinerPrice) throws SQLException {
        requirePositiveId(showId, "Show ID");
        requirePositiveId(screenId, "Screen ID");
        validatePrice(regularPrice, "Regular price");
        validatePrice(premiumPrice, "Premium price");
        validatePrice(reclinerPrice, "Recliner price");
        return showSeatDao.createShowSeatsForShow(showId, screenId, regularPrice,
                premiumPrice, reclinerPrice);
    }

    @Override
    public ShowSeat getShowSeatById(int showSeatId) throws SQLException {
        requirePositiveId(showSeatId, "Show seat ID");
        return showSeatDao.findById(showSeatId);
    }

    @Override
    public List<ShowSeat> getShowSeatsByShowId(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return showSeatDao.findByShowId(showId);
    }

    @Override
    public List<ShowSeat> getAvailableShowSeatsByShowId(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return showSeatDao.findAvailableByShowId(showId);
    }

    @Override
    public List<ShowSeat> getBookedShowSeatsByShowId(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return showSeatDao.findBookedByShowId(showId);
    }

    @Override
    public ShowSeat getShowSeatByShowAndSeat(int showId, int seatId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        requirePositiveId(seatId, "Seat ID");
        return showSeatDao.findByShowAndSeat(showId, seatId);
    }

    @Override
    public int getAvailableSeatCountByShowId(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return showSeatDao.countAvailableByShowId(showId);
    }

    @Override
    public boolean updateShowSeatPrice(int showSeatId, BigDecimal price) throws SQLException {
        requirePositiveId(showSeatId, "Show seat ID");
        validatePrice(price, "Price");
        return showSeatDao.updatePrice(showSeatId, price);
    }

    @Override
    public boolean bookShowSeat(int showSeatId) throws SQLException {
        requirePositiveId(showSeatId, "Show seat ID");
        return showSeatDao.bookSeat(showSeatId);
    }

    @Override
    public boolean releaseShowSeat(int showSeatId) throws SQLException {
        requirePositiveId(showSeatId, "Show seat ID");
        return showSeatDao.releaseSeat(showSeatId);
    }

    @Override
    public boolean isShowSeatAvailable(int showSeatId) throws SQLException {
        requirePositiveId(showSeatId, "Show seat ID");
        return showSeatDao.isAvailable(showSeatId);
    }

    @Override
    public boolean showSeatExists(int showSeatId) throws SQLException {
        requirePositiveId(showSeatId, "Show seat ID");
        return showSeatDao.existsById(showSeatId);
    }

    @Override
    public boolean showSeatCombinationExists(int showId, int seatId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        requirePositiveId(seatId, "Seat ID");
        return showSeatDao.existsByShowAndSeat(showId, seatId);
    }

    private static void validateShowSeat(ShowSeat showSeat) {
        if (showSeat == null) {
            throw new IllegalArgumentException("Show seat cannot be null.");
        }
        requirePositiveId(showSeat.getShowId(), "Show ID");
        requirePositiveId(showSeat.getSeatId(), "Seat ID");
        if (showSeat.getStatus() == null) {
            throw new IllegalArgumentException("Show seat status cannot be null.");
        }
        validatePrice(showSeat.getPrice(), "Price");
    }

    private static void validatePrice(BigDecimal price, String fieldName) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be null or negative.");
        }
    }

    private static void requirePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }
}
