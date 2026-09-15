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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.request.TheatreCreateRequest;
import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.TheatreCreateResponse;
import com.movie_booking.dto.response.TheatreResponse;
import com.movie_booking.exception.TheatreNotFoundException;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.model.Theatre;
import com.movie_booking.service.TheatreService;
import com.movie_booking.service.TheatreServiceImpl;

@Path("/theatres")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class TheatreResource {

    private TheatreService theatreService;

    public TheatreResource() {
        this.theatreService = new TheatreServiceImpl();
    }

    
    @GET
    @Path("/")
    public List<TheatreResponse> getTheatres(
            @QueryParam("keyword") String keyword) throws SQLException {

        List<Theatre> theatres;

        if (keyword == null || keyword.isBlank()) {
            theatres = theatreService.getAllTheatres();
        } else {
            theatres = theatreService.searchTheatres(keyword);
        }

        return theatres.stream()
                .map(theatre -> toTheatreResponse(theatre))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{theatreId}")
    public TheatreResponse getTheatreById(@PathParam("theatreId") int theatreId) throws java.sql.SQLException {
        Theatre theatre = theatreService.getTheatreById(theatreId);
        if (theatre == null) {
            throw new TheatreNotFoundException("Theatre not found with ID: " + theatreId);
        }
        return toTheatreResponse(theatre);
    }



    @POST
    @Path("/")
    public TheatreCreateResponse createTheatre(TheatreCreateRequest request) throws java.sql.SQLException {
        Theatre theatre = toTheatre(request);

        int theatreId = theatreService.addTheatre(theatre,getAuthenticatedUserId());
        return new TheatreCreateResponse(theatreId, "Theatre created successfully with ID: " + theatreId);
    }

    @PUT
    @Path("/{theatreId}")
    public TheatreCreateResponse updateTheatre(@PathParam("theatreId") int theatreId, TheatreCreateRequest request) throws java.sql.SQLException {
        Theatre theatre = toTheatre(request);
        theatre.setTheatreId(theatreId);

        boolean updated = theatreService.updateTheatre(theatre,getAuthenticatedUserId());
        if (!updated) {
            throw new TheatreNotFoundException("Theatre not found with ID: " + theatreId);
        }
        return new TheatreCreateResponse(theatre.getTheatreId(), "Theatre updated successfully with ID: " + theatre.getTheatreId());
    }

    @DELETE
    @Path("/{theatreId}")
    public Response deleteTheatre(@PathParam("theatreId") int theatreId) throws java.sql.SQLException {
        boolean deleted = theatreService.deactivateTheatre(theatreId,getAuthenticatedUserId());
        if (!deleted) {
            throw new TheatreNotFoundException("Theatre not found with ID: " + theatreId);
        }
        return Response.ok(new MessageResponse("Theatre deleted successfully.")).build();
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
    


    public TheatreResponse toTheatreResponse(Theatre theatre) {
        TheatreResponse response = new TheatreResponse();
        response.setTheatreId(theatre.getTheatreId());
        response.setName(theatre.getName());
        response.setLocation(theatre.getLocation());
        response.setStatus(theatre.getStatus());
        response.setCreatedAt(theatre.getCreatedAt());
        return response;
    }

    public Theatre toTheatre(TheatreCreateRequest request) {
        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setLocation(request.getLocation());
        return theatre;
    }

    
    
}
