package com.movie_booking.resource;

import java.sql.SQLException;
import java.time.LocalDate;
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

import com.movie_booking.dto.request.ShowCreateRequest;
import com.movie_booking.dto.request.ShowUpdateRequest;
import com.movie_booking.dto.response.CreateResponse;
import com.movie_booking.dto.response.ShowResponse;
import com.movie_booking.exception.ResourceNotFoundException;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.exception.ValidationException;
import com.movie_booking.model.Show;
import com.movie_booking.service.ShowService;
import com.movie_booking.service.ShowServiceImpl;

@Path("/shows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ShowResource {

    private final ShowService showService;

    public ShowResource() {
        this.showService = new ShowServiceImpl();
    }


    @GET
    @Path("/")
    public List<ShowResponse> getShows() throws SQLException {
        List<Show> shows = showService.getAllShows();
        return shows.stream()
                .map(show -> toShowResponse(show))
                .collect(Collectors.toList());
    }

    @GET

    @Path("/{showId}")
    public ShowResponse getShowById(@PathParam("showId") int showId) throws SQLException {
        Show show = showService.getShowById(showId);
        if (show == null) {
            throw new ResourceNotFoundException("Show with ID " + showId + " not found.");
        }
        return toShowResponse(show);
    }

    @GET
    @Path("/movie/{movieId}")
    public List<ShowResponse> getShowsByMovieId(@PathParam("movieId") int movieId) throws SQLException {
        List<Show> shows = showService.getShowsByMovieId(movieId);
        return shows.stream()
                .map(show -> toShowResponse(show))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/movie/{movieId}/date/{date}")
    public List<ShowResponse> getShowsByMovieAndDate(@PathParam("movieId") int movieId, @PathParam("date") String date) throws java.sql.SQLException {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ValidationException("Invalid date format. Expected format: YYYY-MM-DD");
        }
        List<Show> shows = showService.getShowsByMovieAndDate(movieId, localDate);
        return shows.stream()
                .map(show -> toShowResponse(show))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/date/{date}")
    public List<ShowResponse> getShowsByDate(@PathParam("date") String date) throws java.sql.SQLException {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ValidationException("Invalid date format. Expected format: YYYY-MM-DD");
        }
        List<Show> shows = showService.getShowsByDate(localDate);
        return shows.stream()
                .map(show -> toShowResponse(show))
                .collect(Collectors.toList());
    }


    @POST
    @Path("/")
    public CreateResponse addShow(ShowCreateRequest request) throws java.sql.SQLException {
        Show show = toShow(request);
        int showId = showService.addShow(show, getAuthenticatedUserId());
        return new CreateResponse(showId, "Show added successfully");
    }

    @PUT
    @Path("/{showId}")
    public CreateResponse updateShow(@PathParam("showId") int showId, ShowUpdateRequest request) throws java.sql.SQLException {
        Show show = toShow(request);
        show.setShowId(showId);
        boolean updated = showService.updateShow(show, getAuthenticatedUserId());
        if (!updated) {
            throw new ResourceNotFoundException("Show with ID " + showId + " not found.");
        }
        return new CreateResponse(showId, "Show updated successfully");
    }


    @DELETE
    @Path("/{showId}")
    public CreateResponse cancelShow(@PathParam("showId") int showId) throws java.sql.SQLException {
        boolean deleted = showService.cancelShow(showId, getAuthenticatedUserId());
        if (!deleted) {
            throw new ResourceNotFoundException("Show with ID " + showId + " not found.");
        }
        return new CreateResponse(showId, "Show deleted successfully");
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

    private ShowResponse toShowResponse(Show show) {
        ShowResponse response = new ShowResponse();
        response.setShowId(show.getShowId());
        response.setMovieId(show.getMovieId());
        response.setScreenId(show.getScreenId());
        response.setStartTime(show.getStartTime());
        response.setEndTime(show.getEndTime());
        response.setStatus(show.getStatus());
        return response;
    }

    private Show toShow(ShowCreateRequest request) {
        Show show = new Show();
        show.setMovieId(request.getMovieId());
        show.setScreenId(request.getScreenId());
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        show.setRegularPrice(request.getRegularPrice());
        show.setPremiumPrice(request.getPremiumPrice());
        show.setReclinerPrice(request.getReclinerPrice());

        return show;
    }

    private Show toShow(ShowUpdateRequest request) {
        Show show = new Show();
        show.setShowId(request.getShowId());
        show.setMovieId(request.getMovieId());
        show.setScreenId(request.getScreenId());
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        return show;
    }


}
