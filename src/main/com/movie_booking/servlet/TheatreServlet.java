package com.movie_booking.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie_booking.dto.request.TheatreCreateRequest;
import com.movie_booking.dto.request.TheatreUpdateRequest;
import com.movie_booking.dto.response.TheatreResponse;
import com.movie_booking.model.Theatre;
import com.movie_booking.service.AuthenticationRequiredException;
import com.movie_booking.service.AuthorizationException;
import com.movie_booking.service.TheatreService;
import com.movie_booking.service.TheatreServiceImpl;
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

@WebServlet("/api/theatres/*")
public class TheatreServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private TheatreService theatreService;

    @Override
    public void init() throws ServletException {
        theatreService = new TheatreServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String path = request.getPathInfo();
            if (isCollectionPath(path)) {
                List<Theatre> theatres = theatreService.getAllTheatres();
                writeJson(response, HttpServletResponse.SC_OK, theatreResponses(theatres));
                return;
            }

            Theatre theatre = theatreService.getTheatreById(parseTheatreId(path));
            if (theatre == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Theatre was not found."));
                return;
            }
            writeJson(response, HttpServletResponse.SC_OK, theatreResponse(theatre));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to retrieve theatres", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatres could not be retrieved."));
        } catch (RuntimeException exception) {
            log("Unexpected error while retrieving theatres", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatres could not be retrieved."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if (!isCollectionPath(request.getPathInfo())) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Theatre endpoint was not found."));
                return;
            }

            TheatreCreateRequest createRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), TheatreCreateRequest.class);
                int theatreId = theatreService.addTheatre(toTheatre(createRequest),
                    sessionUserId(request));
            Map<String, Object> body = successResponse("Theatre created successfully.");
            body.put("theatreId", theatreId);
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
            writeJson(response, theatreErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to create theatre", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatre could not be created."));
        } catch (RuntimeException exception) {
            log("Unexpected error while creating theatre", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatre could not be created."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int theatreId = parseTheatreId(request.getPathInfo());
            TheatreUpdateRequest updateRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), TheatreUpdateRequest.class);
            updateRequest.setTheatreId(theatreId);

                boolean updated = theatreService.updateTheatre(toTheatre(updateRequest),
                    sessionUserId(request));
            if (!updated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Theatre was not found."));
                return;
            }
            Theatre theatre = theatreService.getTheatreById(theatreId);
            writeJson(response, HttpServletResponse.SC_OK, theatreResponse(theatre));
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
            writeJson(response, theatreErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to update theatre", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatre could not be updated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while updating theatre", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatre could not be updated."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
                boolean deactivated = theatreService.deactivateTheatre(
                    parseTheatreId(request.getPathInfo()), sessionUserId(request));
            if (!deactivated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Theatre was not found."));
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
            log("Unable to deactivate theatre", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatre could not be deactivated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while deactivating theatre", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Theatre could not be deactivated."));
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

    private static int parseTheatreId(String path) {
        if (path == null || !path.matches("/[0-9]+")) {
            throw new IllegalArgumentException("A valid theatre ID is required.");
        }
        try {
            return Integer.parseInt(path.substring(1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A valid theatre ID is required.");
        }
    }

    private static Theatre toTheatre(TheatreCreateRequest request) {
        if (request == null) {
            return null;
        }
        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setLocation(request.getLocation());
        return theatre;
    }

    private static Theatre toTheatre(TheatreUpdateRequest request) {
        if (request == null) {
            return null;
        }
        Theatre theatre = new Theatre();
        theatre.setTheatreId(request.getTheatreId());
        theatre.setName(request.getName());
        theatre.setLocation(request.getLocation());
        return theatre;
    }

    private static List<Map<String, Object>> theatreResponses(List<Theatre> theatres) {
        return theatres.stream().map(TheatreServlet::theatreResponse)
                .collect(Collectors.toList());
    }

    private static Map<String, Object> theatreResponse(Theatre theatre) {
        TheatreResponse response = new TheatreResponse();
        response.setTheatreId(theatre.getTheatreId());
        response.setName(theatre.getName());
        response.setLocation(theatre.getLocation());
        response.setStatus(theatre.getStatus());
        response.setCreatedAt(theatre.getCreatedAt());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("theatreId", response.getTheatreId());
        body.put("name", response.getName());
        body.put("location", response.getLocation());
        body.put("status", response.getStatus());
        body.put("createdAt", response.getCreatedAt() == null
                ? null : response.getCreatedAt().toString());
        return body;
    }

    private static int theatreErrorStatus(IllegalArgumentException exception) {
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