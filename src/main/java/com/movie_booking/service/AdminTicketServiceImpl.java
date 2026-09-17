package com.movie_booking.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.movie_booking.dao.BookingDao;
import com.movie_booking.dao.BookingDaoImpl;
import com.movie_booking.dao.ShowDao;
import com.movie_booking.dao.ShowDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.dto.response.TicketCheckInResponse;
import com.movie_booking.model.Booking;
import com.movie_booking.model.BookingStatus;
import com.movie_booking.model.Show;
import com.movie_booking.model.ShowStatus;
import com.movie_booking.model.TicketStatus;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.util.DBConnection;

public class AdminTicketServiceImpl implements AdminTicketService {

    private static final int CHECK_IN_MINUTES_BEFORE_SHOW = 30;

    private final BookingDao bookingDao;
    private final ShowDao showDao;
    private final UserDao userDao;

    public AdminTicketServiceImpl() {
        this(new BookingDaoImpl(), new ShowDaoImpl(), new UserDaoImpl());
    }

    public AdminTicketServiceImpl(
            BookingDao bookingDao,
            ShowDao showDao,
            UserDao userDao) {
        if (bookingDao == null || showDao == null || userDao == null) {
            throw new IllegalArgumentException(
                    "Admin ticket service dependencies cannot be null.");
        }
        this.bookingDao = bookingDao;
        this.showDao = showDao;
        this.userDao = userDao;
    }

    @Override
    public TicketCheckInResponse checkInTicket(
            int adminUserId,
            String ticketCode
    ) throws SQLException {
        if (adminUserId <= 0) {
            return failure("UNAUTHORIZED", "Administrator authentication is required.", null);
        }
        if (ticketCode == null || ticketCode.trim().isEmpty()) {
            return failure("TICKET_CODE_REQUIRED", "Ticket code is required.", null);
        }

        User admin = userDao.findById(adminUserId);
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return failure("FORBIDDEN", "Administrator access is required.", null);
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDao.findByTicketCode(
                        connection,
                        ticketCode.trim());

                if (booking == null) {
                    return finish(connection, failure(
                            "INVALID_TICKET",
                            "Ticket code is invalid.",
                            null));
                }

                if (booking.getStatus() != BookingStatus.CONFIRMED) {
                    return finish(connection, failure(
                            "BOOKING_NOT_CONFIRMED",
                            "This booking is not confirmed.",
                            booking));
                }

                if (booking.getTicketStatus() == TicketStatus.USED) {
                    return finish(connection, failure(
                            "TICKET_ALREADY_USED",
                            "This ticket has already been used.",
                            booking));
                }

                if (booking.getTicketStatus() == TicketStatus.CANCELLED) {
                    return finish(connection, failure(
                            "TICKET_CANCELLED",
                            "This ticket has been cancelled.",
                            booking));
                }

                if (booking.getTicketStatus() != TicketStatus.VALID) {
                    return finish(connection, failure(
                            "INVALID_TICKET",
                            "This ticket is not valid.",
                            booking));
                }

                Show show = showDao.findById(connection, booking.getShowId());
                TicketCheckInResponse timingFailure = validateShowWindow(booking, show);
                if (timingFailure != null) {
                    return finish(connection, timingFailure);
                }

                boolean markedUsed = bookingDao.markTicketAsUsed(
                        connection,
                        ticketCode.trim());
                if (!markedUsed) {
                    return finish(connection, failure(
                            "TICKET_ALREADY_USED",
                            "This ticket has already been used.",
                            booking));
                }

                booking.setTicketStatus(TicketStatus.USED);
                return finish(connection, success(booking));
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private TicketCheckInResponse validateShowWindow(
            Booking booking,
            Show show) {
        if (show == null || show.getShowDate() == null
                || show.getStartTime() == null || show.getEndTime() == null) {
            return failure(
                    "SHOW_DETAILS_UNAVAILABLE",
                    "Show timing details are unavailable.",
                    booking);
        }

        if (show.getStatus() == ShowStatus.CANCELLED) {
            return failure(
                    "SHOW_CANCELLED",
                    "This show has been cancelled.",
                    booking);
        }

        LocalDate today = LocalDate.now();
        LocalDate showDate = show.getShowDate();

        if (showDate.isAfter(today)) {
            return failure(
                    "FUTURE_SHOW",
                    "This is a future show. Check-in has not started.",
                    booking);
        }

        if (showDate.isBefore(today)) {
            return failure(
                    "TICKET_EXPIRED",
                    "This ticket has expired.",
                    booking);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkInStart = LocalDateTime.of(
                showDate,
                show.getStartTime()).minusMinutes(CHECK_IN_MINUTES_BEFORE_SHOW);
        LocalDateTime showEnd = LocalDateTime.of(
                showDate,
                show.getEndTime());

        if (now.isBefore(checkInStart)) {
            return failure(
                    "CHECKIN_NOT_STARTED",
                    "Check-in starts 30 minutes before the show.",
                    booking);
        }

        if (now.isAfter(showEnd)) {
            return failure(
                    "TICKET_EXPIRED",
                    "This show has already ended. The ticket has expired.",
                    booking);
        }

        return null;
    }

    private TicketCheckInResponse success(Booking booking) {
        return new TicketCheckInResponse(
                true,
                "TICKET_VERIFIED",
                "Ticket verified successfully. Customer may enter.",
                booking.getBookingId(),
                booking.getTicketCode(),
                booking.getTicketStatus());
    }

    private TicketCheckInResponse failure(
            String code,
            String message,
            Booking booking) {
        return new TicketCheckInResponse(
                false,
                code,
                message,
                booking == null ? null : booking.getBookingId(),
                booking == null ? null : booking.getTicketCode(),
                booking == null ? null : booking.getTicketStatus());
    }

    private TicketCheckInResponse finish(
            Connection connection,
            TicketCheckInResponse response) throws SQLException {
        connection.commit();
        return response;
    }

    private void rollback(Connection connection, Exception exception) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            exception.addSuppressed(rollbackException);
        }
    }
}
