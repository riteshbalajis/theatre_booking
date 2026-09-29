package com.movie_booking.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import com.movie_booking.model.Movie;
import com.movie_booking.model.MovieStatus;

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

    //boolean updateMovie(Movie movie, int authenticatedUserId) throws SQLException;

    boolean updateMovie(Movie movie, int authenticatedUserId,InputStream posterInputStream,
        String originalFileName) throws SQLException, IOException;

    boolean activateMovie(int movieId, int authenticatedUserId) throws SQLException;

    boolean deactivateMovie(int movieId, int authenticatedUserId) throws SQLException;

    boolean movieExists(int movieId) throws SQLException;

    List<Movie> getUpcomingMovies() throws SQLException;

    boolean releaseMovie(int movieId, int authenticatedUserId) throws SQLException;

    public String saveMoviePoster(InputStream inputStream, String originalFileName) throws IOException;

    Path getMoviePoster(int movieId) throws SQLException;
}
