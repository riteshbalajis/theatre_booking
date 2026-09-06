package com.movie_booking.service;

import com.movie_booking.dto.request.ChangePasswordRequest;
import com.movie_booking.dto.request.LoginRequest;
import com.movie_booking.dto.request.RegisterRequest;
import com.movie_booking.dto.request.UserUpdateRequest;
import com.movie_booking.dto.response.LoginResponse;
import com.movie_booking.dto.response.UserResponse;
import com.movie_booking.model.UserRole;
import com.movie_booking.model.UserStatus;
import java.sql.SQLException;
import java.util.List;

public interface UserService {
    int registerUser(RegisterRequest request) throws SQLException;

    LoginResponse login(LoginRequest request) throws SQLException;

    UserResponse getUserById(int userId) throws SQLException;

    UserResponse getUserByEmail(String email) throws SQLException;

    boolean updateProfile(UserUpdateRequest request) throws SQLException;

    boolean changePassword(ChangePasswordRequest request) throws SQLException;

    boolean activateUser(int userId) throws SQLException;

    boolean deactivateUser(int userId) throws SQLException;

        List<UserResponse> getAllUsers() throws SQLException;

        List<UserResponse> getUsersByRole(UserRole role) throws SQLException;

        List<UserResponse> getUsersByStatus(UserStatus status) throws SQLException;

        List<UserResponse> getUsersByRoleAndStatus(UserRole role, UserStatus status)
            throws SQLException;

    boolean emailExists(String email) throws SQLException;
}
