package com.movie_booking.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.movie_booking.dto.request.RazorpayPaymentRequest;
import com.movie_booking.dto.response.RazorpayOrderResponse;
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
import com.movie_booking.model.User;
import com.movie_booking.util.DBConnection;
import com.movie_booking.util.RazorpayConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

public class BookingServiceImpl implements BookingService {
    private final BookingDao bookingDao;
    private final BookingSeatDao bookingSeatDao;
    private final ShowDao showDao;
    private final ShowSeatDao showSeatDao;
    private final EmailService emailService;
    private final UserDao userDao;
    private final MovieDao movieDao;
    private final ScreenDao screenDao;
    private final TheatreDao theatreDao;
    private final SeatDao seatDao;

    public BookingServiceImpl() {
        this(new BookingDaoImpl(), new BookingSeatDaoImpl(), new ShowDaoImpl(),
                new ShowSeatDaoImpl(), new EmailService(), new UserDaoImpl(),
                new MovieDaoImpl(), new ScreenDaoImpl(), new TheatreDaoImpl(),
                new SeatDaoImpl());
    }

    public BookingServiceImpl(BookingDao bookingDao, BookingSeatDao bookingSeatDao,
            ShowSeatDao showSeatDao) {
        this(bookingDao, bookingSeatDao, new ShowDaoImpl(), showSeatDao);
    }

    public BookingServiceImpl(BookingDao bookingDao, BookingSeatDao bookingSeatDao,
            ShowDao showDao, ShowSeatDao showSeatDao) {
        this(bookingDao, bookingSeatDao, showDao, showSeatDao, new EmailService(),
            new UserDaoImpl(), new MovieDaoImpl(), new ScreenDaoImpl(),
            new TheatreDaoImpl(), new SeatDaoImpl());
        }

        public BookingServiceImpl(BookingDao bookingDao, BookingSeatDao bookingSeatDao,
            ShowDao showDao, ShowSeatDao showSeatDao, EmailService emailService,
            UserDao userDao, MovieDao movieDao, ScreenDao screenDao,
            TheatreDao theatreDao, SeatDao seatDao) {
        if (bookingDao == null || bookingSeatDao == null || showDao == null
            || showSeatDao == null || emailService == null || userDao == null
            || movieDao == null || screenDao == null || theatreDao == null
            || seatDao == null) {
            throw new IllegalArgumentException("Booking DAOs cannot be null.");
        }
        this.bookingDao = bookingDao;
        this.bookingSeatDao = bookingSeatDao;
        this.showDao = showDao;
        this.showSeatDao = showSeatDao;
        this.emailService = emailService;
        this.userDao = userDao;
        this.movieDao = movieDao;
        this.screenDao = screenDao;
        this.theatreDao = theatreDao;
        this.seatDao = seatDao;
    }

    @Override
    public List<BookingSeat> getBookingSeats(int authenticatedUserId, int bookingId)
            throws SQLException {
        requireAuthenticatedUser(authenticatedUserId);
        requirePositiveId(bookingId, "Booking ID");
        requireBookingOwner(authenticatedUserId, bookingDao.findById(bookingId));
        return bookingSeatDao.findByBookingId(bookingId);
    }

    @Override
    public int createBookingWithSeats(int authenticatedUserId, int showId,
            List<Integer> showSeatIds)
            throws SQLException {
        requireAuthenticatedUser(authenticatedUserId);
        requirePositiveId(showId, "Show ID");
        validateSeatIds(showSeatIds);

        

        List<Integer> orderedSeatIds = new ArrayList<>(showSeatIds);
        Collections.sort(orderedSeatIds);
        Set<Integer> uniqueSeatIds = new HashSet<>(orderedSeatIds);
        if (uniqueSeatIds.size() != orderedSeatIds.size()) {
            throw new IllegalArgumentException("The same show seat cannot be selected twice.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                validateBookableShow(showDao.findById(connection, showId));
                BigDecimal totalAmount = BigDecimal.ZERO;
                List<BookingSeat> bookingSeats = new ArrayList<>();

                LocalDateTime holdUntil = LocalDateTime.now().plusMinutes(2);
                for (int showSeatId : orderedSeatIds) {
                    ShowSeat showSeat = showSeatDao.findById(connection, showSeatId);
                    if (showSeat == null || showSeat.getShowId() != showId
                            || showSeat.getStatus() == null) {
                        throw new IllegalArgumentException("Invalid seat selected for this show.");
                    }

                    boolean held = showSeatDao.holdSeat(connection,showSeatId,authenticatedUserId,holdUntil);
                    if (!held) {
                        throw new IllegalStateException(
                                "Seat " + showSeatId + " is no longer available"
                        );
                    }
                    
                    ShowSeat reservedSeat = showSeatDao.findById(connection, showSeatId);
                    if (reservedSeat == null || reservedSeat.getPrice() == null) {
                        throw new SQLException("Reserved show seat price could not be retrieved.");
                    }
                    totalAmount = totalAmount.add(reservedSeat.getPrice());
                    bookingSeats.add(new BookingSeat(0, 0, showSeatId,
                            reservedSeat.getPrice()));
                }

                Booking booking = new Booking();
                booking.setUserId(authenticatedUserId);
                booking.setShowId(showId);
                booking.setTotalAmount(totalAmount);
                booking.setStatus(BookingStatus.PENDING);
                booking.setHoldUntil(holdUntil);
                int bookingId = bookingDao.createBooking(connection, booking);
                for (BookingSeat bookingSeat : bookingSeats) {
                    bookingSeat.setBookingId(bookingId);
                }

                int insertedCount = bookingSeatDao.createBookingSeats(connection, bookingId,
                        bookingSeats);
                if (insertedCount != bookingSeats.size()) {
                    throw new SQLException("Not all booking seats were inserted.");
                }

                connection.commit();
                return bookingId;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }

    }

    private void sendTicketConfirmationEmailSafely(int bookingId) {
        try {
            Booking booking = bookingDao.findById(bookingId);
            User user = userDao.findById(booking.getUserId());
            Show show = showDao.findById(booking.getShowId());
            Movie movie = movieDao.findById(show.getMovieId());
            Screen screen = screenDao.findById(show.getScreenId());
            Theatre theatre = theatreDao.findById(screen.getTheatreId());
            List<BookingSeat> bookingSeats = bookingSeatDao.findByBookingId(bookingId);

            StringBuilder seatNames = new StringBuilder();
            for (int index = 0; index < bookingSeats.size(); index++) {
                ShowSeat showSeat = showSeatDao.findById(bookingSeats.get(index).getShowSeatId());
                Seat seat = seatDao.findById(showSeat.getSeatId());
                if (index > 0) {
                    seatNames.append(", ");
                }
                seatNames.append(seat.getRowLabel()).append(seat.getSeatNumber());
            }

            emailService.sendTicketConfirmationEmail(
                    user.getEmail(),
                    user.getName(),
                    booking.getBookingId(),
                    movie.getTitle(),
                    theatre.getName(),
                    screen.getName(),
                    show.getShowDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    show.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                    seatNames.toString(),
                    bookingSeats.size(),
                    booking.getTotalAmount().doubleValue());
        } catch (SQLException | RuntimeException exception) {
            System.err.println("Booking confirmed, but ticket email could not be sent for booking "
                    + bookingId + ": " + exception.getMessage());
        }
    }

    @Override
    public Booking getBookingById(int authenticatedUserId, int bookingId) throws SQLException {
        requireAuthenticatedUser(authenticatedUserId);
        requirePositiveId(bookingId, "Booking ID");
        Booking booking = bookingDao.findById(bookingId);
        requireBookingOwner(authenticatedUserId, booking);
        return booking;
    }

    @Override
    public List<Booking> getBookingsForUser(int authenticatedUserId) throws SQLException {
        requireAuthenticatedUser(authenticatedUserId);
        return bookingDao.findByUserId(authenticatedUserId);
    }

    @Override
    public List<Booking> getBookingsByShowId(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return bookingDao.findByShowId(showId);
    }

    @Override
    public List<Booking> getBookingsByUserAndShow(int userId, int showId)
            throws SQLException {
        requirePositiveId(userId, "User ID");
        requirePositiveId(showId, "Show ID");
        return bookingDao.findByUserAndShow(userId, showId);
    }

    @Override
    public List<Booking> getAllBookings() throws SQLException {
        return bookingDao.findAll();
    }

    @Override
    public List<Booking> getBookingsByStatus(BookingStatus status) throws SQLException {
        requireStatus(status);
        return bookingDao.findByStatus(status);
    }

    @Override
    public List<Booking> getUserBookingsByStatus(int userId, BookingStatus status)
            throws SQLException {
        requirePositiveId(userId, "User ID");
        requireStatus(status);
        return bookingDao.findUserBookingsByStatus(userId, status);
    }

    @Override
    public boolean updateBookingStatus(int bookingId, BookingStatus status) throws SQLException {
        requirePositiveId(bookingId, "Booking ID");
        requireStatus(status);
        validateStatusTransition(bookingDao.findById(bookingId), status);
        return bookingDao.updateStatus(bookingId, status);
    }

    @Override
    public boolean confirmBooking(int bookingId) throws SQLException {
        requirePositiveId(bookingId, "Booking ID");
        validateStatusTransition(bookingDao.findById(bookingId), BookingStatus.CONFIRMED);
        return bookingDao.confirmBooking(bookingId);
    }

    @Override
    public boolean cancelBooking(int authenticatedUserId, int bookingId) throws SQLException {
        requireAuthenticatedUser(authenticatedUserId);
        requirePositiveId(bookingId, "Booking ID");
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingDao.findById(connection, bookingId);
                requireBookingOwner(authenticatedUserId, booking);
                validateStatusTransition(booking, BookingStatus.CANCELLED);
                List<BookingSeat> bookingSeats = bookingSeatDao.findByBookingId(connection, bookingId);
                if (!bookingDao.updateStatus(connection, bookingId, BookingStatus.CANCELLED)) {
                    throw new SQLException("Booking could not be cancelled.");
                }
                for (BookingSeat bookingSeat : bookingSeats) {

                    boolean released;
                    if (booking.getStatus() == BookingStatus.PENDING) {
                        released = showSeatDao.releaseHeldSeat(connection,bookingSeat.getShowSeatId(),authenticatedUserId);
                    } else {
                        released = showSeatDao.releaseSeat(connection,bookingSeat.getShowSeatId());
                    }
                    if (!released) {
                        throw new SQLException("One or more booking seats could not be released.");
                    }
                }
                
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void confirmPayment(int authenticatedUserId, int bookingId, String pin) throws SQLException {
        requireAuthenticatedUser(authenticatedUserId);
        requirePositiveId(bookingId, "Booking ID");

        if (!"1234".equals(pin)) {
            throw new IllegalArgumentException("Invalid payment PIN.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Booking booking = bookingDao.findById(connection, bookingId);

                if (booking == null) {
                    throw new IllegalArgumentException("Booking not found.");
                }

                if (booking.getUserId() != authenticatedUserId) {
                    throw new IllegalArgumentException("You cannot access this booking.");
                }

                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new IllegalStateException("Booking is not pending payment.");
                }
                if (booking.getHoldUntil() == null || !booking.getHoldUntil().isAfter(LocalDateTime.now())) {
                    throw new IllegalStateException("Payment time has expired.");
                }

                List<BookingSeat> bookingSeats = bookingSeatDao.findByBookingId(connection, bookingId);

                for (BookingSeat bookingSeat : bookingSeats) {
                    if (!showSeatDao.confirmHeldSeat(connection, bookingSeat.getShowSeatId(), authenticatedUserId)) {
                        throw new IllegalStateException("One or more seats are no longer available.");
                    }
                }

                boolean confirmed = bookingDao.confirmBooking(connection, bookingId);

                if (!confirmed) {
                    throw new IllegalStateException("Booking could not be confirmed.");
                }

               

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public RazorpayOrderResponse createRazorpayOrder(
            int authenticatedUserId, int bookingId) throws SQLException {

        requireAuthenticatedUser(authenticatedUserId);
        requirePositiveId(bookingId, "Booking ID");

        Booking booking = bookingDao.findById(bookingId);

        if (booking == null) {
            throw new IllegalArgumentException("Booking not found.");
        }

        if (booking.getUserId() != authenticatedUserId) {
            throw new IllegalArgumentException("You cannot access this booking.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking is not pending payment.");
        }

        if (booking.getHoldUntil() == null
                || !booking.getHoldUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Payment time has expired.");
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(
                    RazorpayConfig.getKeyId(),
                    RazorpayConfig.getKeySecret());

            long amountInPaise = booking.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            org.json.JSONObject orderRequest = new org.json.JSONObject();

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + bookingId);

            Order order = razorpayClient.orders.create(orderRequest);

            return new RazorpayOrderResponse(
                    bookingId,
                    order.get("id"),
                    RazorpayConfig.getKeyId(),
                    booking.getTotalAmount(),
                    "INR");

        } catch (Exception exception) {
            throw new SQLException(
                    "Unable to create Razorpay order.",
                    exception);
        }
    }

    @Override
    public void verifyRazorpayPayment(int authenticatedUserId, int bookingId, RazorpayPaymentRequest request)
            throws SQLException {

        requireAuthenticatedUser(authenticatedUserId);
        requirePositiveId(bookingId, "Booking ID");

        if (request == null) {
            throw new IllegalArgumentException(
                    "Payment verification data is required.");
        }

        if (request.getRazorpayPaymentId() == null
                || request.getRazorpayPaymentId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Razorpay payment ID is required.");
        }

        if (request.getRazorpayOrderId() == null
                || request.getRazorpayOrderId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Razorpay order ID is required.");
        }

        if (request.getRazorpaySignature() == null
                || request.getRazorpaySignature().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Razorpay signature is required.");
        }

        Booking booking = bookingDao.findById(bookingId);

        if (booking == null) {
            throw new IllegalArgumentException("Booking not found.");
        }

        if (booking.getUserId() != authenticatedUserId) {
            throw new IllegalArgumentException(
                    "You cannot access this booking.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking is not pending payment.");
        }

        try {
            String payload
                    = request.getRazorpayOrderId()
                    + "|"
                    + request.getRazorpayPaymentId();

            Utils.verifySignature(
                    payload,
                    request.getRazorpaySignature(),
                    RazorpayConfig.getKeySecret());

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid Razorpay payment signature.",
                    exception);
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Booking currentBooking = bookingDao.findById(connection, bookingId);

                if (currentBooking == null) {
                    throw new IllegalArgumentException("Booking not found.");
                }

                if (currentBooking.getUserId() != authenticatedUserId) {
                    throw new IllegalArgumentException(
                            "You cannot access this booking.");
                }

                if (currentBooking.getStatus() != BookingStatus.PENDING) {
                    throw new IllegalStateException(
                            "Booking is not pending payment.");
                }

                if (currentBooking.getHoldUntil() == null
                        || !currentBooking.getHoldUntil().isAfter(LocalDateTime.now())) {
                    throw new IllegalStateException(
                            "Payment time has expired.");
                }

                List<BookingSeat> bookingSeats
                        = bookingSeatDao.findByBookingId(connection, bookingId);

                for (BookingSeat bookingSeat : bookingSeats) {
                    if (!showSeatDao.confirmHeldSeat(
                            connection,
                            bookingSeat.getShowSeatId(),
                            authenticatedUserId)) {

                        throw new IllegalStateException(
                                "One or more seats are no longer available.");
                    }
                }

                boolean confirmed
                        = bookingDao.confirmBooking(connection, bookingId);

                if (!confirmed) {
                    throw new IllegalStateException(
                            "Booking could not be confirmed.");
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }
        }

        sendTicketConfirmationEmailSafely(bookingId);
    }

    @Override
    public void cleanupExpiredBookingsAndHolds() {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                showSeatDao.releaseExpiredHolds(connection);
                bookingDao.cancelExpiredBookings(connection);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public boolean completeBooking(int bookingId) throws SQLException {
        requirePositiveId(bookingId, "Booking ID");
        validateStatusTransition(bookingDao.findById(bookingId), BookingStatus.COMPLETED);
        return bookingDao.completeBooking(bookingId);
    }

    @Override
    public boolean updateTotalAmount(int bookingId, BigDecimal totalAmount) throws SQLException {
        requirePositiveId(bookingId, "Booking ID");
        if (totalAmount == null || totalAmount.signum() < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative.");
        }
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            return false;
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can change their total amount.");
        }
        return bookingDao.updateTotalAmount(bookingId, totalAmount);
    }

    @Override
    public boolean bookingExists(int bookingId) throws SQLException {
        requirePositiveId(bookingId, "Booking ID");
        return bookingDao.existsById(bookingId);
    }

    @Override
    public int countBookingsByShowId(int showId) throws SQLException {
        requirePositiveId(showId, "Show ID");
        return bookingDao.countByShowId(showId);
    }

    private static void validateSeatIds(List<Integer> showSeatIds) {
        if (showSeatIds == null || showSeatIds.isEmpty()) {
            throw new IllegalArgumentException("At least one show seat is required.");
        }
        for (Integer showSeatId : showSeatIds) {
            if (showSeatId == null || showSeatId <= 0) {
                throw new IllegalArgumentException("Show seat IDs must be positive.");
            }
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

    private static void requireStatus(BookingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Booking status cannot be null.");
        }
    }

    private static void requirePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }

    private static void requireAuthenticatedUser(int authenticatedUserId) {
        if (authenticatedUserId <= 0) {
            throw new AuthenticationRequiredException();
        }
    }

    private static void requireBookingOwner(int authenticatedUserId, Booking booking) {
        if (booking == null) {
            return;
        }
        if (booking.getUserId() != authenticatedUserId) {
            throw new BookingAccessException("You are not allowed to access this booking.");
        }
    }

    private static void validateBookableShow(Show show) {
        if (show == null) {
            throw new IllegalArgumentException("Show was not found.");
        }
        if (show.getStatus() != ShowStatus.SCHEDULED && show.getStatus() != ShowStatus.ONGOING) {
            throw new IllegalArgumentException("Show is not available for booking.");
        }
    }

    private static void validateStatusTransition(Booking booking, BookingStatus targetStatus) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking was not found.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED
                && targetStatus != BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled bookings cannot change status.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED
                && targetStatus != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed bookings cannot change status.");
        }
    }
}
