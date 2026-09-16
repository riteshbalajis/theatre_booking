package com.movie_booking.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.movie_booking.dto.response.ErrorResponse;

@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException exception) {
        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                exception.getMessage()
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}