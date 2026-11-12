package com.jobtrack.controller;

import com.jobtrack.dao.UserDAO;
import com.jobtrack.model.User;
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
 * Controller handling user registration workflow.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServlet.class);
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        // Retain entered values on validation failure
        req.setAttribute("name", name != null ? name.trim() : "");
        req.setAttribute("email", email != null ? email.trim() : "");

        // 1. Validate full name
        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isValidLength(name, 2, 100)) {
            req.setAttribute("errorMessage", "Please provide a valid full name (2-100 characters).");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        // 2. Validate email format
        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("errorMessage", "Please provide a valid email address.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        // 3. Validate password strength
        if (!ValidationUtil.isStrongPassword(password)) {
            req.setAttribute("errorMessage", ValidationUtil.getPasswordRequirementsMessage());
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        // 4. Confirm password equality
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        // 5. Check duplicate email existence
        if (userDAO.emailExists(email)) {
            req.setAttribute("errorMessage", "An account with this email address already exists.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        // 6. Securely hash password and persist user
        try {
            String passwordHash = PasswordUtil.hashPassword(password);
            User newUser = new User(name.trim(), email.trim().toLowerCase(), passwordHash);

            boolean created = userDAO.registerUser(newUser);
            if (created) {
                LOGGER.info("Successfully registered new user: {}", email.trim().toLowerCase());
                resp.sendRedirect(req.getContextPath() + "/login?registered=true");
            } else {
                req.setAttribute("errorMessage", "Registration failed due to a database error. Please try again.");
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error during registration for email {}", email, e);
            req.setAttribute("errorMessage", "An unexpected system error occurred. Please try again later.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }
}
