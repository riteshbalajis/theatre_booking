package com.movie_booking.service;

import com.movie_booking.model.ShowSeat;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface ShowSeatService {
    int addShowSeat(ShowSeat showSeat) throws SQLException;

    int createShowSeatsForShow(int showId, int screenId, BigDecimal regularPrice,
            BigDecimal premiumPrice, BigDecimal reclinerPrice) throws SQLException;

    ShowSeat getShowSeatById(int showSeatId) throws SQLException;

    List<ShowSeat> getShowSeatsByShowId(int showId) throws SQLException;

    List<ShowSeat> getAvailableShowSeatsByShowId(int showId) throws SQLException;

    List<ShowSeat> getBookedShowSeatsByShowId(int showId) throws SQLException;

    ShowSeat getShowSeatByShowAndSeat(int showId, int seatId) throws SQLException;

    int getAvailableSeatCountByShowId(int showId) throws SQLException;

    boolean updateShowSeatPrice(int showSeatId, BigDecimal price) throws SQLException;

    boolean bookShowSeat(int showSeatId) throws SQLException;

    boolean releaseShowSeat(int showSeatId) throws SQLException;

    boolean isShowSeatAvailable(int showSeatId) throws SQLException;

    boolean showSeatExists(int showSeatId) throws SQLException;

    boolean showSeatCombinationExists(int showId, int seatId) throws SQLException;
}
