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

import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.MovieResponse;
import com.movie_booking.exception.MovieNotFoundException;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.model.Movie;
import com.movie_booking.service.MovieService;
import com.movie_booking.service.MovieServiceImpl;


@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class MovieResource {


    private final MovieService movieService;

    public MovieResource() {
        this.movieService = new MovieServiceImpl();
    }


    //GET Methods 
    @GET
    @Path("/")
    public List<MovieResponse> getMovies(
            @QueryParam("keyword") String keyword) throws SQLException {

        List<Movie> movies;

        if (keyword == null || keyword.isBlank()) {
            movies = movieService.getAllMovies();
        } else {
            movies = movieService.searchMovies(keyword);
        }

        return movies.stream()
                .map(movie -> toResponse(movie))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{movieId}")
    public MovieResponse getMovieById(@PathParam("movieId") int movieId) throws java.sql.SQLException {
        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            throw new MovieNotFoundException("Movie not found with ID: " + movieId);
        }
        return toResponse(movie);
        
    }


    //POST Methods

    @POST
    @Path("/")
    public Response addMovie(Movie movie) throws java.sql.SQLException {
        int movieId = movieService.addMovie(movie, getAuthenticatedUserId());

        return Response.status(Response.Status.CREATED)
                .entity(toResponse(movieService.getMovieById(movieId)))
                .build();
    }

    @POST
    @Path("/{movieId}/activate")
    public Response activateMovie(@PathParam("movieId") int movieId) throws java.sql.SQLException {
        if (!movieService.activateMovie(movieId, getAuthenticatedUserId())) {
            throw new MovieNotFoundException("Movie not found with ID: " + movieId);
        }
        return Response
            .ok(new MessageResponse("Movie activated successfully."))
            .build();
    }

    @POST
    @Path("/{movieId}/deactivate")
    public Response deactivateMovie(@PathParam("movieId") int movieId) throws java.sql.SQLException {
        if (!movieService.deactivateMovie(movieId, getAuthenticatedUserId())) {
            throw new MovieNotFoundException("Movie not found with ID: " + movieId);
        }
        return Response
            .ok(new MessageResponse("Movie deactivated successfully."))
            .build();
    }

    //put

    @PUT
    @Path("/{movieId}")
    public Response updateMovie(@PathParam("movieId") int movieId, Movie movie) throws java.sql.SQLException {
        movie.setMovieId(movieId);
        if (!movieService.updateMovie(movie, getAuthenticatedUserId())) {
            throw new MovieNotFoundException("Movie not found with ID: " + movieId);
        }
        return Response
            .ok(new MessageResponse("Movie updated successfully."))
            .build();
    }

    //delete 

    @DELETE
    @Path("/{movieId}")
    public Response deleteMovie(@PathParam("movieId") int movieId) throws java.sql.SQLException {
        if(!movieService.deactivateMovie(movieId, getAuthenticatedUserId())) {
            throw new MovieNotFoundException("Movie not found with ID: " + movieId);
        }
        return Response
            .ok(new MessageResponse("Movie deleted successfully."))
            .build();
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
    



    private MovieResponse toResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setMovieId(movie.getMovieId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setLanguage(movie.getLanguage());
        response.setGenre(movie.getGenre());
        response.setReleaseDate(movie.getReleaseDate());
        response.setStatus(movie.getStatus());
        return response;
    }


    
}
