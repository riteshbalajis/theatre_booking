package com.movie_booking.resource;

import javax.ws.rs.Path;

import com.movie_booking.service.EmailService;

@Path("/email")
public class EmailTestResource {

    private final EmailService emailService = new EmailService();

    
}
