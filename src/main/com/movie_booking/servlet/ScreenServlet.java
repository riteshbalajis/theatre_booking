package com.movie_booking.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie_booking.dto.request.ScreenCreateRequest;
import com.movie_booking.dto.request.ScreenUpdateRequest;
import com.movie_booking.dto.response.ScreenResponse;
import com.movie_booking.model.Screen;
import com.movie_booking.service.AuthenticationRequiredException;
import com.movie_booking.service.AuthorizationException;
import com.movie_booking.service.ScreenService;
import com.movie_booking.service.ScreenServiceImpl;
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

@WebServlet("/api/screens/*")
public class ScreenServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ScreenService screenService;

    @Override
    public void init() throws ServletException {
        screenService = new ScreenServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String path = request.getPathInfo();
            if (isCollectionPath(path)) {
                writeJson(response, HttpServletResponse.SC_OK,
                        screenResponses(screenService.getAllScreens()));
                return;
            }

            Screen screen = screenService.getScreenById(parseScreenId(path));
            if (screen == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Screen was not found."));
                return;
            }
            writeJson(response, HttpServletResponse.SC_OK, screenResponse(screen));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to retrieve screens", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screens could not be retrieved."));
        } catch (RuntimeException exception) {
            log("Unexpected error while retrieving screens", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screens could not be retrieved."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if (!isCollectionPath(request.getPathInfo())) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Screen endpoint was not found."));
                return;
            }

            ScreenCreateRequest createRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), ScreenCreateRequest.class);
                int screenId = screenService.addScreen(toScreen(createRequest),
                    sessionUserId(request));
            Map<String, Object> body = successResponse("Screen created successfully.");
            body.put("screenId", screenId);
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
            writeJson(response, screenErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to create screen", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screen could not be created."));
        } catch (RuntimeException exception) {
            log("Unexpected error while creating screen", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screen could not be created."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int screenId = parseScreenId(request.getPathInfo());
            Screen existingScreen = screenService.getScreenById(screenId);
            if (existingScreen == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Screen was not found."));
                return;
            }
            ScreenUpdateRequest updateRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), ScreenUpdateRequest.class);
            updateRequest.setScreenId(screenId);

            Screen updatedScreen = toScreen(updateRequest);
            if (updatedScreen != null) {
                updatedScreen.setTheatreId(existingScreen.getTheatreId());
            }
            boolean updated = screenService.updateScreen(updatedScreen, sessionUserId(request));
            if (!updated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Screen was not found."));
                return;
            }
            Screen screen = screenService.getScreenById(screenId);
            writeJson(response, HttpServletResponse.SC_OK, screenResponse(screen));
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
            writeJson(response, screenErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to update screen", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screen could not be updated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while updating screen", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screen could not be updated."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
                boolean deactivated = screenService.deactivateScreen(
                    parseScreenId(request.getPathInfo()), sessionUserId(request));
            if (!deactivated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Screen was not found."));
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
            log("Unable to deactivate screen", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screen could not be deactivated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while deactivating screen", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Screen could not be deactivated."));
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

    private static int parseScreenId(String path) {
        if (path == null || !path.matches("/[0-9]+")) {
            throw new IllegalArgumentException("A valid screen ID is required.");
        }
        try {
            return Integer.parseInt(path.substring(1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A valid screen ID is required.");
        }
    }

    private static Screen toScreen(ScreenCreateRequest request) {
        if (request == null) {
            return null;
        }
        Screen screen = new Screen();
        screen.setTheatreId(request.getTheatreId());
        screen.setName(request.getName());
        screen.setCapacity(request.getCapacity());
        return screen;
    }

    private static Screen toScreen(ScreenUpdateRequest request) {
        if (request == null) {
            return null;
        }
        Screen screen = new Screen();
        screen.setScreenId(request.getScreenId());
        screen.setName(request.getName());
        screen.setCapacity(request.getCapacity());
        return screen;
    }

    private static List<Map<String, Object>> screenResponses(List<Screen> screens) {
        return screens.stream().map(ScreenServlet::screenResponse).collect(Collectors.toList());
    }

    private static Map<String, Object> screenResponse(Screen screen) {
        ScreenResponse response = new ScreenResponse();
        response.setScreenId(screen.getScreenId());
        response.setTheatreId(screen.getTheatreId());
        response.setName(screen.getName());
        response.setCapacity(screen.getCapacity());
        response.setStatus(screen.getStatus());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("screenId", response.getScreenId());
        body.put("theatreId", response.getTheatreId());
        body.put("name", response.getName());
        body.put("capacity", response.getCapacity());
        body.put("status", response.getStatus());
        return body;
    }

    private static int screenErrorStatus(IllegalArgumentException exception) {
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