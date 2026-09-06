package com.movie_booking.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie_booking.dto.request.LoginRequest;
import com.movie_booking.dto.request.RegisterRequest;
import com.movie_booking.dto.response.LoginResponse;
import com.movie_booking.dto.response.UserResponse;
import com.movie_booking.service.UserService;
import com.movie_booking.service.UserServiceImpl;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        String endpoint = request.getPathInfo();

        if ("/register".equals(endpoint)) {
            register(request, response);
        } else if ("/login".equals(endpoint)) {
            login(request, response);
        } else if ("/logout".equals(endpoint)) {
            logout(request, response);
        } else {
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                    errorResponse("Authentication endpoint was not found."));
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Allow", "POST");
            writeJson(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    errorResponse("Only POST is supported for this endpoint."));
            return;
        }
        super.service(request, response);
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            RegisterRequest registerRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), RegisterRequest.class);
            int userId = userService.registerUser(registerRequest);
            Map<String, Object> body = successResponse("Registration successful.");
            body.put("userId", userId);
            writeJson(response, HttpServletResponse.SC_CREATED, body);
        } catch (JsonProcessingException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse("Request body must contain valid JSON."));
        } catch (IllegalArgumentException exception) {
            writeJson(response, registrationErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to register user", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Registration could not be completed."));
        } catch (RuntimeException exception) {
            log("Unexpected error while registering user", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Registration could not be completed."));
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            LoginRequest loginRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), LoginRequest.class);
            LoginResponse loginResponse = userService.login(loginRequest);
            if (loginResponse == null || loginResponse.getUser() == null) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        errorResponse("Invalid email or password."));
                return;
            }

            request.getSession(true).setAttribute("userId", loginResponse.getUser().getUserId());
            Map<String, Object> body = successResponse(loginResponse.getMessage());
            body.put("user", safeUserResponse(loginResponse.getUser()));
            writeJson(response, HttpServletResponse.SC_OK, body);
        } catch (JsonProcessingException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse("Request body must contain valid JSON."));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to authenticate user", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Login could not be completed."));
        } catch (RuntimeException exception) {
            log("Unexpected error while authenticating user", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Login could not be completed."));
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    private static int registrationErrorStatus(IllegalArgumentException exception) {
        return exception.getMessage() != null
                && exception.getMessage().contains("already registered")
                ? HttpServletResponse.SC_CONFLICT
                : HttpServletResponse.SC_BAD_REQUEST;
    }

    private static Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message == null ? "Success." : message);
        return response;
    }

    private static Map<String, Object> safeUserResponse(UserResponse user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", user.getUserId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole());
        response.put("status", user.getStatus());
        return response;
    }

    private static Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", message == null ? "Invalid request." : message);
        return response;
    }
}