package com.movie_booking.service;

import com.movie_booking.model.Booking;
import com.movie_booking.model.BookingSeat;
import com.movie_booking.model.BookingStatus;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface BookingService {
        int createBookingWithSeats(int authenticatedUserId, int showId,
            List<Integer> showSeatIds) throws SQLException;

        Booking getBookingById(int authenticatedUserId, int bookingId) throws SQLException;

        List<BookingSeat> getBookingSeats(int authenticatedUserId, int bookingId)
            throws SQLException;
    
    List<Booking> getBookingsForUser(int authenticatedUserId) throws SQLException;

    List<Booking> getBookingsByShowId(int showId) throws SQLException;

    List<Booking> getBookingsByUserAndShow(int userId, int showId) throws SQLException;

    List<Booking> getAllBookings() throws SQLException;

    List<Booking> getBookingsByStatus(BookingStatus status) throws SQLException;

    List<Booking> getUserBookingsByStatus(int userId, BookingStatus status) throws SQLException;

    boolean updateBookingStatus(int bookingId, BookingStatus status) throws SQLException;

    boolean confirmBooking(int bookingId) throws SQLException;

    boolean cancelBooking(int authenticatedUserId, int bookingId) throws SQLException;

    boolean completeBooking(int bookingId) throws SQLException;

    boolean updateTotalAmount(int bookingId, BigDecimal totalAmount) throws SQLException;

    boolean bookingExists(int bookingId) throws SQLException;

    int countBookingsByShowId(int showId) throws SQLException;
}
