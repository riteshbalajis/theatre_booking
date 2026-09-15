package com.movie_booking.resource;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.request.BookTicketsRequest;
import com.movie_booking.dto.request.PaymentRequest;
import com.movie_booking.dto.request.RazorpayPaymentRequest;
import com.movie_booking.dto.response.BookingCreatedResponse;
import com.movie_booking.dto.response.BookingResponse;
import com.movie_booking.dto.response.BookingSeatResponse;
import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.RazorpayOrderResponse;
import com.movie_booking.exception.ResourceNotFoundException;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.model.Booking;
import com.movie_booking.model.BookingSeat;
import com.movie_booking.model.Seat;
import com.movie_booking.model.ShowSeat;
import com.movie_booking.service.BookingService;
import com.movie_booking.service.BookingServiceImpl;
import com.movie_booking.service.SeatService;
import com.movie_booking.service.SeatServiceImpl;
import com.movie_booking.service.ShowSeatService;
import com.movie_booking.service.ShowSeatServiceImpl;

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    private final BookingService bookingService;
    private final ShowSeatService showSeatService;
    private final SeatService seatService;

    public BookingResource() {
        this.bookingService = new BookingServiceImpl();
        this.showSeatService = new ShowSeatServiceImpl();
        this.seatService = new SeatServiceImpl();
    }

    @GET
    public List<BookingResponse> getBookings() throws SQLException {
        int userId = getAuthenticatedUserId();
        List<Booking> bookings = bookingService.getBookingsForUser(userId);
        return bookings.stream()
                .map(booking -> toBookingResponse(booking))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/")
    public List<BookingResponse> getBookingsWithTrailingSlash() throws SQLException {
        return getBookings();
    }

    @GET
    @Path("/{bookingId}")
    public BookingResponse getBookingById(@PathParam("bookingId") int bookingId) throws SQLException {
        int userId = getAuthenticatedUserId();
        Booking booking = bookingService.getBookingById(userId, bookingId);
        if (booking == null) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }
        return toBookingResponse(booking);
    }

    @POST
    public BookingCreatedResponse createBooking(BookTicketsRequest request) throws java.sql.SQLException{

        int userId = getAuthenticatedUserId();
        int bookingId = bookingService.createBookingWithSeats(userId,request.getShowId(),request.getShowSeatIds());
        
        Booking booking = bookingService.getBookingById(userId, bookingId);
        
        return new BookingCreatedResponse(bookingId, "Payment Pending", booking.getTotalAmount(), booking.getHoldUntil());


    }

    @POST
    @Path("/{bookingId}/payment")
    public Response confirmPayment(@PathParam("bookingId") int bookingId, PaymentRequest request) throws SQLException {
        int userId = getAuthenticatedUserId();

        try {
            bookingService.confirmPayment(userId, bookingId, request.getPin());

            return Response.ok(new MessageResponse("Payment successful. Booking confirmed.")).build();

        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(exception.getMessage()))
                    .build();

        } catch (IllegalStateException exception) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new MessageResponse(exception.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{bookingId}/razorpay_order")
    public RazorpayOrderResponse createRazorpayOrder(
            @PathParam("bookingId") int bookingId) throws SQLException {

        int userId = getAuthenticatedUserId();

        return bookingService.createRazorpayOrder(userId, bookingId);
    }

    @POST
    @Path("/{bookingId}/razorpay_verify")
    public void verifyRazorpayPayment(
            @PathParam("bookingId") int bookingId,
            RazorpayPaymentRequest request) throws SQLException {

        int userId = getAuthenticatedUserId();

        bookingService.verifyRazorpayPayment(
                userId,
                bookingId,
                request);
    }

    @DELETE
    @Path("{booking_id}")
    public Response cancelBooking(@PathParam("booking_id") int bookingId) throws java.sql.SQLException {

        int userId = getAuthenticatedUserId();
        if(!bookingService.cancelBooking(userId,bookingId)){
            throw new ResourceNotFoundException("Booking not Found with id "+bookingId);

        }
        return Response
            .ok(new MessageResponse("Booking Cancelled successfully."))
            .build();
    }


    private BookingResponse toBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setUserId(booking.getUserId());
        response.setShowId(booking.getShowId());
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus());
        response.setBookedAt(booking.getBookedAt());

        try {
            List<BookingSeat> bookingSeats = bookingService.getBookingSeats(booking.getUserId(), booking.getBookingId());
            response.setSeats(bookingSeats.stream()
        .map(bookingSeat -> toBookingSeatResponse(bookingSeat))
        .collect(Collectors.toList()));

        } catch (SQLException ignored) {
            response.setSeats(List.of());
        }

        return response;
    }

    private BookingSeatResponse toBookingSeatResponse(BookingSeat bookingSeat) {
        BookingSeatResponse response = new BookingSeatResponse();
        response.setBookingSeatId(bookingSeat.getBookingSeatId());
        response.setBookingId(bookingSeat.getBookingId());
        response.setShowSeatId(bookingSeat.getShowSeatId());
        response.setPrice(bookingSeat.getPrice());

        try {
            ShowSeat showSeat = showSeatService.getShowSeatById(bookingSeat.getShowSeatId());
            if (showSeat != null) {
                Seat seat = seatService.getSeatById(showSeat.getSeatId());
                if (seat != null) {
                    response.setRowLabel(seat.getRowLabel());
                    response.setSeatNumber(seat.getSeatNumber());
                }
            }
        } catch (SQLException ignored) {
            
            
        }

        return response;
    }

    @Context
    private HttpServletRequest httpRequest;

    private int getAuthenticatedUserId() {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthorizedException("User is not logged in.");
        }
        return (int) session.getAttribute("userId");
    }
}
