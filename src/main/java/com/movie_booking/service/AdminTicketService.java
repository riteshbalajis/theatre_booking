package com.movie_booking.service;

import java.sql.SQLException;

import com.movie_booking.dto.response.TicketCheckInResponse;

public interface AdminTicketService {

    TicketCheckInResponse checkInTicket(
            int adminUserId,
            String ticketCode
    ) throws SQLException;
}
