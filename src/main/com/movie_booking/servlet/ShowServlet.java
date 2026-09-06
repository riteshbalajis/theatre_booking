package com.movie_booking.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.movie_booking.dto.request.ShowCreateRequest;
import com.movie_booking.dto.request.ShowUpdateRequest;
import com.movie_booking.dto.response.ShowResponse;
import com.movie_booking.model.Show;
import com.movie_booking.service.AuthenticationRequiredException;
import com.movie_booking.service.AuthorizationException;
import com.movie_booking.service.ShowService;
import com.movie_booking.service.ShowServiceImpl;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/shows/*")
public class ShowServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private ShowService showService;

    @Override
    public void init() throws ServletException {
        showService = new ShowServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String path = request.getPathInfo();
            if (path != null && path.matches("/movie/[0-9]+/date/[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                int movieId = parsePositiveId(path.substring(7, path.indexOf("/date/")), "Movie ID");
                LocalDate date = parseDate(path.substring(path.indexOf("/date/") + 6));
                writeJson(response, HttpServletResponse.SC_OK,
                        showResponses(showService.getShowsByMovieAndDate(movieId, date)));
                return;
            }
            if (path != null && path.matches("/movie/[0-9]+")) {
                int movieId = parsePositiveId(path.substring(7), "Movie ID");
                writeJson(response, HttpServletResponse.SC_OK,
                        showResponses(showService.getShowsByMovieId(movieId)));
                return;
            }
            if (path != null && path.matches("/date/[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                writeJson(response, HttpServletResponse.SC_OK,
                        showResponses(showService.getShowsByDate(parseDate(path.substring(6)))));
                return;
            }
            if (path != null && path.matches("/[0-9]+")) {
                Show show = showService.getShowById(parsePositiveId(path.substring(1), "Show ID"));
                if (show == null) {
                    writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                            errorResponse("Show was not found."));
                    return;
                }
                writeJson(response, HttpServletResponse.SC_OK, showResponse(show));
                return;
            }
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                    errorResponse("Show endpoint was not found."));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to retrieve shows", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Shows could not be retrieved."));
        } catch (RuntimeException exception) {
            log("Unexpected error while retrieving shows", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Shows could not be retrieved."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if (!isCollectionPath(request.getPathInfo())) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Show endpoint was not found."));
                return;
            }
            ShowCreateRequest createRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), ShowCreateRequest.class);
            int showId = showService.addShow(toShow(createRequest), sessionUserId(request));
            Map<String, Object> body = successResponse("Show created successfully.");
            body.put("showId", showId);
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
            writeJson(response, showErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to create show", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show could not be created."));
        } catch (RuntimeException exception) {
            log("Unexpected error while creating show", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show could not be created."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int showId = parseNumericPathId(request.getPathInfo(), "Show ID");
            ShowUpdateRequest updateRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), ShowUpdateRequest.class);
            if (updateRequest != null) {
                updateRequest.setShowId(showId);
            }
            boolean updated = showService.updateShow(toShow(updateRequest), sessionUserId(request));
            if (!updated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Show was not found."));
                return;
            }
            Show show = showService.getShowById(showId);
            writeJson(response, HttpServletResponse.SC_OK, showResponse(show));
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
            writeJson(response, showErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to update show", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show could not be updated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while updating show", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show could not be updated."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
                boolean cancelled = showService.cancelShow(
                    parseNumericPathId(request.getPathInfo(), "Show ID"), sessionUserId(request));
            if (!cancelled) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Show was not found."));
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
            log("Unable to cancel show", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show could not be cancelled."));
        } catch (RuntimeException exception) {
            log("Unexpected error while cancelling show", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Show could not be cancelled."));
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
        javax.servlet.http.HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("userId");
        return value instanceof Integer ? (Integer) value : 0;
    }

    private static int parseNumericPathId(String path, String fieldName) {
        if (path == null || !path.matches("/[0-9]+")) {
            throw new IllegalArgumentException("A valid " + fieldName + " is required.");
        }
        return parsePositiveId(path.substring(1), fieldName);
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

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Date must be a valid ISO date.");
        }
    }

    private static Show toShow(ShowCreateRequest request) {
        if (request == null) {
            return null;
        }
        Show show = new Show();
        show.setMovieId(request.getMovieId());
        show.setScreenId(request.getScreenId());
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        show.setRegularPrice(request.getRegularPrice());
        show.setPremiumPrice(request.getPremiumPrice());
        show.setReclinerPrice(request.getReclinerPrice());
        return show;
    }

    private static Show toShow(ShowUpdateRequest request) {
        if (request == null) {
            return null;
        }
        Show show = new Show();
        show.setShowId(request.getShowId());
        show.setMovieId(request.getMovieId());
        show.setScreenId(request.getScreenId());
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        return show;
    }

    private static List<Map<String, Object>> showResponses(List<Show> shows) {
        return shows.stream().map(ShowServlet::showResponse).collect(Collectors.toList());
    }

    private static Map<String, Object> showResponse(Show show) {
        ShowResponse response = new ShowResponse();
        response.setShowId(show.getShowId());
        response.setMovieId(show.getMovieId());
        response.setScreenId(show.getScreenId());
        response.setShowDate(show.getShowDate());
        response.setStartTime(show.getStartTime());
        response.setEndTime(show.getEndTime());
        response.setStatus(show.getStatus());
        response.setCreatedAt(show.getCreatedAt());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("showId", response.getShowId());
        body.put("movieId", response.getMovieId());
        body.put("screenId", response.getScreenId());
        body.put("showDate", response.getShowDate() == null ? null : response.getShowDate().toString());
        body.put("startTime", response.getStartTime() == null ? null : response.getStartTime().toString());
        body.put("endTime", response.getEndTime() == null ? null : response.getEndTime().toString());
        body.put("status", response.getStatus());
        body.put("createdAt", response.getCreatedAt() == null ? null : response.getCreatedAt().toString());
        return body;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    private static int showErrorStatus(IllegalArgumentException exception) {
        return exception.getMessage() != null && exception.getMessage().contains("overlaps")
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

    private static final class LocalDateDeserializer extends StdDeserializer<LocalDate> {
        private LocalDateDeserializer() {
            super(LocalDate.class);
        }

        @Override
        public LocalDate deserialize(com.fasterxml.jackson.core.JsonParser parser,
                DeserializationContext context) throws IOException {
            try {
                return LocalDate.parse(parser.getText().trim());
            } catch (DateTimeParseException exception) {
                throw new JsonProcessingException("showDate must be a valid ISO date.", exception) { };
            }
        }
    }

    private static final class LocalTimeDeserializer extends StdDeserializer<LocalTime> {
        private LocalTimeDeserializer() {
            super(LocalTime.class);
        }

        @Override
        public LocalTime deserialize(com.fasterxml.jackson.core.JsonParser parser,
                DeserializationContext context) throws IOException {
            try {
                return LocalTime.parse(parser.getText().trim());
            } catch (DateTimeParseException exception) {
                throw new JsonProcessingException("Time must be a valid ISO time.", exception) { };
            }
        }
    }
}