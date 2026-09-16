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

import com.movie_booking.dto.request.TotpVerifyRequest;
import com.movie_booking.dto.response.ErrorResponse;
import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.TotpSetupResponse;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.service.TotpService;
import com.movie_booking.service.TotpServiceImpl;

@Path("/totp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TotpResource {

    private final TotpService totpService;

    @Context
    private HttpServletRequest httpRequest;

    public TotpResource() {
        this.totpService = new TotpServiceImpl();
    }

    @POST
    @Path("/setup")
    public Response setupTotp() throws SQLException {

        HttpSession session
                = httpRequest.getSession(false);

        if (session == null) {
            throw new UnauthorizedException(
                    "Login required."
            );
        }

        Object userIdAttribute
                = session.getAttribute("userId");

        if (userIdAttribute == null) {
            throw new UnauthorizedException(
                    "Login required."
            );
        }

        int userId = (Integer) userIdAttribute;

        TotpSetupResponse response
                = totpService.setupTotp(userId);

        return Response.ok(response).build();
    }

    @POST
    @Path("/setup/verify")
    public Response verifySetupCode(TotpVerifyRequest request) throws SQLException {

        HttpSession session
                = httpRequest.getSession(false);

        if (session == null) {
            throw new UnauthorizedException(
                    "Login required."
            );
        }

        Object userIdAttribute = session.getAttribute("userId");
        boolean recoveryLogin = userIdAttribute == null
                && isVerifiedRecoverySession(session);

        if (userIdAttribute == null && !recoveryLogin) {
            throw new UnauthorizedException("Login required.");
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Verification request is required."
            );
        }

        int userId = recoveryLogin
                ? (Integer) session.getAttribute("totpRecoveryUserId")
                : (Integer) userIdAttribute;

        boolean verified = totpService.verifySetupCode(userId, request.getCode());

        if (!verified) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(
                            "INVALID_TOTP_CODE",
                            "Invalid TOTP code."
                    ))
                    .build();
        }

        if (recoveryLogin) {
            session.removeAttribute("pendingTotpUserId");
            session.removeAttribute("totpRecoveryUserId");
            session.removeAttribute("totpRecoveryVerified");
            session.setAttribute("userId", userId);
        }

        return Response
                .ok(new MessageResponse(
                        "TOTP enabled successfully."
                ))
                .build();
    }

    @POST
    @Path("/disable")
    public Response disableTotp(TotpVerifyRequest request) throws SQLException {

        HttpSession session
                = httpRequest.getSession(false);

        if (session == null) {
            throw new UnauthorizedException(
                    "Login required."
            );
        }

        Object userIdAttribute
                = session.getAttribute("userId");

        if (userIdAttribute == null) {
            throw new UnauthorizedException(
                    "Login required."
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "TOTP verification request is required."
            );
        }

        int userId
                = (Integer) userIdAttribute;

        boolean disabled = totpService.disableTotp(userId, request.getCode());

        if (!disabled) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(
                            "INVALID_TOTP_CODE",
                            "Invalid TOTP code."
                    ))
                    .build();
        }

        return Response
                .ok(new MessageResponse(
                        "TOTP disabled successfully."
                ))
                .build();
    }

    @POST
    @Path("/regenerate")
    public Response regenerateTotp()
            throws SQLException {

        HttpSession session
                = httpRequest.getSession(false);

        if (session == null) {
            throw new UnauthorizedException(
                    "Login required."
            );
        }

        int userId = getTotpOperationUserId(session);

        TotpSetupResponse response
                = totpService.regenerateTotp(userId);

        return Response.ok(response).build();
    }

    private int getTotpOperationUserId(HttpSession session) {
        Object userIdAttribute = session.getAttribute("userId");
        if (userIdAttribute instanceof Integer) {
            return (Integer) userIdAttribute;
        }

        if (isVerifiedRecoverySession(session)) {
            return (Integer) session.getAttribute("totpRecoveryUserId");
        }

        throw new UnauthorizedException("Login required.");
        }

    private boolean isVerifiedRecoverySession(HttpSession session) {
        Object recoveryUserId = session.getAttribute("totpRecoveryUserId");
        Object recoveryVerified = session.getAttribute("totpRecoveryVerified");
        Object pendingUserId = session.getAttribute("pendingTotpUserId");

        return recoveryUserId instanceof Integer
                && Boolean.TRUE.equals(recoveryVerified)
                && pendingUserId instanceof Integer
                && recoveryUserId.equals(pendingUserId);
        }

    @POST
    @Path("/login/verify")
    public Response verifyLoginCode(TotpVerifyRequest request) throws SQLException {

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            throw new UnauthorizedException("TOTP login session not found.");
        }

        Integer userId = (Integer) session.getAttribute("pendingTotpUserId");

        if (userId == null) {
            throw new UnauthorizedException("TOTP verification is not pending.");
        }

        if (request == null) {
            throw new IllegalArgumentException("TOTP verification request is required.");
        }

        boolean valid = totpService.verifyLoginCode(userId, request.getCode());

        if (!valid) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("INVALID_TOTP_CODE", "Invalid TOTP code."))
                    .build();
        }

        /*
     * TOTP is correct.
     * Convert the pending login into a fully authenticated session.
         */
        session.removeAttribute("pendingTotpUserId");
        session.setAttribute("userId", userId);

        return Response.ok(new MessageResponse("Login successful.")).build();
    }

}
