package com.jobtrack.controller;

import com.jobtrack.dao.InterviewDAO;
import com.jobtrack.model.Interview;
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
import java.time.LocalDateTime;

/**
 * Controller managing Interview rounds: scheduling new interviews and deleting
 * existing rounds, strictly verifying ownership of the parent application.
 */
@WebServlet(name = "InterviewServlet", urlPatterns = {"/interviews", "/interviews/*", "/interview"})
public class InterviewServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterviewServlet.class);
    private final InterviewDAO interviewDAO = new InterviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userId = getAuthenticatedUserId(req, resp);
        if (userId <= 0) {
            return;
        }

        String action = determineAction(req);
        if ("delete".equalsIgnoreCase(action)) {
            handleDeleteInterview(req, resp, userId);
        } else {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
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
            case "add", "create", "schedule" -> handleAddInterview(req, resp, userId);
            case "delete" -> handleDeleteInterview(req, resp, userId);
            default -> resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    private void handleAddInterview(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        int applicationId = parseId(req.getParameter("applicationId"));
        String interviewRound = req.getParameter("interviewRound");
        String interviewDateStr = req.getParameter("interviewDate");
        String remarks = req.getParameter("remarks");

        if (applicationId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=invalidApplication");
            return;
        }

        // Validate round and datetime
        if (!ValidationUtil.isNotEmpty(interviewRound) || !ValidationUtil.isValidLength(interviewRound, 1, 100)) {
            resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + applicationId + "&error=invalidRound");
            return;
        }

        if (!ValidationUtil.isValidDateTime(interviewDateStr)) {
            resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + applicationId + "&error=invalidDateTime");
            return;
        }

        LocalDateTime interviewDate = ValidationUtil.parseDateTime(interviewDateStr);
        Interview interview = new Interview(
                applicationId,
                interviewRound.trim(),
                interviewDate,
                ValidationUtil.isNotEmpty(remarks) ? remarks.trim() : null
        );

        boolean scheduled = interviewDAO.addInterview(interview, userId);
        if (scheduled) {
            LOGGER.info("Interview {} added for application {} by user {}", interview.getInterviewId(), applicationId, userId);
            resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + applicationId + "&interviewAdded=true");
        } else {
            LOGGER.warn("Failed to schedule interview for application {} by user {}", applicationId, userId);
            resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + applicationId + "&error=scheduleUnauthorized");
        }
    }

    private void handleDeleteInterview(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        int interviewId = parseId(req.getParameter("id"));
        if (interviewId <= 0) {
            interviewId = parseId(req.getParameter("interviewId"));
        }

        int applicationId = parseId(req.getParameter("applicationId"));

        if (interviewId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        boolean deleted = interviewDAO.deleteInterview(interviewId, userId);
        if (deleted) {
            LOGGER.info("Interview {} deleted by user {}", interviewId, userId);
            if (applicationId > 0) {
                resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + applicationId + "&interviewDeleted=true");
            } else {
                resp.sendRedirect(req.getContextPath() + "/dashboard?interviewDeleted=true");
            }
        } else {
            LOGGER.warn("Unauthorized attempt to delete interview {} by user {}", interviewId, userId);
            if (applicationId > 0) {
                resp.sendRedirect(req.getContextPath() + "/applications?action=view&id=" + applicationId + "&error=deleteUnauthorized");
            } else {
                resp.sendRedirect(req.getContextPath() + "/dashboard?error=deleteUnauthorized");
            }
        }
    }

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

        return "add";
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
}
