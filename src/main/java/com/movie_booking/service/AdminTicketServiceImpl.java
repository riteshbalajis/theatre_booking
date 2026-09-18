package com.movie_booking.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.movie_booking.dao.BookingDao;
import com.movie_booking.dao.BookingDaoImpl;
import com.movie_booking.dao.BookingSeatDao;
import com.movie_booking.dao.BookingSeatDaoImpl;
import com.movie_booking.dao.MovieDao;
import com.movie_booking.dao.MovieDaoImpl;
import com.movie_booking.dao.ScreenDao;
import com.movie_booking.dao.ScreenDaoImpl;
import com.movie_booking.dao.SeatDao;
import com.movie_booking.dao.SeatDaoImpl;
import com.movie_booking.dao.ShowDao;
import com.movie_booking.dao.ShowDaoImpl;
import com.movie_booking.dao.ShowSeatDao;
import com.movie_booking.dao.ShowSeatDaoImpl;
import com.movie_booking.dao.TheatreDao;
import com.movie_booking.dao.TheatreDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.dto.response.TicketCheckInResponse;
import com.movie_booking.model.Booking;
import com.movie_booking.model.BookingSeat;
import com.movie_booking.model.BookingStatus;
import com.movie_booking.model.Movie;
import com.movie_booking.model.Screen;
import com.movie_booking.model.Seat;
import com.movie_booking.model.Show;
import com.movie_booking.model.ShowSeat;
import com.movie_booking.model.ShowStatus;
import com.movie_booking.model.Theatre;
import com.movie_booking.model.TicketStatus;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.util.DBConnection;

public class AdminTicketServiceImpl implements AdminTicketService {

    private static final int CHECK_IN_MINUTES_BEFORE_SHOW = 30;

    private final BookingDao bookingDao;
    private final ShowDao showDao;
    private final UserDao userDao;
    private final MovieDao movieDao;
    private final ScreenDao screenDao;
    private final TheatreDao theatreDao;
    private final BookingSeatDao bookingSeatDao;
    private final ShowSeatDao showSeatDao;
    private final SeatDao seatDao;

    public AdminTicketServiceImpl() {
        this(new BookingDaoImpl(), new ShowDaoImpl(), new UserDaoImpl(),
             new MovieDaoImpl(), new ScreenDaoImpl(), new TheatreDaoImpl(),
             new BookingSeatDaoImpl(), new ShowSeatDaoImpl(), new SeatDaoImpl());
    }

    public AdminTicketServiceImpl(
            BookingDao bookingDao,
            ShowDao showDao,
            UserDao userDao) {
        this(bookingDao, showDao, userDao,
             new MovieDaoImpl(), new ScreenDaoImpl(), new TheatreDaoImpl(),
             new BookingSeatDaoImpl(), new ShowSeatDaoImpl(), new SeatDaoImpl());
    }

    public AdminTicketServiceImpl(
            BookingDao bookingDao,
            ShowDao showDao,
            UserDao userDao,
            MovieDao movieDao,
            ScreenDao screenDao,
            TheatreDao theatreDao,
            BookingSeatDao bookingSeatDao,
            ShowSeatDao showSeatDao,
            SeatDao seatDao) {
        if (bookingDao == null || showDao == null || userDao == null) {
            throw new IllegalArgumentException(
                    "Admin ticket service dependencies cannot be null.");
        }
        this.bookingDao = bookingDao;
        this.showDao = showDao;
        this.userDao = userDao;
        this.movieDao = movieDao == null ? new MovieDaoImpl() : movieDao;
        this.screenDao = screenDao == null ? new ScreenDaoImpl() : screenDao;
        this.theatreDao = theatreDao == null ? new TheatreDaoImpl() : theatreDao;
        this.bookingSeatDao = bookingSeatDao == null ? new BookingSeatDaoImpl() : bookingSeatDao;
        this.showSeatDao = showSeatDao == null ? new ShowSeatDaoImpl() : showSeatDao;
        this.seatDao = seatDao == null ? new SeatDaoImpl() : seatDao;
    }

    @Override
    public TicketCheckInResponse checkInTicket(
            int adminUserId,
            String ticketCode
    ) throws SQLException {
        if (adminUserId <= 0) {
            return failure(null, "UNAUTHORIZED", "Administrator authentication is required.", null);
        }
        if (ticketCode == null || ticketCode.trim().isEmpty()) {
            return failure(null, "TICKET_CODE_REQUIRED", "Ticket code is required.", null);
        }

        User admin = userDao.findById(adminUserId);
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return failure(null, "FORBIDDEN", "Administrator access is required.", null);
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDao.findByTicketCode(
                        connection,
                        ticketCode.trim());

                if (booking == null) {
                    return finish(connection, failure(
                            connection,
                            "INVALID_TICKET",
                            "Ticket code is invalid.",
                            null));
                }

                if (booking.getStatus() != BookingStatus.CONFIRMED) {
                    return finish(connection, failure(
                            connection,
                            "BOOKING_NOT_CONFIRMED",
                            "This booking is not confirmed.",
                            booking));
                }

                if (booking.getTicketStatus() == TicketStatus.USED) {
                    return finish(connection, failure(
                            connection,
                            "TICKET_ALREADY_USED",
                            "This ticket has already been used.",
                            booking));
                }

                if (booking.getTicketStatus() == TicketStatus.CANCELLED) {
                    return finish(connection, failure(
                            connection,
                            "TICKET_CANCELLED",
                            "This ticket has been cancelled.",
                            booking));
                }

                if (booking.getTicketStatus() != TicketStatus.VALID) {
                    return finish(connection, failure(
                            connection,
                            "INVALID_TICKET",
                            "This ticket is not valid.",
                            booking));
                }

                Show show = showDao.findById(connection, booking.getShowId());
                TicketCheckInResponse timingFailure = validateShowWindow(connection, booking, show);
                if (timingFailure != null) {
                    return finish(connection, timingFailure);
                }

                boolean markedUsed = bookingDao.markTicketAsUsed(
                        connection,
                        ticketCode.trim());
                if (!markedUsed) {
                    return finish(connection, failure(
                            connection,
                            "TICKET_ALREADY_USED",
                            "This ticket has already been used.",
                            booking));
                }

                booking.setTicketStatus(TicketStatus.USED);
                return finish(connection, success(connection, booking));
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private TicketCheckInResponse validateShowWindow(
            Connection connection,
            Booking booking,
            Show show) {
        if (show == null || show.getShowDate() == null
                || show.getStartTime() == null || show.getEndTime() == null) {
            return failure(
                    connection,
                    "SHOW_DETAILS_UNAVAILABLE",
                    "Show timing details are unavailable.",
                    booking);
        }

        if (show.getStatus() == ShowStatus.CANCELLED) {
            return failure(
                    connection,
                    "SHOW_CANCELLED",
                    "This show has been cancelled.",
                    booking);
        }

        LocalDate today = LocalDate.now();
        LocalDate showDate = show.getShowDate();

        if (showDate.isAfter(today)) {
            return failure(
                    connection,
                    "FUTURE_SHOW",
                    "This is a future show. Check-in has not started.",
                    booking);
        }

        if (showDate.isBefore(today)) {
            return failure(
                    connection,
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
                    connection,
                    "CHECKIN_NOT_STARTED",
                    "Check-in starts 30 minutes before the show.",
                    booking);
        }

        if (now.isAfter(showEnd)) {
            return failure(
                    connection,
                    "TICKET_EXPIRED",
                    "This show has already ended. The ticket has expired.",
                    booking);
        }

        return null;
    }

    private TicketCheckInResponse success(Connection connection, Booking booking) {
        TicketCheckInResponse response = new TicketCheckInResponse(
                true,
                "TICKET_VERIFIED",
                "Ticket verified successfully. Customer may enter.",
                booking.getBookingId(),
                booking.getTicketCode(),
                booking.getTicketStatus());
        populateTicketDetails(connection, response, booking);
        return response;
    }

    private TicketCheckInResponse failure(
            Connection connection,
            String code,
            String message,
            Booking booking) {
        TicketCheckInResponse response = new TicketCheckInResponse(
                false,
                code,
                message,
                booking == null ? null : booking.getBookingId(),
                booking == null ? null : booking.getTicketCode(),
                booking == null ? null : booking.getTicketStatus());
        if (booking != null && connection != null) {
            populateTicketDetails(connection, response, booking);
        }
        return response;
    }

    private void populateTicketDetails(
            Connection connection,
            TicketCheckInResponse response,
            Booking booking) {
        if (booking == null || response == null) {
            return;
        }

        response.setBookingReference(booking.getBookingReference());
        response.setTotalAmount(booking.getTotalAmount());
        if (booking.getBookedAt() != null) {
            response.setBookedAt(booking.getBookedAt().toString());
        }

        try {
            User customer = userDao.findById(booking.getUserId());
            if (customer != null) {
                response.setCustomerName(customer.getName());
                response.setCustomerEmail(customer.getEmail());
            }

            Show show = showDao.findById(connection, booking.getShowId());
            if (show != null) {
                if (show.getShowDate() != null) {
                    response.setShowDate(show.getShowDate().toString());
                }
                if (show.getStartTime() != null) {
                    response.setStartTime(show.getStartTime().toString());
                }
                if (show.getEndTime() != null) {
                    response.setEndTime(show.getEndTime().toString());
                }

                Movie movie = movieDao.findById(show.getMovieId());
                if (movie != null) {
                    response.setMovieTitle(movie.getTitle());
                }

                Screen screen = screenDao.findById(show.getScreenId());
                if (screen != null) {
                    response.setScreenName(screen.getName());
                    Theatre theatre = theatreDao.findById(screen.getTheatreId());
                    if (theatre != null) {
                        response.setTheatreName(theatre.getName());
                    }
                }
            }

            List<BookingSeat> bookingSeats = bookingSeatDao.findByBookingId(connection, booking.getBookingId());
            List<String> seatLabels = new ArrayList<>();
            if (bookingSeats != null) {
                for (BookingSeat bs : bookingSeats) {
                    ShowSeat ss = showSeatDao.findById(connection, bs.getShowSeatId());
                    if (ss != null) {
                        Seat seat = seatDao.findById(ss.getSeatId());
                        if (seat != null) {
                            String row = seat.getRowLabel() == null ? "" : seat.getRowLabel().trim();
                            seatLabels.add(row + seat.getSeatNumber());
                        }
                    }
                }
            }
            if (!seatLabels.isEmpty()) {
                response.setSeats(String.join(", ", seatLabels));
            }
        } catch (Exception ignored) {
            // Best effort enrichment
        }
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
