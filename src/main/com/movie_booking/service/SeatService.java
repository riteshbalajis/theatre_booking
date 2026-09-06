package com.movie_booking.service;

import com.movie_booking.model.Seat;
import java.sql.SQLException;
import java.util.List;

public interface SeatService {
    int addSeat(Seat seat, int authenticatedUserId) throws SQLException;

    Seat getSeatById(int seatId) throws SQLException;

    List<Seat> getSeatsByScreenId(int screenId) throws SQLException;

    List<Seat> getActiveSeatsByScreenId(int screenId) throws SQLException;

    List<Seat> getSeatsByRow(int screenId, String rowLabel) throws SQLException;

    Seat getSeatByPosition(int screenId, String rowLabel, int seatNumber) throws SQLException;

    List<Seat> getAllSeats() throws SQLException;

    boolean updateSeat(Seat seat, int authenticatedUserId) throws SQLException;

    boolean activateSeat(int seatId, int authenticatedUserId) throws SQLException;

    boolean deactivateSeat(int seatId, int authenticatedUserId) throws SQLException;

    boolean seatExists(int seatId) throws SQLException;

    boolean seatPositionExists(int screenId, String rowLabel, int seatNumber) throws SQLException;
}
