package com.movie_booking.resource;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.response.AuditLogResponse;
import com.movie_booking.dto.response.SecurityLogResponse;
import com.movie_booking.dto.response.UserResponse;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import com.movie_booking.service.AuditService;
import com.movie_booking.service.UserService;
import com.movie_booking.service.UserServiceImpl;

@Path("/admin/logs")
@Produces(MediaType.APPLICATION_JSON)
public class AdminLogResource {

    private final AuditService auditService;
    private final UserService userService;

    @Context
    private HttpServletRequest httpRequest;

    public AdminLogResource() {
        this.auditService = new AuditService();
        this.userService = new UserServiceImpl();
    }

    @GET
    @Path("/audit")
    public Response getAuditLogs() throws SQLException {
        getAuthenticatedAdminId();
        List<AuditLogResponse> logs = auditService.getLast20AuditLogs();
        return Response.ok(logs).build();
    }

    @GET
    @Path("/security")
    public Response getSecurityLogs() throws SQLException {
        getAuthenticatedAdminId();
        List<SecurityLogResponse> logs = auditService.getLast20SecurityLogs();
        return Response.ok(logs).build();
    }

    private int getAuthenticatedAdminId() throws SQLException {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthorizedException("User is not logged in.");
        }

        int userId = (int) session.getAttribute("userId");
        UserResponse user = userService.getUserById(userId);

        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User is not active.");
        }

        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Admin access required.");
        }

        return userId;
    }
}
