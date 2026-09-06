package com.movie_booking.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie_booking.dto.response.ShowSeatResponse;
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

@WebServlet("/api/show-seats/*")
public class ShowSeatServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ShowSeatService showSeatService;
    private SeatService seatService;

    @Override
    public void init() throws ServletException {
        showSeatService = new ShowSeatServiceImpl();
        seatService = new SeatServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String path = request.getPathInfo();
            if (path != null && path.matches("/show/[0-9]+/available")) {
                int showId = parsePositiveId(path.substring(6, path.length() - 10), "Show ID");
                writeJson(response, HttpServletResponse.SC_OK,
                        showSeatResponses(showSeatService.getAvailableShowSeatsByShowId(showId)));
                return;
            }
            if (path != null && path.matches("/show/[0-9]+/booked")) {
                int showId = parsePositiveId(path.substring(6, path.length() - 7), "Show ID");
                writeJson(response, HttpServletResponse.SC_OK,
                        showSeatResponses(showSeatService.getBookedShowSeatsByShowId(showId)));
                return;
            }
            if (path != null && path.matches("/show/[0-9]+/count")) {
                int showId = parsePositiveId(path.substring(6, path.length() - 6), "Show ID");
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("showId", showId);
                body.put("availableSeatCount",
                        showSeatService.getAvailableSeatCountByShowId(showId));
                writeJson(response, HttpServletResponse.SC_OK, body);
                return;
            }
            if (path != null && path.matches("/show/[0-9]+")) {
                int showId = parsePositiveId(path.substring(6), "Show ID");
                writeJson(response, HttpServletResponse.SC_OK,
                        showSeatResponses(showSeatService.getShowSeatsByShowId(showId)));
                return;
            }
            if (path != null && path.matches("/[0-9]+")) {
                ShowSeat showSeat = showSeatService.getShowSeatById(
                        parsePositiveId(path.substring(1), "Show seat ID"));
                if (showSeat == null) {
                    writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                            errorResponse("Show seat was not found."));
                    return;
                }
                writeJson(response, HttpServletResponse.SC_OK, showSeatResponse(showSeat));
                return;
            }
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                    errorResponse("Show seat endpoint was not found."));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to retrieve show seats", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show seats could not be retrieved."));
        } catch (RuntimeException exception) {
            log("Unexpected error while retrieving show seats", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show seats could not be retrieved."));
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Allow", "GET");
            writeJson(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    errorResponse("Only GET is supported for this endpoint."));
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

    private List<Map<String, Object>> showSeatResponses(List<ShowSeat> showSeats)
            throws SQLException {
        List<Map<String, Object>> responses = new ArrayList<>();
        for (ShowSeat showSeat : showSeats) {
            responses.add(showSeatResponse(showSeat));
        }
        return responses;
    }

    private Map<String, Object> showSeatResponse(ShowSeat showSeat) throws SQLException {
        Seat seat = seatService.getSeatById(showSeat.getSeatId());

        ShowSeatResponse response = new ShowSeatResponse();
        response.setShowSeatId(showSeat.getShowSeatId());
        response.setShowId(showSeat.getShowId());
        response.setSeatId(showSeat.getSeatId());
        response.setRowLabel(seat == null ? null : seat.getRowLabel());
        response.setSeatNumber(seat == null ? 0 : seat.getSeatNumber());
        response.setSeatType(seat == null || seat.getSeatType() == null
                ? null : seat.getSeatType().name());
        response.setStatus(showSeat.getStatus());
        response.setPrice(showSeat.getPrice());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("showSeatId", response.getShowSeatId());
        body.put("showId", response.getShowId());
        body.put("seatId", response.getSeatId());
        body.put("rowLabel", response.getRowLabel());
        body.put("seatNumber", response.getSeatNumber());
        body.put("seatType", response.getSeatType());
        body.put("status", response.getStatus());
        body.put("price", response.getPrice());
        return body;
    }

    private static int parsePositiveId(String value, String fieldName) {
        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A valid " + fieldName + " is required.");
        }
    }

    private static Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", message == null ? "Invalid request." : message);
        return response;
    }

}
