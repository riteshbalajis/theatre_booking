package com.movie_booking.exception;

import java.sql.SQLException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.movie_booking.dto.response.ErrorResponse;

@Provider
public class SQLExceptionMapper
        implements ExceptionMapper<SQLException> {

    @Override
    public Response toResponse(
            SQLException exception) {

        exception.printStackTrace();

        ErrorResponse error =
                new ErrorResponse(
                        "DATABASE_ERROR",
                        "A database error occurred."
                );

        return Response
                .status(
                        Response.Status.INTERNAL_SERVER_ERROR
                )
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}