package com.movie_booking.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.movie_booking.model.Booking;
import com.movie_booking.model.BookingStatus;
import com.movie_booking.model.TicketStatus;

public interface BookingDao {
    int createBooking(Booking booking) throws SQLException;

    int createBooking(Connection connection, Booking booking) throws SQLException;

    Booking findById(int bookingId) throws SQLException;

    Booking findById(Connection connection, int bookingId) throws SQLException;

    List<Booking> findByUserId(int userId) throws SQLException;

    List<Booking> findByShowId(int showId) throws SQLException;

    List<Booking> findByUserAndShow(int userId, int showId) throws SQLException;

    List<Booking> findAll() throws SQLException;

    List<Booking> findByStatus(BookingStatus status) throws SQLException;

    List<Booking> findUserBookingsByStatus(int userId, BookingStatus status) throws SQLException;

    boolean updateStatus(int bookingId, BookingStatus status) throws SQLException;

        boolean updateStatus(Connection connection, int bookingId, BookingStatus status)
            throws SQLException;

    boolean confirmBooking(int bookingId) throws SQLException;

    boolean cancelBooking(int bookingId) throws SQLException;

    boolean completeBooking(int bookingId) throws SQLException;

    boolean updateTotalAmount(int bookingId, BigDecimal totalAmount) throws SQLException;

    boolean existsById(int bookingId) throws SQLException;

    int countByShowId(int showId) throws SQLException;

    boolean confirmBooking(Connection connection, int bookingId) throws SQLException;

        boolean assignTicketDetails(
            Connection connection,
            int bookingId,
            String ticketCode,
            TicketStatus ticketStatus
        ) throws SQLException;

            Booking findByTicketCode(String ticketCode) throws SQLException;

            Booking findByTicketCode(Connection connection, String ticketCode)
                throws SQLException;

            boolean markTicketAsUsed(Connection connection, String ticketCode)
                throws SQLException;

    int cancelExpiredBookings(Connection connection) throws SQLException;

}
