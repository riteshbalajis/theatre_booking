package com.movie_booking.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.movie_booking.dto.request.MovieCreateRequest;
import com.movie_booking.dto.request.MovieUpdateRequest;
import com.movie_booking.dto.response.MovieResponse;
import com.movie_booking.model.Movie;
import com.movie_booking.service.AuthenticationRequiredException;
import com.movie_booking.service.AuthorizationException;
import com.movie_booking.service.MovieService;
import com.movie_booking.service.MovieServiceImpl;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/movies/*")
public class MovieServlet extends HttpServlet {
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private MovieService movieService;

    @Override
    public void init() throws ServletException {
        movieService = new MovieServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String path = request.getPathInfo();
            if (isCollectionPath(path)) {
                String keyword = request.getParameter("keyword");
                List<Movie> movies = keyword == null || keyword.trim().isEmpty()
                        ? movieService.getAllMovies()
                        : movieService.searchMovies(keyword);
                writeJson(response, HttpServletResponse.SC_OK, movieResponses(movies));
                return;
            }

            int movieId = parseMovieId(path);
            Movie movie = movieService.getMovieById(movieId);
            if (movie == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Movie was not found."));
                return;
            }
            writeJson(response, HttpServletResponse.SC_OK, movieResponse(movie));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to retrieve movies", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movies could not be retrieved."));
        } catch (RuntimeException exception) {
            log("Unexpected error while retrieving movies", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movies could not be retrieved."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();
        try {
            if (isCollectionPath(path)) {
                MovieCreateRequest createRequest = OBJECT_MAPPER.readValue(
                        request.getReader(), MovieCreateRequest.class);
                int movieId = movieService.addMovie(toMovie(createRequest), sessionUserId(request));
                Map<String, Object> body = successResponse("Movie created successfully.");
                body.put("movieId", movieId);
                writeJson(response, HttpServletResponse.SC_CREATED, body);
                return;
            }

            int movieId = parseMovieActionId(path);
            if (path.endsWith("/activate")) {
                writeStatusResult(response, movieService.activateMovie(movieId,
                    sessionUserId(request)),
                        "Movie activated successfully.");
            } else if (path.endsWith("/deactivate")) {
                writeStatusResult(response, movieService.deactivateMovie(movieId,
                    sessionUserId(request)),
                        "Movie deactivated successfully.");
            } else {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Movie endpoint was not found."));
            }
        } catch (JsonProcessingException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse("Request body must contain valid JSON."));
        } catch (AuthenticationRequiredException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                errorResponse(exception.getMessage()));
        } catch (AuthorizationException exception) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, movieErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to change movie state", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movie operation could not be completed."));
        } catch (RuntimeException exception) {
            log("Unexpected error while changing movie state", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movie operation could not be completed."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int movieId = parseMovieId(request.getPathInfo());
            MovieUpdateRequest updateRequest = OBJECT_MAPPER.readValue(
                    request.getReader(), MovieUpdateRequest.class);
            updateRequest.setMovieId(movieId);
                boolean updated = movieService.updateMovie(toMovie(updateRequest),
                    sessionUserId(request));
            if (!updated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Movie was not found."));
                return;
            }
            Movie movie = movieService.getMovieById(movieId);
            writeJson(response, HttpServletResponse.SC_OK, movieResponse(movie));
        } catch (JsonProcessingException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse("Request body must contain valid JSON."));
        } catch (AuthenticationRequiredException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                errorResponse(exception.getMessage()));
        } catch (AuthorizationException exception) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, movieErrorStatus(exception),
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to update movie", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movie could not be updated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while updating movie", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movie could not be updated."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int movieId = parseMovieId(request.getPathInfo());
                boolean deactivated = movieService.deactivateMovie(movieId,
                    sessionUserId(request));
            if (!deactivated) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        errorResponse("Movie was not found."));
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (AuthenticationRequiredException exception) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                errorResponse(exception.getMessage()));
        } catch (AuthorizationException exception) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                errorResponse(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    errorResponse(exception.getMessage()));
        } catch (SQLException exception) {
            log("Unable to deactivate movie", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movie could not be deactivated."));
        } catch (RuntimeException exception) {
            log("Unexpected error while deactivating movie", exception);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    errorResponse("Movie could not be deactivated."));
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)
                && !"PUT".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            response.setHeader("Allow", "GET, POST, PUT, DELETE");
            writeJson(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    errorResponse("HTTP method is not supported for this endpoint."));
            return;
        }
        super.service(request, response);
    }

    private void writeStatusResult(HttpServletResponse response, boolean changed, String message)
            throws IOException {
        if (!changed) {
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                    errorResponse("Movie was not found."));
            return;
        }
        writeJson(response, HttpServletResponse.SC_OK, successResponse(message));
    }

    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    private static boolean isCollectionPath(String path) {
        return path == null || "/".equals(path) || path.isEmpty();
    }

    private static int sessionUserId(HttpServletRequest request) {
        javax.servlet.http.HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("userId");
        return value instanceof Integer ? (Integer) value : 0;
    }

    private static int parseMovieId(String path) {
        if (path == null || !path.matches("/[0-9]+")) {
            throw new IllegalArgumentException("A valid movie ID is required.");
        }
        try {
            return Integer.parseInt(path.substring(1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A valid movie ID is required.");
        }
    }

    private static int parseMovieActionId(String path) {
        if (path == null || !path.matches("/[0-9]+/(activate|deactivate)")) {
            throw new IllegalArgumentException("A valid movie action is required.");
        }
        return parseMovieId(path.substring(0, path.lastIndexOf('/')));
    }

    private static Movie toMovie(MovieCreateRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setLanguage(request.getLanguage());
        movie.setGenre(request.getGenre());
        movie.setReleaseDate(request.getReleaseDate());
        return movie;
    }

    private static Movie toMovie(MovieUpdateRequest request) {
        Movie movie = new Movie();
        movie.setMovieId(request.getMovieId());
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setLanguage(request.getLanguage());
        movie.setGenre(request.getGenre());
        movie.setReleaseDate(request.getReleaseDate());
        return movie;
    }

    private static List<Map<String, Object>> movieResponses(List<Movie> movies) {
        return movies.stream().map(MovieServlet::movieResponse).collect(Collectors.toList());
    }

    private static Map<String, Object> movieResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setMovieId(movie.getMovieId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setLanguage(movie.getLanguage());
        response.setGenre(movie.getGenre());
        response.setReleaseDate(movie.getReleaseDate());
        response.setStatus(movie.getStatus());
        response.setCreatedAt(movie.getCreatedAt());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("movieId", response.getMovieId());
        body.put("title", response.getTitle());
        body.put("description", response.getDescription());
        body.put("durationMinutes", response.getDurationMinutes());
        body.put("language", response.getLanguage());
        body.put("genre", response.getGenre());
        body.put("releaseDate", response.getReleaseDate() == null
                ? null : response.getReleaseDate().toString());
        body.put("status", response.getStatus());
        body.put("createdAt", response.getCreatedAt() == null
                ? null : response.getCreatedAt().toString());
        return body;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    private static int movieErrorStatus(IllegalArgumentException exception) {
        return exception.getMessage() != null && exception.getMessage().contains("already exists")
                ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST;
    }

    private static Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    private static Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", message == null ? "Invalid request." : message);
        return response;
    }

    private static final class LocalDateDeserializer extends StdDeserializer<LocalDate> {
        private LocalDateDeserializer() {
            super(LocalDate.class);
        }

        @Override
        public LocalDate deserialize(com.fasterxml.jackson.core.JsonParser parser,
                com.fasterxml.jackson.databind.DeserializationContext context) throws IOException {
            try {
                return LocalDate.parse(parser.getText().trim());
            } catch (DateTimeParseException exception) {
                throw new JsonProcessingException("releaseDate must be a valid ISO date.", exception) { };
            }
        }
    }
}