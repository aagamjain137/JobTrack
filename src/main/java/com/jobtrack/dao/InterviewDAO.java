package com.jobtrack.dao;

import com.jobtrack.model.Interview;
import com.jobtrack.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'interviews' database table operations.
 * Enforces parent application ownership verification across all scheduling, retrieval,
 * and deletion operations to guarantee multi-tenant security.
 */
public class InterviewDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterviewDAO.class);

    private static final String SQL_VERIFY_APPLICATION_OWNERSHIP =
            "SELECT 1 FROM applications WHERE application_id = ? AND user_id = ?";

    private static final String SQL_INSERT_INTERVIEW =
            "INSERT INTO interviews (application_id, interview_round, interview_date, remarks) VALUES (?, ?, ?, ?)";

    private static final String SQL_GET_BY_APPLICATION_AND_USER =
            "SELECT i.interview_id, i.application_id, i.interview_round, i.interview_date, i.remarks, i.created_at, " +
            "       a.company_name, a.job_role " +
            "FROM interviews i " +
            "INNER JOIN applications a ON i.application_id = a.application_id " +
            "WHERE i.application_id = ? AND a.user_id = ? " +
            "ORDER BY i.interview_date ASC, i.interview_id ASC";

    private static final String SQL_GET_UPCOMING_BY_USER_ID =
            "SELECT i.interview_id, i.application_id, i.interview_round, i.interview_date, i.remarks, i.created_at, " +
            "       a.company_name, a.job_role " +
            "FROM interviews i " +
            "INNER JOIN applications a ON i.application_id = a.application_id " +
            "WHERE a.user_id = ? AND i.interview_date >= NOW() " +
            "ORDER BY i.interview_date ASC, i.interview_id ASC";

    private static final String SQL_DELETE_INTERVIEW_WITH_OWNERSHIP =
            "DELETE i FROM interviews i " +
            "INNER JOIN applications a ON i.application_id = a.application_id " +
            "WHERE i.interview_id = ? AND a.user_id = ?";

    /**
     * Schedules a new interview round for an application, verifying within a database transaction
     * that the parent application genuinely belongs to the authenticated userId.
     *
     * @param interview the Interview domain object
     * @param userId    authenticated user primary key
     * @return true if successfully verified and inserted; false otherwise
     */
    public boolean addInterview(Interview interview, int userId) {
        if (interview == null || interview.getApplicationId() == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid interview or user parameters for interview creation.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // 1. Verify parent application ownership
                try (PreparedStatement checkStmt = conn.prepareStatement(SQL_VERIFY_APPLICATION_OWNERSHIP)) {
                    checkStmt.setInt(1, interview.getApplicationId());
                    checkStmt.setInt(2, userId);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            LOGGER.warn("Security violation: user {} attempted to schedule interview for unowned application {}",
                                    userId, interview.getApplicationId());
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // 2. Insert interview round
                try (PreparedStatement insertStmt = conn.prepareStatement(SQL_INSERT_INTERVIEW, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setInt(1, interview.getApplicationId());
                    insertStmt.setString(2, interview.getInterviewRound().trim());
                    insertStmt.setTimestamp(3, Timestamp.valueOf(interview.getInterviewDate()));
                    insertStmt.setString(4, interview.getRemarks());

                    int affectedRows = insertStmt.executeUpdate();
                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                interview.setInterviewId(generatedKeys.getInt(1));
                            }
                        }
                        conn.commit();
                        LOGGER.info("Interview {} scheduled successfully for application {} (User: {})",
                                interview.getInterviewId(), interview.getApplicationId(), userId);
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                LOGGER.error("Failed to schedule interview for application {}: {}", interview.getApplicationId(), e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {
            LOGGER.error("Database error during interview insertion for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all interview rounds scheduled for an application, strictly scoped by user ownership.
     *
     * @param applicationId parent application ID
     * @param userId        authenticated user ID
     * @return List of Interview objects ordered by interview_date ASC
     */
    public List<Interview> getInterviewsByApplicationIdAndUserId(int applicationId, int userId) {
        List<Interview> interviews = new ArrayList<>();
        if (applicationId <= 0 || userId <= 0) {
            return interviews;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_BY_APPLICATION_AND_USER)) {

            stmt.setInt(1, applicationId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interviews.add(mapResultSetToInterview(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error retrieving interviews for application {} and user {}: {}", applicationId, userId, e.getMessage());
        }
        return interviews;
    }

    /**
     * Retrieves all upcoming interviews scheduled for a user from the current moment onwards (interview_date >= NOW()),
     * joining with the applications table to include company name and role.
     *
     * @param userId authenticated user ID
     * @return List of upcoming Interview objects ordered chronologically
     */
    public List<Interview> getUpcomingInterviewsByUserId(int userId) {
        List<Interview> upcoming = new ArrayList<>();
        if (userId <= 0) {
            return upcoming;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_UPCOMING_BY_USER_ID)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    upcoming.add(mapResultSetToInterview(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error retrieving upcoming interviews for user {}: {}", userId, e.getMessage());
        }
        return upcoming;
    }

    /**
     * Deletes an interview round ONLY if its parent application belongs to the authenticated userId.
     *
     * @param interviewId primary key of interview
     * @param userId      authenticated user primary key
     * @return true if deleted, false if unauthorized or interview not found
     */
    public boolean deleteInterview(int interviewId, int userId) {
        if (interviewId <= 0 || userId <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_INTERVIEW_WITH_OWNERSHIP)) {

            stmt.setInt(1, interviewId);
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Interview {} deleted successfully by user {}", interviewId, userId);
                return true;
            } else {
                LOGGER.warn("Delete interview skipped: interview {} not found or unauthorized for user {}", interviewId, userId);
                return false;
            }

        } catch (SQLException e) {
            LOGGER.error("Error deleting interview {} for user {}: {}", interviewId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * Maps the current row of a ResultSet to an Interview domain instance,
     * including joined company and role fields when present.
     */
    private Interview mapResultSetToInterview(ResultSet rs) throws SQLException {
        Interview interview = new Interview();
        interview.setInterviewId(rs.getInt("interview_id"));
        interview.setApplicationId(rs.getInt("application_id"));
        interview.setInterviewRound(rs.getString("interview_round"));

        Timestamp interviewDate = rs.getTimestamp("interview_date");
        if (interviewDate != null) {
            interview.setInterviewDate(interviewDate.toLocalDateTime());
        }

        interview.setRemarks(rs.getString("remarks"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            interview.setCreatedAt(createdAt.toLocalDateTime());
        }

        // Populate joined metadata if present in query projection
        try {
            interview.setCompanyName(rs.getString("company_name"));
            interview.setJobRole(rs.getString("job_role"));
        } catch (SQLException ignored) {
            // Projected query may not include joined application columns
        }

        return interview;
    }
}
