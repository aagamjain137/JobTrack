package com.jobtrack.dao;

import com.jobtrack.model.User;
import com.jobtrack.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Data Access Object for 'users' database table operations.
 * Manages user registration, credential verification, and profile retrieval.
 */
public class UserDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    private static final String SQL_INSERT_USER =
            "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT user_id, name, email, password_hash, created_at FROM users WHERE LOWER(email) = LOWER(?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT user_id, name, email, password_hash, created_at FROM users WHERE user_id = ?";

    private static final String SQL_EMAIL_EXISTS =
            "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";

    /**
     * Inserts a new user into the database and populates the generated userId on the User object.
     *
     * @param user the User domain object containing name, email, and passwordHash
     * @return true if user registration succeeded, false otherwise
     */
    public boolean registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User object cannot be null.");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName().trim());
            stmt.setString(2, user.getEmail().trim().toLowerCase());
            stmt.setString(3, user.getPasswordHash());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.info("User registered successfully with ID: {}", user.getUserId());
                return true;
            }
            return false;

        } catch (SQLException e) {
            LOGGER.error("Error occurred while registering user with email '{}': {}", user.getEmail(), e.getMessage());
            return false;
        }
    }

    /**
     * Inserts a new user and returns the generated primary key userId.
     *
     * @param user the User domain object
     * @return generated userId (> 0) if successful, or -1 on failure
     */
    public int registerUserAndGetId(User user) {
        if (registerUser(user) && user.getUserId() != null) {
            return user.getUserId();
        }
        return -1;
    }

    /**
     * Retrieves a user by email address (case-insensitive).
     *
     * @param email user's email address
     * @return User object if found, or null otherwise
     */
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error finding user by email '{}': {}", email, e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a user by email address wrapped in an {@link Optional}.
     *
     * @param email user's email address
     * @return Optional containing User if found, or empty Optional
     */
    public Optional<User> findOptionalByEmail(String email) {
        return Optional.ofNullable(findByEmail(email));
    }

    /**
     * Retrieves a user by unique primary key userId.
     *
     * @param userId unique user identifier
     * @return User object if found, or null otherwise
     */
    public User findById(int userId) {
        if (userId <= 0) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error finding user by ID {}: {}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * Checks whether an account already exists with the given email address.
     *
     * @param email the email to verify
     * @return true if email exists in database, false otherwise
     */
    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EMAIL_EXISTS)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            LOGGER.error("Error checking email existence for '{}': {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Maps the current row of a ResultSet to a User domain instance.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        return user;
    }
}
