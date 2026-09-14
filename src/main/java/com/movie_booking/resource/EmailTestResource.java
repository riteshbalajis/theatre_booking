package com.movie_booking.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.movie_booking.service.EmailService;

@Path("/email")
public class EmailTestResource {

    private final EmailService emailService = new EmailService();

    @GET
    @Path("/test")
    public Response testEmail(
            @QueryParam("to") String recipient) {

        if (recipient == null || recipient.isBlank()) {

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(
                    "Recipient email is required"
            ).build();
        }

        emailService.sendTestEmail(recipient);

        return Response.ok(
                "Test email request processed"
        ).build();
    }
}
