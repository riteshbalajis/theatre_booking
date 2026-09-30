package com.movie_booking.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.MovieResponse;
import com.movie_booking.exception.MovieNotFoundException;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.model.Movie;
import com.movie_booking.service.AuditService;
import com.movie_booking.service.MovieService;
import com.movie_booking.service.MovieServiceImpl;

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class MovieResource {

    private final MovieService movieService;
    private final AuditService auditService;

    @Context
    private HttpServletRequest httpRequest;

    public MovieResource() {
        this.movieService = new MovieServiceImpl();
        this.auditService = new AuditService();
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

    @GET
    @Path("/{movieId}/poster")
    @Produces("image/*")
    public Response getMoviePoster(@PathParam("movieId") int movieId) throws SQLException, IOException {

        //Path posterPath = movieService.getMoviePoster(movieId);
        java.nio.file.Path posterPath
                = movieService.getMoviePoster(movieId);

        String contentType = Files.probeContentType(posterPath);

        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return Response.ok(posterPath.toFile())
                .type(contentType)
                .build();
    }

    //POST Methods
    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMovie(
            @FormDataParam("title") String title,
            @FormDataParam("description") String description,
            @FormDataParam("durationMinutes") int durationMinutes,
            @FormDataParam("language") String language,
            @FormDataParam("genre") String genre,
            @FormDataParam("releaseDate") String releaseDate,
            @FormDataParam("poster") InputStream posterInputStream,
            @FormDataParam("poster") FormDataContentDisposition posterDetails) {

        try {
            Movie movie = new Movie();

            movie.setTitle(title);
            movie.setDescription(description);
            movie.setDurationMinutes(durationMinutes);
            movie.setLanguage(language);
            movie.setGenre(genre);
            movie.setReleaseDate(LocalDate.parse(releaseDate));

            if (posterInputStream != null && posterDetails != null) {
                String posterFileName = movieService.saveMoviePoster(posterInputStream, posterDetails.getFileName());
                movie.setPosterFileName(posterFileName);
            } else {
                movie.setPosterFileName(null);
            }

            int movieId = movieService.addMovie(movie, getAuthenticatedUserId());

            //logging the movie creation event
            auditService.logMovieCreated(movieId, movie.getTitle(), getAuthenticatedUserId(), getClientIp());

            return Response.status(Response.Status.CREATED)
                    .entity(toResponse(movieService.getMovieById(movieId)))
                    .build();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add movie.", e);
        }

    }

    /*@POST
    @Path("/")
    public Response addMovie(Movie movie) throws java.sql.SQLException {
        int movieId = movieService.addMovie(movie, getAuthenticatedUserId());

        return Response.status(Response.Status.CREATED)
                .entity(toResponse(movieService.getMovieById(movieId)))
                .build();
    }*/
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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMovie(
            @PathParam("movieId") int movieId,
            @FormDataParam("title") String title,
            @FormDataParam("description") String description,
            @FormDataParam("durationMinutes") int durationMinutes,
            @FormDataParam("language") String language,
            @FormDataParam("genre") String genre,
            @FormDataParam("releaseDate") String releaseDate,
            @FormDataParam("poster") InputStream posterInputStream,
            @FormDataParam("poster") FormDataContentDisposition posterDetails)
            throws SQLException, IOException {

        Movie movie = new Movie();

        movie.setMovieId(movieId);
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDurationMinutes(durationMinutes);
        movie.setLanguage(language);
        movie.setGenre(genre);
        movie.setReleaseDate(LocalDate.parse(releaseDate));

        String originalFileName = null;

        if (posterDetails != null) {
            originalFileName = posterDetails.getFileName();
        }

        movieService.updateMovie(movie, getAuthenticatedUserId(), posterInputStream, originalFileName);

        //logging the movie update event
        auditService.logMovieUpdated(movieId, movie.getTitle(), getAuthenticatedUserId() , getClientIp());

        return Response.ok(
                new MessageResponse("Movie updated successfully.")
        ).build();

    }

    //delete 
    @DELETE
    @Path("/{movieId}")
    public Response deleteMovie(@PathParam("movieId") int movieId) throws java.sql.SQLException {
        if (!movieService.deactivateMovie(movieId, getAuthenticatedUserId())) {
            throw new MovieNotFoundException("Movie not found with ID: " + movieId);
        }

        //logging the movie deletion event

        Movie movie = movieService.getMovieById(movieId);
        auditService.logMovieDeleted(movieId, movie.getTitle(), getAuthenticatedUserId(), getClientIp());
        return Response
                .ok(new MessageResponse("Movie deleted successfully."))
                .build();
    }

    private int getAuthenticatedUserId() {

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

        if (movie.getPosterFileName() != null && !movie.getPosterFileName().isBlank()) {
            response.setPosterUrl("/movie_booking/api/movies/" + movie.getMovieId() + "/poster");
        }
        return response;
    }

    private String getClientIp() {
        return httpRequest.getRemoteAddr();
    }

}
