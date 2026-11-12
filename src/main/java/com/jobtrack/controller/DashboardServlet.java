package com.jobtrack.controller;

import com.jobtrack.dao.ApplicationDAO;
import com.jobtrack.dao.InterviewDAO;
import com.jobtrack.model.Interview;
import com.jobtrack.model.JobApplication;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller aggregating dashboard analytics, application search/filter results,
 * and upcoming interviews for the authenticated user.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardServlet.class);

    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final InterviewDAO interviewDAO = new InterviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");

        // 1. Fetch dashboard metric cards
        Map<String, Integer> stats = applicationDAO.getDashboardStats(userId);

        // 2. Fetch search and filtered job applications
        List<JobApplication> applications = applicationDAO.searchAndFilterApplications(userId, keyword, status);

        // 3. Fetch upcoming interview schedule
        List<Interview> upcomingInterviews = interviewDAO.getUpcomingInterviewsByUserId(userId);

        // Flash message handling
        if ("true".equals(req.getParameter("deleted"))) {
            req.setAttribute("successMessage", "Job application deleted successfully.");
        }

        // Attach attributes for dashboard.jsp rendering
        req.setAttribute("stats", stats);
        req.setAttribute("applications", applications);
        req.setAttribute("upcomingInterviews", upcomingInterviews);
        req.setAttribute("keyword", keyword != null ? keyword.trim() : "");
        req.setAttribute("selectedStatus", status != null ? status.trim().toUpperCase() : "ALL");

        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}
