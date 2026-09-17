package com.movie_booking.resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.request.GoogleLoginRequest;
import com.movie_booking.dto.request.LoginRequest;
import com.movie_booking.dto.request.RegisterRequest;
import com.movie_booking.dto.request.RegistrationVerifyRequest;
import com.movie_booking.dto.response.LoginResponse;
import com.movie_booking.dto.response.RegistrationResponse;
import com.movie_booking.dto.response.UserResponse;
import com.movie_booking.service.RegistrationService;
import com.movie_booking.service.RegistrationServiceImpl;
import com.movie_booking.service.TotpService;
import com.movie_booking.service.TotpServiceImpl;
import com.movie_booking.service.UserService;
import com.movie_booking.service.UserServiceImpl;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class AuthResource {

    private final UserService userService;
    private final TotpService totpService;
        private final RegistrationService registrationService;

    @Context
    private HttpServletRequest httpRequest;

    public AuthResource() {
        this.userService = new UserServiceImpl();
        this.totpService = new TotpServiceImpl();
        this.registrationService = new RegistrationServiceImpl();
    }

    @GET
    @Path("/session")
    public Response getCurrentSession() throws SQLException {

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "message", "User is not logged in."
                    ))
                    .build();
        }

        Object userIdObject = session.getAttribute("userId");

        if (userIdObject == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "message", "User is not logged in."
                    ))
                    .build();
        }

        int userId = (Integer) userIdObject;

        UserResponse user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "message", "User session is invalid."
                    ))
                    .build();
        }

        return Response.ok(user).build();
    }

    @POST
    @Path("/login")
    public LoginResponse login(LoginRequest request) throws SQLException {

        LoginResponse response = userService.login(request);
        int userId = response.getUser().getUserId();
        boolean totpEnabled = totpService.isTotpEnabled(userId);

        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = httpRequest.getSession(true);

        if (totpEnabled) {
            session.setAttribute("pendingTotpUserId", userId);
            response.setTotpRequired(true);
            response.setMessage("TOTP verification required.");
            return response;
        }

        session.setAttribute("userId", userId);
        response.setTotpRequired(false);

        return response;
    }

    @POST
    @Path("/forgot_password")
    public Response forgotPassword(Map<String, String> request) {

        try {

            String email = request.get("email");

            boolean result = userService.forgotPassword(email);

            if (!result) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of(
                                "message",
                                "No account found with this email."
                        ))
                        .build();
            }

            return Response.ok(
                    Map.of(
                            "message",
                            "Password reset OTP generated successfully."
                    )
            ).build();

        } catch (SQLException e) {

            e.printStackTrace();

            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR
            ).entity(Map.of(
                    "message",
                    "Unable to process password reset request."
            )).build();
        }
    }

    @POST
    @Path("/verify_reset_otp")
    public Response verifyResetOtp(Map<String, String> request) {

        try {

            String email = request.get("email");
            String otp = request.get("otp");

            String resetToken
                    = userService.verifyResetOtp(email, otp);

            return Response.ok(
                    Map.of(
                            "message",
                            "OTP verified successfully.",
                            "resetToken",
                            resetToken
                    )
            ).build();

        } catch (IllegalArgumentException e) {

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            ).build();

        } catch (IllegalStateException e) {

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            ).build();

        } catch (SQLException e) {

            e.printStackTrace();

            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR
            ).entity(
                    Map.of(
                            "message",
                            "Unable to verify OTP."
                    )
            ).build();
        }
    }

    @POST
    @Path("/reset_password")
    public Response resetPassword(Map<String, String> request) {

        try {

            String email = request.get("email");
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");

            boolean reset
                    = userService.resetPassword(
                            email,
                            resetToken,
                            newPassword
                    );

            if (reset) {

                return Response.ok(
                        Map.of(
                                "message",
                                "Password reset successfully."
                        )
                ).build();
            }

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(
                    Map.of(
                            "message",
                            "Unable to reset password."
                    )
            ).build();

        } catch (IllegalArgumentException e) {

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            ).build();

        } catch (IllegalStateException e) {

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            ).build();

        } catch (SQLException e) {

            e.printStackTrace();

            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR
            ).entity(
                    Map.of(
                            "message",
                            "Unable to reset password."
                    )
            ).build();
        }
    }

    @POST
    @Path("/google")
    public LoginResponse googleLogin(GoogleLoginRequest request)
            throws SQLException, GeneralSecurityException, IOException {

        LoginResponse response
                = userService.loginWithGoogle(request.getCredential());

        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = httpRequest.getSession(true);
        newSession.setAttribute("userId", response.getUser().getUserId());

        System.out.println(">>> GOOGLE LOGIN ENDPOINT REACHED");

        return response;
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request)
                        throws SQLException {
                try {
            return Response.ok(
                    registrationService.startRegistration(request))
                    .build();
                } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "message", exception.getMessage()))
                    .build();
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "message", "Unable to start registration."))
                    .build();
                }
        }

        @POST
        @Path("/register/verify")
        public Response verifyRegistration(RegistrationVerifyRequest request) {
                try {
                        RegistrationResponse response
                                        = registrationService.verifyRegistration(request);
                        return Response.ok(response).build();
                } catch (IllegalArgumentException exception) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(Map.of(
                                                        "message", exception.getMessage()))
                                        .build();
                } catch (IllegalStateException exception) {
                        return Response.status(Response.Status.CONFLICT)
                                        .entity(Map.of(
                                                        "message", exception.getMessage()))
                                        .build();
                } catch (SQLException exception) {
                        exception.printStackTrace();
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(Map.of(
                                                        "message", "Unable to verify registration."))
                                        .build();
                }
    }

    @POST
    @Path("/logout")
    public Response logout() {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Response
                .status(Response.Status.NO_CONTENT)
                .build();

    }

}
