package com.jobtrack.dao;

import com.jobtrack.model.JobApplication;
import com.jobtrack.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for 'applications' database table operations.
 * Implements CRUD, secure tenant-scoped querying by user_id, parameterized filtering,
 * and status metrics aggregation.
 */
public class ApplicationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDAO.class);

    private static final String SQL_INSERT_APPLICATION =
            "INSERT INTO applications (user_id, company_name, job_role, location, salary, " +
            "job_description, job_url, applied_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_GET_ALL_BY_USER_ID =
            "SELECT application_id, user_id, company_name, job_role, location, salary, " +
            "job_description, job_url, applied_date, status, created_at, updated_at " +
            "FROM applications WHERE user_id = ? ORDER BY applied_date DESC, application_id DESC";

    private static final String SQL_GET_BY_ID_AND_USER_ID =
            "SELECT application_id, user_id, company_name, job_role, location, salary, " +
            "job_description, job_url, applied_date, status, created_at, updated_at " +
            "FROM applications WHERE application_id = ? AND user_id = ?";

    private static final String SQL_UPDATE_APPLICATION =
            "UPDATE applications SET company_name = ?, job_role = ?, location = ?, salary = ?, " +
            "job_description = ?, job_url = ?, applied_date = ?, status = ? " +
            "WHERE application_id = ? AND user_id = ?";

    private static final String SQL_DELETE_APPLICATION =
            "DELETE FROM applications WHERE application_id = ? AND user_id = ?";

    private static final String SQL_DASHBOARD_STATS =
            "SELECT status, COUNT(*) AS status_count FROM applications WHERE user_id = ? GROUP BY status";

    /**
     * Inserts a new job application record for the authenticated user.
     * Sets the generated applicationId on the provided JobApplication instance.
     *
     * @param app JobApplication domain object
     * @return true if successfully inserted, false otherwise
     */
    public boolean addApplication(JobApplication app) {
        if (app == null || app.getUserId() == null) {
            throw new IllegalArgumentException("Application and associated userId cannot be null.");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_APPLICATION, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, app.getUserId());
            stmt.setString(2, app.getCompanyName().trim());
            stmt.setString(3, app.getJobRole().trim());
            stmt.setString(4, app.getLocation().trim());

            if (app.getSalary() != null) {
                stmt.setBigDecimal(5, app.getSalary());
            } else {
                stmt.setNull(5, Types.DECIMAL);
            }

            stmt.setString(6, app.getJobDescription());
            stmt.setString(7, app.getJobUrl());

            if (app.getAppliedDate() != null) {
                stmt.setDate(8, Date.valueOf(app.getAppliedDate()));
            } else {
                stmt.setDate(8, new Date(System.currentTimeMillis()));
            }

            stmt.setString(9, app.getStatus() != null ? app.getStatus().toUpperCase() : "APPLIED");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        app.setApplicationId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.info("Application inserted successfully [ID: {}, UserID: {}]", app.getApplicationId(), app.getUserId());
                return true;
            }
            return false;

        } catch (SQLException e) {
            LOGGER.error("Failed to insert job application for user {}: {}", app.getUserId(), e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all job applications belonging to a specific user, ordered by applied_date DESC.
     *
     * @param userId unique identifier of the owner user
     * @return List of JobApplication objects (empty list if none found)
     */
    public List<JobApplication> getApplicationsByUserId(int userId) {
        List<JobApplication> applications = new ArrayList<>();
        if (userId <= 0) {
            return applications;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_ALL_BY_USER_ID)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error retrieving applications for user {}: {}", userId, e.getMessage());
        }
        return applications;
    }

    /**
     * Retrieves a single job application strictly checking both application_id and user_id ownership.
     * Prevents Insecure Direct Object Reference (IDOR) attacks.
     *
     * @param applicationId application primary key
     * @param userId        authenticated user primary key
     * @return JobApplication if matched and owned, or null otherwise
     */
    public JobApplication getApplicationByIdAndUserId(int applicationId, int userId) {
        if (applicationId <= 0 || userId <= 0) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_BY_ID_AND_USER_ID)) {

            stmt.setInt(1, applicationId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToApplication(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error retrieving application {} for user {}: {}", applicationId, userId, e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing application record ONLY if it belongs to the authenticated userId.
     *
     * @param app    the updated JobApplication data
     * @param userId authenticated user primary key
     * @return true if updated, false if not found or unauthorized
     */
    public boolean updateApplication(JobApplication app, int userId) {
        if (app == null || app.getApplicationId() == null || userId <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_APPLICATION)) {

            stmt.setString(1, app.getCompanyName().trim());
            stmt.setString(2, app.getJobRole().trim());
            stmt.setString(3, app.getLocation().trim());

            if (app.getSalary() != null) {
                stmt.setBigDecimal(4, app.getSalary());
            } else {
                stmt.setNull(4, Types.DECIMAL);
            }

            stmt.setString(5, app.getJobDescription());
            stmt.setString(6, app.getJobUrl());

            if (app.getAppliedDate() != null) {
                stmt.setDate(7, Date.valueOf(app.getAppliedDate()));
            } else {
                stmt.setDate(7, new Date(System.currentTimeMillis()));
            }

            stmt.setString(8, app.getStatus() != null ? app.getStatus().toUpperCase() : "APPLIED");
            stmt.setInt(9, app.getApplicationId());
            stmt.setInt(10, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Application {} updated successfully by user {}", app.getApplicationId(), userId);
                return true;
            } else {
                LOGGER.warn("Update skipped: application {} not found or does not belong to user {}", app.getApplicationId(), userId);
                return false;
            }

        } catch (SQLException e) {
            LOGGER.error("Error updating application {} for user {}: {}", app.getApplicationId(), userId, e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a job application ONLY where application_id = ? AND user_id = ?.
     * Cascades automatically to child interviews due to database schema constraints.
     *
     * @param applicationId application primary key to delete
     * @param userId        authenticated user primary key
     * @return true if deleted, false if not found or unauthorized
     */
    public boolean deleteApplication(int applicationId, int userId) {
        if (applicationId <= 0 || userId <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_APPLICATION)) {

            stmt.setInt(1, applicationId);
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Application {} deleted successfully by user {}", applicationId, userId);
                return true;
            } else {
                LOGGER.warn("Delete skipped: application {} not found or not owned by user {}", applicationId, userId);
                return false;
            }

        } catch (SQLException e) {
            LOGGER.error("Error deleting application {} for user {}: {}", applicationId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * Performs parameterized LIKE search across company_name and job_role with an optional status filter,
     * strictly scoped to the authenticated userId.
     *
     * @param userId  authenticated user primary key
     * @param keyword search term for company name or job title (optional)
     * @param status  filter status enum string (optional, "ALL" ignores filter)
     * @return List of matching JobApplication objects ordered by applied_date DESC
     */
    public List<JobApplication> searchAndFilterApplications(int userId, String keyword, String status) {
        List<JobApplication> results = new ArrayList<>();
        if (userId <= 0) {
            return results;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT application_id, user_id, company_name, job_role, location, salary, " +
                "job_description, job_url, applied_date, status, created_at, updated_at " +
                "FROM applications WHERE user_id = ?"
        );

        List<Object> params = new ArrayList<>();
        params.add(userId);

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append(" AND (LOWER(company_name) LIKE ? OR LOWER(job_role) LIKE ?)");
            String wildcardKeyword = "%" + keyword.trim().toLowerCase() + "%";
            params.add(wildcardKeyword);
            params.add(wildcardKeyword);
        }

        boolean hasStatus = (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL"));
        if (hasStatus) {
            sql.append(" AND status = ?");
            params.add(status.trim().toUpperCase());
        }

        sql.append(" ORDER BY applied_date DESC, application_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToApplication(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error executing search/filter applications for user {}: {}", userId, e.getMessage());
        }
        return results;
    }

    /**
     * Computes dashboard metrics for a user, returning total applications and breakdown
     * per status: APPLIED, ASSESSMENT, INTERVIEW, SELECTED, REJECTED.
     * Guaranteed to contain all status keys initialized to 0.
     *
     * @param userId authenticated user primary key
     * @return Map of status strings to integer counts
     */
    public Map<String, Integer> getDashboardStats(int userId) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("TOTAL", 0);
        stats.put("APPLIED", 0);
        stats.put("ASSESSMENT", 0);
        stats.put("INTERVIEW", 0);
        stats.put("SELECTED", 0);
        stats.put("REJECTED", 0);

        if (userId <= 0) {
            return stats;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DASHBOARD_STATS)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                int totalCount = 0;
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("status_count");
                    if (status != null) {
                        String upperStatus = status.toUpperCase();
                        stats.put(upperStatus, count);
                        totalCount += count;
                    }
                }
                stats.put("TOTAL", totalCount);
            }

        } catch (SQLException e) {
            LOGGER.error("Error aggregating dashboard metrics for user {}: {}", userId, e.getMessage());
        }
        return stats;
    }

    /**
     * Maps the current row of a ResultSet to a JobApplication domain instance.
     */
    private JobApplication mapResultSetToApplication(ResultSet rs) throws SQLException {
        JobApplication app = new JobApplication();
        app.setApplicationId(rs.getInt("application_id"));
        app.setUserId(rs.getInt("user_id"));
        app.setCompanyName(rs.getString("company_name"));
        app.setJobRole(rs.getString("job_role"));
        app.setLocation(rs.getString("location"));
        app.setSalary(rs.getBigDecimal("salary"));
        app.setJobDescription(rs.getString("job_description"));
        app.setJobUrl(rs.getString("job_url"));

        Date appliedDate = rs.getDate("applied_date");
        if (appliedDate != null) {
            app.setAppliedDate(appliedDate.toLocalDate());
        }

        app.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            app.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            app.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return app;
    }
}
