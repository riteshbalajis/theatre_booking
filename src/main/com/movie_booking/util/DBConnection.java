package com.movie_booking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/movie_booking"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" ;
    private static final String USER = getEnvironmentValue("MOVIE_DB_USER", "root");
    private static final String PASSWORD = getEnvironmentValue("MOVIE_DB_PASSWORD", "Sql1234@");

    private DBConnection() {
        // Utility class.
    }

    public static Connection getConnection() throws SQLException {
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException exception) {
        throw new SQLException("MySQL JDBC driver not found.", exception);
    }

    return DriverManager.getConnection(URL, USER, PASSWORD);
}

 
    private static String getEnvironmentValue(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
