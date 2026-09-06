package com.movie_booking.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie_booking.dto.request.SeatCreateRequest;
import com.movie_booking.dto.request.SeatUpdateRequest;
import com.movie_booking.dto.response.SeatResponse;
import com.movie_booking.model.Seat;
import com.movie_booking.service.AuthenticationRequiredException;
import com.movie_booking.service.AuthorizationException;
import com.movie_booking.service.SeatService;
import com.movie_booking.service.SeatServiceImpl;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/seats/*")
public class SeatServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private SeatService seatService;

    @Override
    public void init() throws ServletException {
        seatService = new SeatServiceImpl();
    }

   @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
    try {
        String path = request.getPathInfo();

        // GET /api/seats/screen/{screenId}
        if (path != null && path.matches("/screen/[0-9]+")) {
            int screenId = Integer.parseInt(path.substring("/screen/".length()));

            List<Seat> seats = seatService.getSeatsByScreenId(screenId);

            writeJson(response, HttpServletResponse.SC_OK, seatResponses(seats));
            return;
        }

        // GET /api/seats/{seatId}
        Seat seat = seatService.getSeatById(parseSeatId(path));

        if (seat == null) {
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                    errorResponse("Seat was not found."));
            return;
        }

        writeJson(response, HttpServletResponse.SC_OK, seatResponse(seat));

    } catch (IllegalArgumentException exception) {
        writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                errorResponse(exception.getMessage()));

    } catch (SQLException exception) {
        log("Unable to retrieve seat", exception);
        writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                errorResponse("Seats could not be retrieved."));

    } catch (RuntimeException exception) {
        log("Unexpected error while retrieving seats", exception);
        writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                errorResponse("Seats could not be retrieved."));
    }
}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if (!isCollectionPath(request.getPathInfo())) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Seat endpoint was not found."));
                return;
            }

            SeatCreateRequest createRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), SeatCreateRequest.class);
            int seatId = seatService.addSeat(toSeat(createRequest), sessionUserId(request));
            Map<String, Object> body = successResponse("Seat created successfully.");
            body.put("seatId", seatId);
            writeJson(response, HttpServletResponse.SC_CREATED, body);
        } catch (JsonProcessingException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse("Request body must contain valid JSON."));
        } catch (AuthenticationRequiredException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                errorResponse(exception.getMessage()));
        } catch (AuthorizationException exception) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, seatErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to create seat", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Seat could not be created."));
        } catch (RuntimeException exception) {
            log("Unexpected error while creating seat", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Seat could not be created."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int seatId = parseSeatId(request.getPathInfo());
            Seat existingSeat = seatService.getSeatById(seatId);
            if (existingSeat == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Seat was not found."));
                return;
            }

            SeatUpdateRequest updateRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), SeatUpdateRequest.class);
            updateRequest.setSeatId(seatId);
            Seat updatedSeat = toSeat(updateRequest);
            if (updatedSeat != null) {
                updatedSeat.setScreenId(existingSeat.getScreenId());
            }

            boolean updated = seatService.updateSeat(updatedSeat, sessionUserId(request));
            if (!updated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Seat was not found."));
                return;
            }
            Seat seat = seatService.getSeatById(seatId);
            writeJson(response, HttpServletResponse.SC_OK, seatResponse(seat));
        } catch (JsonProcessingException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse("Request body must contain valid JSON."));
        } catch (AuthenticationRequiredException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                errorResponse(exception.getMessage()));
        } catch (AuthorizationException exception) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, seatErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to update seat", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Seat could not be updated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while updating seat", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Seat could not be updated."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
                boolean deactivated = seatService.deactivateSeat(
                    parseSeatId(request.getPathInfo()), sessionUserId(request));
            if (!deactivated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Seat was not found."));
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (AuthenticationRequiredException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                errorResponse(exception.getMessage()));
        } catch (AuthorizationException exception) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to deactivate seat", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Seat could not be deactivated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while deactivating seat", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Seat could not be deactivated."));
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)
                && !"PUT".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            response.setHeader("Allow", "GET, POST, PUT, DELETE");
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

    private static boolean isCollectionPath(String path) {
        return path == null || path.isEmpty() || "/".equals(path);
    }

    private static int sessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("userId");
        return value instanceof Integer ? (Integer) value : 0;
    }

    private static int parseSeatId(String path) {
        if (path == null || !path.matches("/[0-9]+")) {
            throw new IllegalArgumentException("A valid seat ID is required.");
        }
        try {
            return Integer.parseInt(path.substring(1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A valid seat ID is required.");
        }
    }

    private static Seat toSeat(SeatCreateRequest request) {
        if (request == null) {
            return null;
        }
        Seat seat = new Seat();
        seat.setScreenId(request.getScreenId());
        seat.setRowLabel(request.getRowLabel());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        return seat;
    }

    private static Seat toSeat(SeatUpdateRequest request) {
        if (request == null) {
            return null;
        }
        Seat seat = new Seat();
        seat.setSeatId(request.getSeatId());
        seat.setRowLabel(request.getRowLabel());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        return seat;
    }

    private static List<Map<String, Object>> seatResponses(List<Seat> seats) {
        return seats.stream().map(SeatServlet::seatResponse).collect(Collectors.toList());
    }

    private static Map<String, Object> seatResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setSeatId(seat.getSeatId());
        response.setScreenId(seat.getScreenId());
        response.setRowLabel(seat.getRowLabel());
        response.setSeatNumber(seat.getSeatNumber());
        response.setSeatType(seat.getSeatType());
        response.setStatus(seat.getStatus());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("seatId", response.getSeatId());
        body.put("screenId", response.getScreenId());
        body.put("rowLabel", response.getRowLabel());
        body.put("seatNumber", response.getSeatNumber());
        body.put("seatType", response.getSeatType());
        body.put("status", response.getStatus());
        return body;
    }

    private static int seatErrorStatus(IllegalArgumentException exception) {
        return exception.getMessage() != null && exception.getMessage().contains("already exists")
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
}