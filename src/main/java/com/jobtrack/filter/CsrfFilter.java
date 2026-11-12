package com.jobtrack.filter;

import com.jobtrack.util.CsrfUtil;
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
import java.util.Set;

/**
 * Filter providing Cross-Site Request Forgery (CSRF) protection across all web endpoints.
 * Registered in web.xml: com.jobtrack.filter.CsrfFilter
 *
 * Enforces:
 * - On GET / safe requests: guarantees an active CSRF token is stored in the session and request attribute.
 * - On state-changing requests (POST, PUT, DELETE): validates submitted token against session token.
 *   Rejects unauthorized or mismatched tokens with HTTP 403 Forbidden.
 */
public class CsrfFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("CsrfFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod().toUpperCase();
        String contextPath = httpRequest.getContextPath();
        String path = httpRequest.getRequestURI().substring(contextPath.length());

        // Exempt static assets from CSRF checks
        if (isStaticAsset(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (SAFE_METHODS.contains(method)) {
            // For safe requests, guarantee that a valid CSRF token is attached to the session and request
            HttpSession session = httpRequest.getSession(true);
            String token = CsrfUtil.getOrCreateToken(session);
            httpRequest.setAttribute(CsrfUtil.CSRF_PARAM_NAME, token);
            chain.doFilter(request, response);
            return;
        }

        // For state-changing methods (POST, PUT, DELETE, PATCH), validate token
        boolean isValid = CsrfUtil.validateRequest(httpRequest);
        if (!isValid) {
            LOGGER.warn("CSRF token validation failed for {} {} from IP {}", method, path, httpRequest.getRemoteAddr());
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF security token.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOGGER.info("CsrfFilter destroyed.");
    }

    private boolean isStaticAsset(String path) {
        if (path == null) {
            return false;
        }
        return path.startsWith("/css/") || path.startsWith("/js/") ||
               path.startsWith("/images/") || path.equals("/favicon.ico");
    }
}
