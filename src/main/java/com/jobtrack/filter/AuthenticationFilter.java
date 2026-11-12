package com.jobtrack.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Security filter enforcing session authentication across all protected application routes.
 * Registered in web.xml: com.jobtrack.filter.AuthenticationFilter
 *
 * Publicly allowed endpoints:
 * - Landing root: "/" or "/index.jsp"
 * - Authentication: "/login", "/register"
 * - Static assets: "/css/*", "/js/*", "/images/*", "/favicon.ico"
 */
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthenticationFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI();
        String path = uri.substring(contextPath.length());

        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("userId") != null);

        if (!isAuthenticated) {
            LOGGER.debug("Unauthenticated access attempt to '{}' - redirecting to /login", path);
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        // Apply anti-caching HTTP response headers on protected pages
        // Prevents browser back-button from displaying sensitive information post-logout
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthenticationFilter destroyed.");
    }

    /**
     * Determines whether the requested path is exempt from authentication checks.
     *
     * @param path servlet path relative to application context
     * @return true if resource is public, false if protected
     */
    private boolean isPublicResource(String path) {
        if (path == null || path.isEmpty() || path.equals("/") || path.equals("/index.jsp")) {
            return true;
        }

        // Authentication endpoints
        if (path.equals("/login") || path.startsWith("/login/") ||
            path.equals("/register") || path.startsWith("/register/")) {
            return true;
        }

        // Static asset directories
        if (path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/images/") || path.equals("/favicon.ico")) {
            return true;
        }

        return false;
    }
}
