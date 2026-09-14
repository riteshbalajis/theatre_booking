package com.movie_booking.exception;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.movie_booking.dto.response.ErrorResponse;

@Provider
public class UnauthorizedExceptionMapper
        implements ExceptionMapper<UnauthorizedException> {

    @Override
    public Response toResponse(UnauthorizedException exception) {

        ErrorResponse error = new ErrorResponse(
                "UNAUTHORIZED",
                exception.getMessage()
        );

        return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}