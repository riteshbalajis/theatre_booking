package com.movie_booking.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.request.SeatCreateRequest;
import com.movie_booking.dto.request.SeatUpdateRequest;
import com.movie_booking.dto.response.CreateResponse;
import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.SeatResponse;
import com.movie_booking.exception.ResourceNotFoundException;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.model.Seat;
import com.movie_booking.service.SeatService;
import com.movie_booking.service.SeatServiceImpl;

@Path("/seats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class SeatResource {

    private final SeatService seatService;

    public SeatResource() {
        this.seatService = new SeatServiceImpl();
    }

    @GET
    @Path("/screen/{screenId}")
    public List<SeatResponse> getSeatsByScreenId(@PathParam("screenId") int screenId) throws java.sql.SQLException {
        List<Seat> seats = seatService.getSeatsByScreenId(screenId);
        return seats.stream()
                .map(seat -> toSeatResponse(seat))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/screens/{screenId}")
    public List<SeatResponse> getSeatsByScreenIdLegacy(@PathParam("screenId") int screenId) throws java.sql.SQLException {
        return getSeatsByScreenId(screenId);
    }

    @GET
    @Path("/{seatId}")
    public SeatResponse getSeatById(@PathParam("seatId") int seatId) throws java.sql.SQLException {
        Seat seat = seatService.getSeatById(seatId);
        if (seat == null) {
            throw new ResourceNotFoundException("Seat not found");
        }
        return toSeatResponse(seat);
    }

    @POST
    @Path("/")
    public CreateResponse addSeat(SeatCreateRequest seatCreateRequest, @Context HttpServletRequest request) throws java.sql.SQLException {
        int authenticatedUserId = getAuthenticatedUserId();
        Seat seat = toSeat(seatCreateRequest);
        
        int newSeatId = seatService.addSeat(seat, authenticatedUserId);
        return new CreateResponse(newSeatId, "Seat created successfully");
    }

    @PUT
    @Path("/{seatId}")
    public Response updateSeat(@PathParam("seatId") int seatId, SeatUpdateRequest seatUpdateRequest) throws java.sql.SQLException {
        int authenticatedUserId = getAuthenticatedUserId();
        Seat seat = toSeat(seatUpdateRequest);
        seatService.updateSeat(seat, authenticatedUserId);
        if (seatService.getSeatById(seatId) == null) {
            throw new ResourceNotFoundException("Seat not found");
        }
        return Response.ok(new MessageResponse("Seat updated successfully")).build();
    }


    @DELETE
    @Path("/{seatId}")
    public Response deactivateSeat(@PathParam("seatId") int seatId) throws java.sql.SQLException {
        int authenticatedUserId = getAuthenticatedUserId();
        boolean deleted = seatService.deactivateSeat(seatId, authenticatedUserId);
        if (!deleted) {
            throw new ResourceNotFoundException("Seat not found");
        }
        return Response.ok(new MessageResponse("Seat deactivated successfully")).build();
    }

    public SeatResponse toSeatResponse(Seat seat) {
        SeatResponse seatResponse = new SeatResponse();
        seatResponse.setSeatId(seat.getSeatId());
        seatResponse.setScreenId(seat.getScreenId());
        seatResponse.setRowLabel(seat.getRowLabel());
        seatResponse.setSeatNumber(seat.getSeatNumber());
        seatResponse.setSeatType(seat.getSeatType());
        seatResponse.setStatus(seat.getStatus());
        return seatResponse;
    }

    public Seat toSeat(SeatCreateRequest seatRequest) {
        Seat seat = new Seat();
        seat.setScreenId(seatRequest.getScreenId());
        seat.setRowLabel(seatRequest.getRowLabel());
        seat.setSeatNumber(seatRequest.getSeatNumber());
        seat.setSeatType(seatRequest.getSeatType());
       
        return seat;
    }
    public Seat toSeat(SeatUpdateRequest seatRequest) {
        Seat seat = new Seat();
        seat.setSeatId(seatRequest.getSeatId());
        seat.setRowLabel(seatRequest.getRowLabel());
        seat.setSeatNumber(seatRequest.getSeatNumber());
        seat.setSeatType(seatRequest.getSeatType());
        return seat;
    }


    @Context
    private HttpServletRequest httpRequest;

    private int getAuthenticatedUserId(){
        
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthorizedException("User is not logged in.");
        }
        return (int) session.getAttribute("userId");
    }

    


    
}
