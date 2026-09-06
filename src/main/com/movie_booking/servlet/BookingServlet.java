package com.movie_booking.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie_booking.dto.request.BookTicketsRequest;
import com.movie_booking.dto.response.BookingResponse;
import com.movie_booking.model.Booking;
import com.movie_booking.service.BookingService;
import com.movie_booking.service.BookingServiceImpl;
import com.movie_booking.service.AuthenticationRequiredException;
import com.movie_booking.service.BookingAccessException;
import com.movie_booking.dto.response.BookingSeatResponse;
import com.movie_booking.model.BookingSeat;
import com.movie_booking.model.Seat;
import com.movie_booking.model.ShowSeat;
import com.movie_booking.service.SeatService;
import com.movie_booking.service.SeatServiceImpl;
import com.movie_booking.service.ShowSeatService;
import com.movie_booking.service.ShowSeatServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/bookings/*")
public class BookingServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private BookingService bookingService;
    private ShowSeatService showSeatService;
    private SeatService seatService;

    @Override
    public void init() throws ServletException {
        bookingService = new BookingServiceImpl();
        showSeatService = new ShowSeatServiceImpl();
        seatService = new SeatServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if (!isCollectionPath(request.getPathInfo())) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Booking endpoint was not found."));
                return;
            }

            int userId = authenticatedUserId(request);
            BookTicketsRequest ticketRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), BookTicketsRequest.class);
            if (ticketRequest == null) {
                throw new IllegalArgumentException("Request body is required.");
            }

            int bookingId = bookingService.createBookingWithSeats(
                    userId, ticketRequest.getShowId(), ticketRequest.getShowSeatIds());
            Map<String, Object> body = successResponse("Booking created successfully.");
            body.put("bookingId", bookingId);
            writeJson(response, HttpServletResponse.SC_CREATED, body);
        } catch (JsonProcessingException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse("Request body must contain valid JSON."));
        } catch (UnauthorizedException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, bookingErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to create booking", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Booking could not be created."));
        } catch (RuntimeException exception) {
            log("Unexpected error while creating booking", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Booking could not be created."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int userId = authenticatedUserId(request);
            String path = request.getPathInfo();
            if (isCollectionPath(path)) {
                writeJson(response, HttpServletResponse.SC_OK,
                    bookingResponses(bookingService.getBookingsForUser(userId), userId));
                return;
            }

            Booking booking = bookingService.getBookingById(userId, parseBookingId(path));
            if (booking == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Booking was not found."));
                return;
            }
            writeJson(response, HttpServletResponse.SC_OK, bookingResponse(booking, userId));
        } catch (UnauthorizedException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    errorResponse(exception.getMessage()));
        } catch (BookingAccessException exception) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to retrieve bookings", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Bookings could not be retrieved."));
        } catch (RuntimeException exception) {
            log("Unexpected error while retrieving bookings", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Bookings could not be retrieved."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int userId = authenticatedUserId(request);
            int bookingId = parseBookingId(request.getPathInfo());
            Booking booking = bookingService.getBookingById(userId, bookingId);
            if (booking == null) {
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                errorResponse("Booking was not found."));
            return;
            }
            boolean cancelled = bookingService.cancelBooking(userId, bookingId);
            if (!cancelled) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Booking was not found."));
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (UnauthorizedException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    errorResponse(exception.getMessage()));
            } catch (AuthenticationRequiredException exception) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    errorResponse(exception.getMessage()));
            } catch (BookingAccessException exception) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                    errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to cancel booking", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Booking could not be cancelled."));
        } catch (RuntimeException exception) {
            log("Unexpected error while cancelling booking", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Booking could not be cancelled."));
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)
                && !"DELETE".equalsIgnoreCase(method)) {
            response.setHeader("Allow", "GET, POST, DELETE");
            writeJson(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    errorResponse("HTTP method is not supported for this endpoint."));
            return;
        }
        super.service(request, response);
    }

    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    private static int authenticatedUserId(HttpServletRequest request)
            throws UnauthorizedException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("userId");
        if (!(value instanceof Integer) || ((Integer) value) <= 0) {
            throw new UnauthorizedException("Authentication is required.");
        }
        return (Integer) value;
    }

    private static boolean isCollectionPath(String path) {
        return path == null || path.isEmpty() || "/".equals(path);
    }

    private static int parseBookingId(String path) {
        if (path == null || !path.matches("/[0-9]+")) {
            throw new IllegalArgumentException("A valid booking ID is required.");
        }
        try {
            int bookingId = Integer.parseInt(path.substring(1));
            if (bookingId <= 0) {
                throw new NumberFormatException();
            }
            return bookingId;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A valid booking ID is required.");
        }
    }

       private List<Map<String, Object>> bookingResponses(List<Booking> bookings,
           int authenticatedUserId)
        throws SQLException {

    List<Map<String, Object>> responses = new ArrayList<>();

    for (Booking booking : bookings) {
        responses.add(bookingResponse(booking, authenticatedUserId));
    }

    return responses;
}

       private Map<String, Object> bookingResponse(Booking booking, int authenticatedUserId)
           throws SQLException {
        BookingResponse response = new BookingResponse();

        response.setBookingId(booking.getBookingId());
        response.setUserId(booking.getUserId());
        response.setShowId(booking.getShowId());
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus());
        response.setBookedAt(booking.getBookedAt());

        List<BookingSeat> bookingSeats =
                bookingService.getBookingSeats(authenticatedUserId, booking.getBookingId());

        List<BookingSeatResponse> seatResponses = new java.util.ArrayList<>();

        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat =
                    showSeatService.getShowSeatById(bookingSeat.getShowSeatId());

            if (showSeat == null) {
                continue;
            }

            Seat seat = seatService.getSeatById(showSeat.getSeatId());

            if (seat == null) {
                continue;
            }

            BookingSeatResponse seatResponse = new BookingSeatResponse();

            seatResponse.setBookingSeatId(bookingSeat.getBookingSeatId());
            seatResponse.setBookingId(bookingSeat.getBookingId());
            seatResponse.setShowSeatId(bookingSeat.getShowSeatId());
            seatResponse.setRowLabel(seat.getRowLabel());
            seatResponse.setSeatNumber(seat.getSeatNumber());
            seatResponse.setPrice(bookingSeat.getPrice());

            seatResponses.add(seatResponse);
        }

        response.setSeats(seatResponses);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bookingId", response.getBookingId());
        body.put("userId", response.getUserId());
        body.put("showId", response.getShowId());
        body.put("totalAmount", response.getTotalAmount());
        body.put("status", response.getStatus());
        body.put("bookedAt", response.getBookedAt() == null
                ? null : response.getBookedAt().toString());
        body.put("seats", response.getSeats());

        return body;
    }
    private static int bookingErrorStatus(IllegalArgumentException exception) {
        return exception.getMessage() != null && exception.getMessage().contains("unavailable")
                ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST;
    }

    private static Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    private static Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", message == null ? "Invalid request." : message);
        return response;
    }

    private static final class UnauthorizedException extends Exception {
        private UnauthorizedException(String message) {
            super(message);
        }
    }
}