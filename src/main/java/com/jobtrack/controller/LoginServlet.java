package com.jobtrack.controller;

import com.jobtrack.dao.UserDAO;
import com.jobtrack.model.User;
import com.jobtrack.util.CsrfUtil;
import com.jobtrack.util.PasswordUtil;
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

/**
 * Controller handling user authentication, session fixation prevention,
 * and post-login redirection.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Display flash notifications
        if ("true".equals(req.getParameter("registered"))) {
            req.setAttribute("successMessage", "Account created successfully! Please log in.");
        } else if ("true".equals(req.getParameter("logout"))) {
            req.setAttribute("successMessage", "You have been logged out successfully.");
        }

        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        req.setAttribute("email", email != null ? email.trim() : "");

        if (!ValidationUtil.isNotEmpty(email) || !ValidationUtil.isNotEmpty(password)) {
            req.setAttribute("errorMessage", "Please provide both email and password.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.findByEmail(email.trim());

        if (user == null || !PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            LOGGER.warn("Failed login attempt for email: {}", email.trim());
            req.setAttribute("errorMessage", "Invalid email or password.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // Prevent Session Fixation attacks: invalidate old session and create a fresh one
        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = req.getSession(true);
        newSession.setAttribute("userId", user.getUserId());
        newSession.setAttribute("userName", user.getName());
        newSession.setAttribute("userEmail", user.getEmail());
        newSession.setAttribute("user", user);

        // Generate and bind a fresh CSRF token to the new authenticated session
        CsrfUtil.refreshToken(newSession);

        LOGGER.info("User {} (ID: {}) authenticated successfully.", user.getEmail(), user.getUserId());
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
