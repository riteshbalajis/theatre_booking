package com.movie_booking.service;

import com.movie_booking.model.Movie;
import com.movie_booking.model.MovieStatus;
import java.sql.SQLException;
import java.util.List;

public interface MovieService {
    int addMovie(Movie movie, int authenticatedUserId) throws SQLException;

    Movie getMovieById(int movieId) throws SQLException;

    Movie getMovieByTitle(String title) throws SQLException;

    List<Movie> getAllMovies() throws SQLException;

    List<Movie> getActiveMovies() throws SQLException;

    List<Movie> searchMovies(String keyword) throws SQLException;

    List<Movie> getMoviesByStatus(MovieStatus status) throws SQLException;

    List<Movie> getMoviesByLanguage(String language) throws SQLException;

    List<Movie> getMoviesByGenre(String genre) throws SQLException;

    boolean updateMovie(Movie movie, int authenticatedUserId) throws SQLException;

    boolean activateMovie(int movieId, int authenticatedUserId) throws SQLException;

    boolean deactivateMovie(int movieId, int authenticatedUserId) throws SQLException;

    boolean movieExists(int movieId) throws SQLException;

    List<Movie> getUpcomingMovies() throws SQLException;

    boolean releaseMovie(int movieId, int authenticatedUserId) throws SQLException;
}
