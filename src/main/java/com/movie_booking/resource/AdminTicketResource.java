package com.movie_booking.resource;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.request.TicketCheckInRequest;
import com.movie_booking.dto.response.ErrorResponse;
import com.movie_booking.dto.response.TicketCheckInResponse;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.service.AdminTicketService;
import com.movie_booking.service.AdminTicketServiceImpl;

@Path("/admin/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminTicketResource {

    private final AdminTicketService adminTicketService;

    @Context
    private HttpServletRequest httpRequest;

    public AdminTicketResource() {
        this.adminTicketService = new AdminTicketServiceImpl();
    }

    @POST
    @Path("/checkin")
    public Response checkInTicket(TicketCheckInRequest request)
            throws SQLException {
        int adminUserId = getAuthenticatedUserId();

        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(
                            "TICKET_CODE_REQUIRED",
                            "Ticket code is required."
                    ))
                    .build();
        }

        TicketCheckInResponse result = adminTicketService.checkInTicket(
                adminUserId,
                request.getTicketCode());

        if (result.isValid()) {
            return Response.ok(result).build();
        }

        Response.Status responseStatus = "FORBIDDEN".equals(result.getCode())
                ? Response.Status.FORBIDDEN
                : Response.Status.BAD_REQUEST;

        return Response.status(responseStatus)
                .entity(result)
                .build();
    }

    private int getAuthenticatedUserId() {
        HttpSession session = httpRequest.getSession(false);
        Object userIdAttribute = session == null
                ? null
                : session.getAttribute("userId");

        if (!(userIdAttribute instanceof Integer)) {
            throw new UnauthorizedException("Administrator login is required.");
        }

        return (Integer) userIdAttribute;
    }
}
