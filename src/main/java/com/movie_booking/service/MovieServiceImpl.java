package com.movie_booking.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.movie_booking.dao.MovieDao;
import com.movie_booking.dao.MovieDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.exception.MovieNotFoundException;
import com.movie_booking.model.Movie;
import com.movie_booking.model.MovieStatus;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;

public class MovieServiceImpl implements MovieService {

    private final MovieDao movieDao;
    private final UserDao userDao;
    private static final String POSTER_DIRECTORY = "D:/movie_booking/uploads/movie-posters";
    private static final String ALL_MOVIES_CACHE_KEY = "all";

    private static final Cache<Integer, Movie> movieCache
            = Caffeine.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();
                private static final Cache<String, List<Movie>> movieListCache
                    = Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(2, TimeUnit.MINUTES)
                        .build();

    public MovieServiceImpl() {
        this(new MovieDaoImpl(), new UserDaoImpl());
    }

    public MovieServiceImpl(MovieDao movieDao) {
        this(movieDao, new UserDaoImpl());
    }

    public MovieServiceImpl(MovieDao movieDao, UserDao userDao) {
        if (movieDao == null || userDao == null) {
            throw new IllegalArgumentException("Movie and user DAOs cannot be null.");
        }
        this.movieDao = movieDao;
        this.userDao = userDao;
    }

    @Override
    public int addMovie(Movie movie, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateMovie(movie);
        if (movieDao.findByTitle(movie.getTitle().trim()) != null) {
            throw new IllegalArgumentException("Movie title already exists.");
        }
        movie.setTitle(movie.getTitle().trim());
        movie.setLanguage(movie.getLanguage().trim());
        if (movie.getStatus() == null) {
            movie.setStatus(MovieStatus.ACTIVE);
        }
        int movieId = movieDao.createMovie(movie);
        invalidateMovieCaches(movieId);
        return movieId;
    }

    @Override
    public String saveMoviePoster(InputStream inputStream, String originalFileName) throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException("Poster file is required.");
        }

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Poster filename is required.");
        }

        Path uploadDirectory = Paths.get(POSTER_DIRECTORY);

        Files.createDirectories(uploadDirectory);

        String extension = "";

        int dotIndex = originalFileName.lastIndexOf('.');

        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex).toLowerCase();
        }

        String uniqueFileName = UUID.randomUUID() + extension;

        Path targetPath = uploadDirectory.resolve(uniqueFileName);

        Files.copy(
                inputStream,
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return uniqueFileName;
    }

    @Override
    public Movie getMovieById(int movieId) throws SQLException {
        requirePositiveId(movieId);

        Movie cachedMovie = movieCache.getIfPresent(movieId);
        if (cachedMovie != null) {
            System.out.println("Movie cache hit for ID: " + movieId);
            return cachedMovie;
        }

        System.out.println("Movie cache miss for ID: " + movieId);
        Movie movie = movieDao.findById(movieId);
        if (movie != null) {
            movieCache.put(movieId, movie);
        }
        return movie;
    }

    @Override
    public Movie getMovieByTitle(String title) throws SQLException {
        requireText(title, "Title");
        return movieDao.findByTitle(title.trim());
    }

    @Override
    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> cachedMovies = movieListCache.getIfPresent(ALL_MOVIES_CACHE_KEY);
        if (cachedMovies != null) {
            System.out.println("Movie list cache hit");
            return cachedMovies;
        }

        System.out.println("Movie list cache miss");
        List<Movie> movies = movieDao.findAll();
        if (movies != null) {
            List<Movie> snapshot = List.copyOf(movies);
            movieListCache.put(ALL_MOVIES_CACHE_KEY, snapshot);
            return snapshot;
        }
        return null;
    }

    @Override
    public List<Movie> getActiveMovies() throws SQLException {
        return movieDao.findActiveMovies();
    }

    @Override
    public List<Movie> searchMovies(String keyword) throws SQLException {
        requireText(keyword, "Search keyword");
        return movieDao.searchMovies(keyword.trim());
    }

    @Override
    public List<Movie> getMoviesByStatus(MovieStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        return movieDao.findAllByStatus(status);
    }

    @Override
    public List<Movie> getMoviesByLanguage(String language) throws SQLException {
        requireText(language, "Language");
        return movieDao.findAllByLanguage(language.trim());
    }

    @Override
    public List<Movie> getMoviesByGenre(String genre) throws SQLException {
        requireText(genre, "Genre");
        return movieDao.findAllByGenre(genre.trim());
    }

    @Override
    public boolean updateMovie(
            Movie movie,
            int authenticatedUserId,
            InputStream posterInputStream,
            String originalFileName)
            throws SQLException, IOException {

        requireAdmin(authenticatedUserId);

        validateMovie(movie);

        if (posterInputStream != null
                && originalFileName != null
                && !originalFileName.isBlank()) {

            String newPosterFileName
                    = saveMoviePoster(posterInputStream, originalFileName);

            movie.setPosterFileName(newPosterFileName);
        }

        boolean updated = movieDao.updateMovie(movie);

        if (!updated) {
            throw new MovieNotFoundException(
                    "Movie not found with ID: " + movie.getMovieId()
            );
        }

        invalidateMovieCaches(movie.getMovieId());
        return true;

    }

    @Override
    public Path getMoviePoster(int movieId) throws SQLException {

        Movie movie = movieDao.findById(movieId);

        if (movie == null) {
            throw new MovieNotFoundException(
                    "Movie not found with ID: " + movieId
            );
        }

        String posterFileName = movie.getPosterFileName();

        if (posterFileName == null || posterFileName.isBlank()) {
            throw new MovieNotFoundException(
                    "Poster not found for movie ID: " + movieId
            );
        }

        Path posterPath = Paths.get(
                POSTER_DIRECTORY,
                posterFileName
        );

        if (!Files.exists(posterPath)) {
            throw new MovieNotFoundException(
                    "Poster file not found for movie ID: " + movieId
            );
        }

        return posterPath;
    }

    @Override
    public boolean activateMovie(int movieId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(movieId);
        boolean activated = movieDao.activateMovie(movieId);
        if (activated) {
            invalidateMovieCaches(movieId);
        }
        return activated;
    }

    @Override
    public boolean deactivateMovie(int movieId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(movieId);
        boolean deactivated = movieDao.deactivateMovie(movieId);
        if (deactivated) {
            invalidateMovieCaches(movieId);
        }
        return deactivated;
    }

    @Override
    public boolean movieExists(int movieId) throws SQLException {
        requirePositiveId(movieId);
        return movieDao.existsById(movieId);
    }

    @Override
    public List<Movie> getUpcomingMovies() throws SQLException {
        return movieDao.findAllByStatus(MovieStatus.UPCOMING);
    }

    @Override
    public boolean releaseMovie(int movieId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(movieId);
        Movie movie = movieDao.findById(movieId);
        if (movie == null) {
            return false;
        }
        if (movie.getStatus() != MovieStatus.UPCOMING) {
            throw new IllegalStateException("Only upcoming movies can be released.");
        }
        boolean released = movieDao.activateMovie(movieId);
        if (released) {
            invalidateMovieCaches(movieId);
        }
        return released;
    }

    private static void invalidateMovieCaches(int movieId) {
        movieCache.invalidate(movieId);
        movieListCache.invalidate(ALL_MOVIES_CACHE_KEY);
    }

    private static void validateMovie(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null.");
        }
        requireText(movie.getTitle(), "Title");
        requireText(movie.getLanguage(), "Language");
        if (movie.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero.");
        }
    }

    private static void validateMovieForUpdate(Movie movie) {
        if (movie == null || movie.getMovieId() <= 0) {
            throw new IllegalArgumentException("A valid movie is required.");
        }
        validateMovie(movie);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    private static void requirePositiveId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Movie ID must be positive.");
        }
    }

    private void requireAdmin(int authenticatedUserId) throws SQLException {
        if (authenticatedUserId <= 0) {
            throw new AuthenticationRequiredException();
        }
        User user = userDao.findById(authenticatedUserId);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationRequiredException();
        }
        if (user.getRole() != UserRole.ADMIN) {
            throw new AuthorizationException();
        }
    }
}
