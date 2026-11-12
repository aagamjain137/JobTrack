package com.jobtrack.controller;

import com.jobtrack.dao.ApplicationDAO;
import com.jobtrack.dao.InterviewDAO;
import com.jobtrack.model.Interview;
import com.jobtrack.model.JobApplication;
import com.jobtrack.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller managing Job Application lifecycle: creating, viewing details (with interviews),
 * editing, updating, and deleting records with Post-Redirect-Get (PRG) patterns.
 */
@WebServlet(name = "ApplicationServlet", urlPatterns = {"/applications", "/applications/*", "/application"})
public class ApplicationServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServlet.class);

    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final InterviewDAO interviewDAO = new InterviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userId = getAuthenticatedUserId(req, resp);
        if (userId <= 0) {
            return;
        }

        String action = determineAction(req);

        switch (action) {
            case "new" -> showCreateForm(req, resp);
            case "edit" -> showEditForm(req, resp, userId);
            case "view" -> showApplicationDetails(req, resp, userId);
            case "delete" -> handleDeleteGet(req, resp, userId);
            default -> resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userId = getAuthenticatedUserId(req, resp);
        if (userId <= 0) {
            return;
        }

        String action = determineAction(req);

        switch (action) {
            case "add", "create", "new" -> handleCreateApplication(req, resp, userId);
            case "update", "edit" -> handleUpdateApplication(req, resp, userId);
            case "delete" -> handleDeleteApplication(req, resp, userId);
            default -> resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    // =========================================================================
    // GET Route Handlers
    // =========================================================================

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("formMode", "add");
        req.setAttribute("appliedDate", LocalDate.now().toString());
        req.setAttribute("status", "APPLIED");
        req.getRequestDispatcher("/WEB-INF/views/application-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, int userId) throws ServletException, IOException {
        int applicationId = parseId(req.getParameter("id"));
        if (applicationId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        JobApplication app = applicationDAO.getApplicationByIdAndUserId(applicationId, userId);
        if (app == null) {
            LOGGER.warn("Attempt to edit non-existent or unauthorized application {} by user {}", applicationId, userId);
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=notfound");
            return;
        }

        req.setAttribute("formMode", "edit");
        req.setAttribute("application", app);
        req.getRequestDispatcher("/WEB-INF/views/application-form.jsp").forward(req, resp);
    }

    private void showApplicationDetails(HttpServletRequest req, HttpServletResponse resp, int userId) throws ServletException, IOException {
        int applicationId = parseId(req.getParameter("id"));
        if (applicationId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        JobApplication app = applicationDAO.getApplicationByIdAndUserId(applicationId, userId);
        if (app == null) {
            LOGGER.warn("Unauthorized access attempt to application {} by user {}", applicationId, userId);
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=notfound");
            return;
        }

        // Fetch associated interview schedule
        List<Interview> interviews = interviewDAO.getInterviewsByApplicationIdAndUserId(applicationId, userId);

        // Flash message handling
        if ("true".equals(req.getParameter("created"))) {
            req.setAttribute("successMessage", "Job application created successfully!");
        } else if ("true".equals(req.getParameter("updated"))) {
            req.setAttribute("successMessage", "Job application updated successfully!");
        } else if ("true".equals(req.getParameter("interviewAdded"))) {
            req.setAttribute("successMessage", "Interview round scheduled successfully!");
        } else if ("true".equals(req.getParameter("interviewDeleted"))) {
            req.setAttribute("successMessage", "Interview round deleted successfully.");
        }

        req.setAttribute("application", app);
        req.setAttribute("interviews", interviews);
        req.getRequestDispatcher("/WEB-INF/views/application-details.jsp").forward(req, resp);
    }

    private void handleDeleteGet(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        handleDeleteApplication(req, resp, userId);
    }

    // =========================================================================
    // POST Mutation Handlers (PRG Pattern)
    // =========================================================================

    private void handleCreateApplication(HttpServletRequest req, HttpServletResponse resp, int userId)
            throws ServletException, IOException {

        String companyName = req.getParameter("companyName");
        String jobRole = req.getParameter("jobRole");
        String location = req.getParameter("location");
        String salaryStr = req.getParameter("salary");
        String jobDescription = req.getParameter("jobDescription");
        String jobUrl = req.getParameter("jobUrl");
        String appliedDateStr = req.getParameter("appliedDate");
        String status = req.getParameter("status");

        // Validation
        String validationError = validateApplicationInputs(companyName, jobRole, location, salaryStr, appliedDateStr, status, jobUrl);
        if (validationError != null) {
            req.setAttribute("errorMessage", validationError);
            req.setAttribute("formMode", "add");
            preserveFormInputs(req, companyName, jobRole, location, salaryStr, jobDescription, jobUrl, appliedDateStr, status);
            req.getRequestDispatcher("/WEB-INF/views/application-form.jsp").forward(req, resp);
            return;
        }

        BigDecimal salary = ValidationUtil.parseSalary(salaryStr);
        LocalDate appliedDate = ValidationUtil.parseDate(appliedDateStr);

        JobApplication newApp = new JobApplication(
                userId,
                companyName.trim(),
                jobRole.trim(),
                location.trim(),
                salary,
                ValidationUtil.isNotEmpty(jobDescription) ? jobDescription.trim() : null,
                ValidationUtil.isNotEmpty(jobUrl) ? jobUrl.trim() : null,
                appliedDate,
                status.trim().toUpperCase()
        );

        boolean created = applicationDAO.addApplication(newApp);
        if (created && newApp.getApplicationId() != null) {
            LOGGER.info("Created application {} for user {}", newApp.getApplicationId(), userId);
            resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + newApp.getApplicationId() + "&created=true");
        } else {
            req.setAttribute("errorMessage", "Failed to create application due to a database error.");
            req.setAttribute("formMode", "add");
            preserveFormInputs(req, companyName, jobRole, location, salaryStr, jobDescription, jobUrl, appliedDateStr, status);
            req.getRequestDispatcher("/WEB-INF/views/application-form.jsp").forward(req, resp);
        }
    }

    private void handleUpdateApplication(HttpServletRequest req, HttpServletResponse resp, int userId)
            throws ServletException, IOException {

        int applicationId = parseId(req.getParameter("applicationId"));
        if (applicationId <= 0) {
            applicationId = parseId(req.getParameter("id"));
        }

        if (applicationId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String companyName = req.getParameter("companyName");
        String jobRole = req.getParameter("jobRole");
        String location = req.getParameter("location");
        String salaryStr = req.getParameter("salary");
        String jobDescription = req.getParameter("jobDescription");
        String jobUrl = req.getParameter("jobUrl");
        String appliedDateStr = req.getParameter("appliedDate");
        String status = req.getParameter("status");

        // Validation
        String validationError = validateApplicationInputs(companyName, jobRole, location, salaryStr, appliedDateStr, status, jobUrl);
        if (validationError != null) {
            JobApplication existingApp = applicationDAO.getApplicationByIdAndUserId(applicationId, userId);
            req.setAttribute("errorMessage", validationError);
            req.setAttribute("formMode", "edit");
            req.setAttribute("application", existingApp);
            preserveFormInputs(req, companyName, jobRole, location, salaryStr, jobDescription, jobUrl, appliedDateStr, status);
            req.getRequestDispatcher("/WEB-INF/views/application-form.jsp").forward(req, resp);
            return;
        }

        BigDecimal salary = ValidationUtil.parseSalary(salaryStr);
        LocalDate appliedDate = ValidationUtil.parseDate(appliedDateStr);

        JobApplication updatedApp = new JobApplication(
                applicationId,
                userId,
                companyName.trim(),
                jobRole.trim(),
                location.trim(),
                salary,
                ValidationUtil.isNotEmpty(jobDescription) ? jobDescription.trim() : null,
                ValidationUtil.isNotEmpty(jobUrl) ? jobUrl.trim() : null,
                appliedDate,
                status.trim().toUpperCase(),
                null,
                null
        );

        boolean updated = applicationDAO.updateApplication(updatedApp, userId);
        if (updated) {
            LOGGER.info("Updated application {} for user {}", applicationId, userId);
            resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + applicationId + "&updated=true");
        } else {
            req.setAttribute("errorMessage", "Update failed: Application not found or unauthorized.");
            req.setAttribute("formMode", "edit");
            req.setAttribute("application", updatedApp);
            req.getRequestDispatcher("/WEB-INF/views/application-form.jsp").forward(req, resp);
        }
    }

    private void handleDeleteApplication(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        int applicationId = parseId(req.getParameter("id"));
        if (applicationId <= 0) {
            applicationId = parseId(req.getParameter("applicationId"));
        }

        if (applicationId > 0) {
            boolean deleted = applicationDAO.deleteApplication(applicationId, userId);
            if (deleted) {
                LOGGER.info("Deleted application {} by user {}", applicationId, userId);
                resp.sendRedirect(req.getContextPath() + "/dashboard?deleted=true");
                return;
            }
        }

        resp.sendRedirect(req.getContextPath() + "/dashboard?error=deleteFailed");
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private int getAuthenticatedUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return -1;
        }
        return (Integer) session.getAttribute("userId");
    }

    private String determineAction(HttpServletRequest req) {
        String actionParam = req.getParameter("action");
        if (actionParam != null && !actionParam.isBlank()) {
            return actionParam.trim().toLowerCase();
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String sub = pathInfo.substring(1);
            int slashIdx = sub.indexOf('/');
            return (slashIdx > 0 ? sub.substring(0, slashIdx) : sub).toLowerCase();
        }

        return "list";
    }

    private int parseId(String idStr) {
        if (idStr == null || idStr.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String validateApplicationInputs(String companyName, String jobRole, String location,
                                             String salaryStr, String appliedDateStr, String status, String jobUrl) {
        if (!ValidationUtil.isNotEmpty(companyName) || !ValidationUtil.isValidLength(companyName, 1, 150)) {
            return "Company name is required and must not exceed 150 characters.";
        }
        if (!ValidationUtil.isNotEmpty(jobRole) || !ValidationUtil.isValidLength(jobRole, 1, 150)) {
            return "Job role is required and must not exceed 150 characters.";
        }
        if (!ValidationUtil.isNotEmpty(location) || !ValidationUtil.isValidLength(location, 1, 150)) {
            return "Location is required and must not exceed 100 characters.";
        }
        if (!ValidationUtil.isValidOptionalSalary(salaryStr)) {
            return "Salary must be a positive number with at most 2 decimal places.";
        }
        if (!ValidationUtil.isValidDate(appliedDateStr)) {
            return "Applied date must be a valid calendar date in YYYY-MM-DD format.";
        }
        if (!ValidationUtil.isValidApplicationStatus(status)) {
            return "Please select a valid application status (APPLIED, ASSESSMENT, INTERVIEW, SELECTED, REJECTED).";
        }
        if (!ValidationUtil.isValidUrl(jobUrl)) {
            return "Job URL must be a valid HTTP or HTTPS web address.";
        }
        return null;
    }

    private void preserveFormInputs(HttpServletRequest req, String companyName, String jobRole, String location,
                                    String salaryStr, String jobDescription, String jobUrl,
                                    String appliedDateStr, String status) {
        req.setAttribute("companyName", companyName);
        req.setAttribute("jobRole", jobRole);
        req.setAttribute("location", location);
        req.setAttribute("salary", salaryStr);
        req.setAttribute("jobDescription", jobDescription);
        req.setAttribute("jobUrl", jobUrl);
        req.setAttribute("appliedDate", appliedDateStr);
        req.setAttribute("status", status);
    }
}
