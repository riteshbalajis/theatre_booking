package com.movie_booking.resource;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.movie_booking.dto.response.ShowSeatCountResponse;
import com.movie_booking.dto.response.ShowSeatResponse;
import com.movie_booking.exception.ResourceNotFoundException;
import com.movie_booking.model.Seat;
import com.movie_booking.model.ShowSeat;
import com.movie_booking.service.SeatService;
import com.movie_booking.service.SeatServiceImpl;
import com.movie_booking.service.ShowSeatService;
import com.movie_booking.service.ShowSeatServiceImpl;


@Path("/show-seats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ShowSeatResource {

    private final ShowSeatService showSeatService;
    private final SeatService seatService;

    public ShowSeatResource() {
        this.showSeatService = new ShowSeatServiceImpl();
        this.seatService = new SeatServiceImpl();
    }


    @GET
    @Path("/show/{showId}/available")
    public List<ShowSeatResponse> getAvailableShowSeatsByShowId(@PathParam("showId") int showId) throws SQLException {
        List<ShowSeat> showSeats = showSeatService.getAvailableShowSeatsByShowId(showId);
        return showSeats.stream()
                .map(showSeat -> toShowSeatResponse(showSeat))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/show/{showId}/booked")
    public List<ShowSeatResponse> getBookedShowSeatsByShowId(@PathParam("showId") int showId) throws SQLException {
        List<ShowSeat> showSeats = showSeatService.getBookedShowSeatsByShowId(showId);
        return showSeats.stream()
                .map(showSeat -> toShowSeatResponse(showSeat))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/show/{showId}/count")
    public ShowSeatCountResponse getShowSeatCountByShowId(@PathParam("showId") int showId) throws SQLException {
        ShowSeatCountResponse response = new ShowSeatCountResponse();
        response.setShowId(showId);
        response.setAvailableSeatCount(showSeatService.getAvailableSeatCountByShowId(showId));
        return response;
    }

    @GET
    @Path("/show/{showId}")
    public List<ShowSeatResponse> getShowSeatsByShowId(@PathParam("showId") int showId) throws java.sql.SQLException {
        List<ShowSeat> showSeats = showSeatService.getShowSeatsByShowId(showId);
        return showSeats.stream()
                .map(showSeat -> toShowSeatResponse(showSeat))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{showSeatId}")
    public ShowSeatResponse getShowSeatById(@PathParam("showSeatId") int showSeatId) throws java.sql.SQLException {
        ShowSeat showSeat = showSeatService.getShowSeatById(showSeatId);
        if (showSeat == null) {
            throw new ResourceNotFoundException("Show seat not found with ID: " + showSeatId);
        }
        return toShowSeatResponse(showSeat);
    }

    private ShowSeatResponse toShowSeatResponse(ShowSeat showSeat) {
        Seat seat = null;
        try {
            seat = seatService.getSeatById(showSeat.getSeatId());
        } catch (SQLException ignored) {
            // leave seat metadata empty if lookup fails
        }

        ShowSeatResponse response = new ShowSeatResponse();
        response.setShowSeatId(showSeat.getShowSeatId());
        response.setShowId(showSeat.getShowId());
        response.setSeatId(showSeat.getSeatId());
        response.setRowLabel(seat == null ? null : seat.getRowLabel());
        response.setSeatNumber(seat == null ? 0 : seat.getSeatNumber());
        response.setSeatType(seat == null || seat.getSeatType() == null ? null : seat.getSeatType().name());
        response.setPrice(showSeat.getPrice());
        response.setStatus(showSeat.getStatus());
        return response;
    }


    
}
