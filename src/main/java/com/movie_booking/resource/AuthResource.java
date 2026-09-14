package com.movie_booking.resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.request.GoogleLoginRequest;
import com.movie_booking.dto.request.LoginRequest;
import com.movie_booking.dto.request.RegisterRequest;
import com.movie_booking.dto.response.LoginResponse;
import com.movie_booking.dto.response.RegisterResponse;
import com.movie_booking.service.UserService;
import com.movie_booking.service.UserServiceImpl;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class AuthResource {

    private final UserService userService;

    @Context
    private HttpServletRequest httpRequest;

    public AuthResource() {
        this.userService = new UserServiceImpl();
    }

    @POST
    @Path("/login")
    public LoginResponse login(LoginRequest request) throws java.sql.SQLException {
        LoginResponse response = userService.login(request);
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = httpRequest.getSession(true);
        newSession.setAttribute("userId", response.getUser().getUserId());

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
    public RegisterResponse register(RegisterRequest request) throws java.sql.SQLException {
        return userService.registerUser(request);
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
