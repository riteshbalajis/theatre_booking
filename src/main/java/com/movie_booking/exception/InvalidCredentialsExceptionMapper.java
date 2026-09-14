package com.movie_booking.exception;

import com.movie_booking.dto.response.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidCredentialsExceptionMapper
        implements ExceptionMapper<InvalidCredentialsException> {

    @Override
    public Response toResponse(
            InvalidCredentialsException exception) {

        ErrorResponse error = new ErrorResponse(
                "INVALID_CREDENTIALS",
                exception.getMessage()
        );

        return Response
                .status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}