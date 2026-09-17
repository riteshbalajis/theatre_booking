package com.movie_booking.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.movie_booking.model.User;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;

public interface UserDao {

    int createUser(User user) throws SQLException;

    int createUser(Connection connection, User user) throws SQLException;

    User findById(int userId) throws SQLException;

    User findByEmail(String email) throws SQLException;

    boolean existsByEmail(String email) throws SQLException;

    boolean updateUser(User user) throws SQLException;

    boolean updatePassword(int userId, String passwordHash) throws SQLException;

    boolean deactivateUser(int userId) throws SQLException;

    boolean activateUser(int userId) throws SQLException;

    List<User> findAll() throws SQLException;

    List<User> findAllByRole(UserRole role) throws SQLException;

    List<User> findAllByStatus(UserStatus status) throws SQLException;

    List<User> findByRoleAndStatus(UserRole role, UserStatus status) throws SQLException;

    User findByGoogleSub(String googleSub) throws SQLException;

    void updateGoogleSub(int userId, String googleSub) throws SQLException;

    boolean updatePassword(Connection connection,int userId,String passwordHash) throws SQLException;

    boolean isTotpEnabled(int userId) throws SQLException;

    String getTotpSecret(int userId) throws SQLException;

    boolean saveTotpSecret(int userId, String secret)
        throws SQLException;

    boolean enableTotp(int userId) throws SQLException;

    boolean disableTotp(int userId) throws SQLException;

    boolean regenerateTotpSecret(int userId,String newSecret) throws SQLException;






}
