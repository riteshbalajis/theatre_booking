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

import com.movie_booking.dto.request.TotpRecoveryVerifyRequest;
import com.movie_booking.dto.response.ErrorResponse;
import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.TotpRecoveryResponse;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.service.TotpRecoveryService;
import com.movie_booking.service.TotpRecoveryServiceImpl;

@Path("/totp/recovery")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TotpRecoveryResource {

    private final TotpRecoveryService recoveryService;

    @Context
    private HttpServletRequest httpRequest;

    public TotpRecoveryResource() {
        this.recoveryService = new TotpRecoveryServiceImpl();
    }

    @POST
    @Path("/email/request")
    public Response requestEmailRecovery() throws SQLException {
        HttpSession session = getSession();
        int userId = getPendingTotpUserId(session);

        recoveryService.requestEmailRecovery(userId);

        return Response.ok(
                new MessageResponse("Recovery code sent to your email.")
        ).build();
    }

    @POST
    @Path("/email/verify")
    public Response verifyEmailRecovery(
            TotpRecoveryVerifyRequest request
    ) throws SQLException {
        HttpSession session = getSession();
        int userId = getPendingTotpUserId(session);

        if (request == null || request.getOtp() == null
                || request.getOtp().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(
                            "RECOVERY_CODE_REQUIRED",
                            "Recovery code is required."
                    ))
                    .build();
        }

        boolean verified = recoveryService.verifyEmailRecovery(userId,request.getOtp());

        if (!verified) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(
                            "INVALID_RECOVERY_CODE",
                            "Invalid recovery code."
                    ))
                    .build();
        }

        session.setAttribute("totpRecoveryUserId", userId);
        session.setAttribute("totpRecoveryVerified", true);

        return Response.ok(
                new TotpRecoveryResponse(
                        "Email recovery verified successfully.",
                        true
                )
        ).build();
    }

    private HttpSession getSession() {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw new UnauthorizedException(
                    "TOTP verification is not pending."
            );
        }
        return session;
    }

    private int getPendingTotpUserId(HttpSession session) {
        Object attribute = session.getAttribute("pendingTotpUserId");
        if (!(attribute instanceof Integer)) {
            throw new UnauthorizedException(
                    "TOTP verification is not pending."
            );
        }
        return (Integer) attribute;
    }
}
