package com.movie_booking.util;

import java.sql.Connection;
import java.sql.SQLException;

import com.movie_booking.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DBConnection {


    private static final HikariDataSource data_source;

    private DBConnection() {
        // Utility class.
    }


    static{


    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(AppConfig.getRequired("DB_URL"));
    config.setUsername(AppConfig.getRequired("DB_USERNAME"));
    config.setPassword(AppConfig.getRequired("DB_PASSWORD"));

    config.setDriverClassName("com.mysql.cj.jdbc.Driver");

    //config.setDriverClassName("com.mysql.cj.jdbc.Driver");

    //System.out.println(AppConfig.getRequired("DB_URL")+" "+AppConfig.getRequired("DB_USERNAME")+" "+AppConfig.getRequired("DB_PASSWORD"));

    config.setMaximumPoolSize(10);
    config.setMinimumIdle(5);

    config.setConnectionTimeout(30000);
    config.setMaxLifetime(1800000);

    data_source=new HikariDataSource(config);


    }

    public static Connection getConnection() throws SQLException{
        return data_source.getConnection();
    }

    public static void closePool(){
        if (data_source != null) {
            data_source.close();
        }
    }

    

    /*public static Connection getConnection() throws SQLException {
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException exception) {
        throw new SQLException("MySQL JDBC driver not found.", exception);
    }

        return DriverManager.getConnection(
                AppConfig.getRequired("DB_URL"),
                AppConfig.getRequired("DB_USERNAME"),
                AppConfig.getRequired("DB_PASSWORD"));
    }*/
}
