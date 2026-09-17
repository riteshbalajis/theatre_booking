package com.movie_booking.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.movie_booking.dto.response.ShowSlotResponse;
import com.movie_booking.dto.response.TheatreShowsResponse;
import com.movie_booking.model.Show;
import com.movie_booking.model.ShowStatus;
import com.movie_booking.util.DBConnection;

public class ShowDaoImpl implements ShowDao {
    private static final String BASE_SELECT = "SELECT show_id, movie_id, screen_id, show_date, "
            + "start_time, end_time, status, created_at FROM shows";
    private static final String ORDER_BY_TIME = " ORDER BY show_date, start_time";

    @Override
    public int createShow(Show show) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return createShow(connection, show);
        }
        }

        @Override
        public int createShow(Connection connection, Show show) throws SQLException {
        String sql = "INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql,
            Statement.RETURN_GENERATED_KEYS)) {
            setShowParameters(statement, show);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Creating show failed: no ID was generated.");
    }

    @Override
    public Show findById(int showId) throws SQLException {
        String sql = BASE_SELECT + " WHERE show_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, showId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapShow(resultSet) : null;
            }
        }
    }

    @Override
    public Show findById(Connection connection, int showId) throws SQLException {
        String sql = BASE_SELECT + " WHERE show_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, showId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapShow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Show> findAll() throws SQLException {
        return findShows(BASE_SELECT + ORDER_BY_TIME);
    }

    @Override
    public List<Show> findShowsByMovieId(int movieId) throws SQLException {
        return findShowsByInt(BASE_SELECT + " WHERE movie_id = ?" + ORDER_BY_TIME, movieId);
    }

    @Override
    public List<Show> findShowsByScreenId(int screenId) throws SQLException {
        return findShowsByInt(BASE_SELECT + " WHERE screen_id = ?" + ORDER_BY_TIME, screenId);
    }

    @Override
    public List<Show> findShowsByDate(LocalDate showDate) throws SQLException {
        return findShowsByDateValue(BASE_SELECT + " WHERE show_date = ?" + ORDER_BY_TIME, showDate);
    }

    @Override
    public List<Show> findShowsByMovieAndDate(int movieId, LocalDate showDate)
            throws SQLException {
        String sql = BASE_SELECT + " WHERE movie_id = ? AND show_date = ?" + ORDER_BY_TIME;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, movieId);
            statement.setDate(2, Date.valueOf(showDate));
            return readShows(statement);
        }
    }
    

    @Override
    public List<TheatreShowsResponse> findGroupedShowsByMovieAndDate(int movieId, LocalDate showDate)
            throws SQLException {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // If the date is in the past, all shows have already ended
        if (showDate.isBefore(today)) {
            return new ArrayList<>();
        }

        boolean isToday = showDate.equals(today);

        StringBuilder sql = new StringBuilder(
                "SELECT s.show_id, s.screen_id, s.start_time, s.end_time, s.status, "
                + "sc.name AS screen_name, "
                + "t.theatre_id, t.name AS theatre_name, t.location AS theatre_location, "
                + "(SELECT MIN(ss.price) FROM show_seats ss WHERE ss.show_id = s.show_id) AS min_price "
                + "FROM shows s "
                + "JOIN screens sc ON s.screen_id = sc.screen_id "
                + "JOIN theatres t ON sc.theatre_id = t.theatre_id "
                + "WHERE s.movie_id = ? AND s.show_date = ? AND s.status != 'CANCELLED' "
        );

        if (isToday) {
            // End time must be greater than current time
            sql.append("AND s.end_time > ? ");
        }

        sql.append("ORDER BY t.name, sc.name, s.start_time");

        Map<Integer, TheatreShowsResponse> theatreMap = new LinkedHashMap<>();

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, movieId);
            statement.setDate(2, Date.valueOf(showDate));
            if (isToday) {
                statement.setTime(3, Time.valueOf(now));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int theatreId = rs.getInt("theatre_id");
                    TheatreShowsResponse theatre = theatreMap.get(theatreId);
                    if (theatre == null) {
                        theatre = new TheatreShowsResponse(
                                theatreId,
                                rs.getString("theatre_name"),
                                rs.getString("theatre_location")
                        );
                        theatreMap.put(theatreId, theatre);
                    }

                    ShowSlotResponse slot = new ShowSlotResponse();
                    slot.setShowId(rs.getInt("show_id"));
                    slot.setScreenId(rs.getInt("screen_id"));
                    slot.setScreenName(rs.getString("screen_name"));

                    Time startTime = rs.getTime("start_time");
                    slot.setStartTime(startTime == null ? null : startTime.toLocalTime());

                    Time endTime = rs.getTime("end_time");
                    slot.setEndTime(endTime == null ? null : endTime.toLocalTime());

                    String statusStr = rs.getString("status");
                    if (statusStr != null) {
                        try {
                            slot.setStatus(ShowStatus.valueOf(statusStr));
                        } catch (IllegalArgumentException ignored) {
                            slot.setStatus(ShowStatus.SCHEDULED);
                        }
                    }

                    BigDecimal minPrice = rs.getBigDecimal("min_price");
                    slot.setRegularPrice(minPrice);

                    theatre.addShow(slot);
                }
            }
        }
        return new ArrayList<>(theatreMap.values());
    }

    @Override
    public List<Show> findShowsByScreenAndDate(int screenId, LocalDate showDate)
            throws SQLException {
        String sql = BASE_SELECT + " WHERE screen_id = ? AND show_date = ?" + ORDER_BY_TIME;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, screenId);
            statement.setDate(2, Date.valueOf(showDate));
            return readShows(statement);
        }
    }

    @Override
    public TheatreShowsResponse findShowsByTheatreAndDate(int theatreId, LocalDate showDate)
            throws SQLException {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (showDate.isBefore(today)) {
            return new TheatreShowsResponse(theatreId, null, null);
        }

        boolean isToday = showDate.equals(today);
        StringBuilder sql = new StringBuilder(
                "SELECT s.show_id, s.screen_id, s.start_time, s.end_time, s.status, "
                + "sc.name AS screen_name, "
                + "t.name AS theatre_name, t.location AS theatre_location, "
                + "(SELECT MIN(ss.price) FROM show_seats ss WHERE ss.show_id = s.show_id) AS min_price "
                + "FROM shows s "
                + "JOIN screens sc ON s.screen_id = sc.screen_id "
                + "JOIN theatres t ON sc.theatre_id = t.theatre_id "
                + "WHERE t.theatre_id = ? AND s.show_date = ? "
                + "AND s.status != 'CANCELLED' ");

        if (isToday) {
            sql.append("AND s.end_time > ? ");
        }
        sql.append("ORDER BY sc.name, s.start_time");

        TheatreShowsResponse theatreShows = null;
        List<ShowSlotResponse> shows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, theatreId);
            statement.setDate(2, Date.valueOf(showDate));
            if (isToday) {
                statement.setTime(3, Time.valueOf(now));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    if (theatreShows == null) {
                        theatreShows = new TheatreShowsResponse(
                                theatreId,
                                rs.getString("theatre_name"),
                                rs.getString("theatre_location")
                        );
                    }

                    ShowSlotResponse slot = new ShowSlotResponse();
                    slot.setShowId(rs.getInt("show_id"));
                    slot.setScreenId(rs.getInt("screen_id"));
                    slot.setScreenName(rs.getString("screen_name"));

                    Time startTime = rs.getTime("start_time");
                    slot.setStartTime(startTime == null ? null : startTime.toLocalTime());

                    Time endTime = rs.getTime("end_time");
                    slot.setEndTime(endTime == null ? null : endTime.toLocalTime());

                    String statusStr = rs.getString("status");
                    if (statusStr != null) {
                        try {
                            slot.setStatus(ShowStatus.valueOf(statusStr));
                        } catch (IllegalArgumentException ignored) {
                            slot.setStatus(ShowStatus.SCHEDULED);
                        }
                    }

                    BigDecimal minPrice = rs.getBigDecimal("min_price");
                    slot.setRegularPrice(minPrice);

                    shows.add(slot);
                }
            }
        }
        if (theatreShows == null) {
            theatreShows = new TheatreShowsResponse(theatreId, null, null);
        }
        theatreShows.setShows(shows);
        return theatreShows;
    }
    

    @Override
    public List<Show> findUpcomingShows() throws SQLException {
        String sql = BASE_SELECT + " WHERE status = ? AND (show_date > CURRENT_DATE "
                + "OR (show_date = CURRENT_DATE AND start_time > CURRENT_TIME))"
                + ORDER_BY_TIME;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ShowStatus.SCHEDULED.name());
            return readShows(statement);
        }
    }

    @Override
    public List<Show> findActiveShowsByMovieId(int movieId) throws SQLException {
        String sql = BASE_SELECT + " WHERE movie_id = ? AND status IN (?, ?)" + ORDER_BY_TIME;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, movieId);
            statement.setString(2, ShowStatus.SCHEDULED.name());
            statement.setString(3, ShowStatus.ONGOING.name());
            return readShows(statement);
        }
    }

    @Override
    public boolean updateShow(Show show) throws SQLException {
        String sql = "UPDATE shows SET movie_id = ?, screen_id = ?, show_date = ?, "
                + "start_time = ?, end_time = ? WHERE show_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, show.getMovieId());
            statement.setInt(2, show.getScreenId());
            statement.setDate(3, Date.valueOf(show.getShowDate()));
            statement.setTime(4, Time.valueOf(show.getStartTime()));
            statement.setTime(5, Time.valueOf(show.getEndTime()));
            statement.setInt(6, show.getShowId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateShowStatus(int showId, ShowStatus status) throws SQLException {
        String sql = "UPDATE shows SET status = ? WHERE show_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, showId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cancelShow(int showId) throws SQLException {
        return updateShowStatus(showId, ShowStatus.CANCELLED);
    }

    @Override
    public boolean completeShow(int showId) throws SQLException {
        return updateShowStatus(showId, ShowStatus.COMPLETED);
    }

    @Override
    public boolean existsById(int showId) throws SQLException {
        String sql = "SELECT 1 FROM shows WHERE show_id = ? LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, showId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void setShowParameters(PreparedStatement statement, Show show) throws SQLException {
        statement.setInt(1, show.getMovieId());
        statement.setInt(2, show.getScreenId());
        statement.setDate(3, Date.valueOf(show.getShowDate()));
        statement.setTime(4, Time.valueOf(show.getStartTime()));
        statement.setTime(5, Time.valueOf(show.getEndTime()));
        statement.setString(6, show.getStatus() == null
                ? ShowStatus.SCHEDULED.name() : show.getStatus().name());
    }

    private List<Show> findShows(String sql) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            return readShows(statement);
        }
    }

    private List<Show> findShowsByInt(String sql, int value) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, value);
            return readShows(statement);
        }
    }

    private List<Show> findShowsByDateValue(String sql, LocalDate showDate) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(showDate));
            return readShows(statement);
        }
    }

    private List<Show> readShows(PreparedStatement statement) throws SQLException {
        List<Show> shows = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                shows.add(mapShow(resultSet));
            }
        }
        return shows;
    }

    private Show mapShow(ResultSet resultSet) throws SQLException {
        Date showDate = resultSet.getDate("show_date");
        Time startTime = resultSet.getTime("start_time");
        Time endTime = resultSet.getTime("end_time");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Show show = new Show();
        show.setShowId(resultSet.getInt("show_id"));
        show.setMovieId(resultSet.getInt("movie_id"));
        show.setScreenId(resultSet.getInt("screen_id"));
        show.setShowDate(showDate == null ? null : showDate.toLocalDate());
        show.setStartTime(startTime == null ? null : startTime.toLocalTime());
        show.setEndTime(endTime == null ? null : endTime.toLocalTime());
        show.setStatus(ShowStatus.valueOf(resultSet.getString("status")));
        show.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return show;
    }
}
