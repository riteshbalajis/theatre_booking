package com.movie_booking.service;

import com.movie_booking.dao.MovieDao;
import com.movie_booking.dao.MovieDaoImpl;
import com.movie_booking.dao.UserDao;
import com.movie_booking.dao.UserDaoImpl;
import com.movie_booking.model.Movie;
import com.movie_booking.model.MovieStatus;
import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import java.sql.SQLException;
import java.util.List;

public class MovieServiceImpl implements MovieService {
    private final MovieDao movieDao;
    private final UserDao userDao;

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
            movie.setStatus(MovieStatus.UPCOMING);
        }
        return movieDao.createMovie(movie);
    }

    @Override
    public Movie getMovieById(int movieId) throws SQLException {
        requirePositiveId(movieId);
        return movieDao.findById(movieId);
    }

    @Override
    public Movie getMovieByTitle(String title) throws SQLException {
        requireText(title, "Title");
        return movieDao.findByTitle(title.trim());
    }

    @Override
    public List<Movie> getAllMovies() throws SQLException {
        return movieDao.findAll();
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
    public boolean updateMovie(Movie movie, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        validateMovieForUpdate(movie);
        Movie existing = movieDao.findByTitle(movie.getTitle().trim());
        if (existing != null && existing.getMovieId() != movie.getMovieId()) {
            throw new IllegalArgumentException("Movie title already exists.");
        }
        movie.setTitle(movie.getTitle().trim());
        movie.setLanguage(movie.getLanguage().trim());
        return movieDao.updateMovie(movie);
    }

    @Override
    public boolean activateMovie(int movieId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(movieId);
        return movieDao.activateMovie(movieId);
    }

    @Override
    public boolean deactivateMovie(int movieId, int authenticatedUserId) throws SQLException {
        requireAdmin(authenticatedUserId);
        requirePositiveId(movieId);
        return movieDao.deactivateMovie(movieId);
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
        return movieDao.activateMovie(movieId);
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
