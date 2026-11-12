package com.jobtrack.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cross-Site Request Forgery (CSRF) protection utility.
 * Generates cryptographically secure 256-bit entropy tokens and provides
 * constant-time verification against timing attacks using {@link MessageDigest#isEqual(byte[], byte[])}.
 */
public final class CsrfUtil {

    /**
     * Session attribute name used to store the user's CSRF token.
     */
    public static final String CSRF_SESSION_ATTR = "csrfToken";

    /**
     * Standard HTTP request parameter name expected in POST/PUT/DELETE forms.
     */
    public static final String CSRF_PARAM_NAME = "csrfToken";

    /**
     * Alternative request parameter name commonly used by frameworks.
     */
    public static final String CSRF_PARAM_NAME_ALT = "_csrf";

    /**
     * Standard HTTP header name for AJAX / fetch API requests.
     */
    public static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    /**
     * 32 bytes (256 bits) provides high cryptographic entropy.
     */
    private static final int TOKEN_BYTE_LENGTH = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfUtil() {
        // Prevent direct instantiation
    }

    /**
     * Generates a cryptographically strong, URL-safe Base64 encoded token.
     *
     * @return 43-character URL-safe string containing 256 bits of entropy
     */
    public static String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Validates that the request token matches the session token using constant-time comparison
     * to protect against side-channel timing attacks.
     *
     * @param requestToken the token received from the client request (parameter or header)
     * @param sessionToken the authoritative token stored in the server session
     * @return true if both tokens are non-null and match byte-for-byte; false otherwise
     */
    public static boolean validateToken(String requestToken, String sessionToken) {
        if (requestToken == null || sessionToken == null) {
            return false;
        }
        if (requestToken.isBlank() || sessionToken.isBlank()) {
            return false;
        }

        byte[] requestBytes = requestToken.getBytes(StandardCharsets.UTF_8);
        byte[] sessionBytes = sessionToken.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(requestBytes, sessionBytes);
    }

    /**
     * Retrieves the current CSRF token from the session, or generates and stores a new one if absent.
     *
     * @param session the current user's {@link HttpSession}
     * @return the active CSRF token string
     * @throws IllegalArgumentException if session is null
     */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null when retrieving or generating CSRF token.");
        }

        synchronized (session) {
            String token = (String) session.getAttribute(CSRF_SESSION_ATTR);
            if (token == null || token.isBlank()) {
                token = generateToken();
                session.setAttribute(CSRF_SESSION_ATTR, token);
            }
            return token;
        }
    }

    /**
     * Regenerates a new CSRF token and associates it with the session.
     * Recommended upon authentication state transitions (e.g. login) to prevent session fixation.
     *
     * @param session the user's {@link HttpSession}
     * @return the freshly generated CSRF token
     */
    public static String refreshToken(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null when refreshing CSRF token.");
        }

        synchronized (session) {
            String newToken = generateToken();
            session.setAttribute(CSRF_SESSION_ATTR, newToken);
            return newToken;
        }
    }

    /**
     * Validates an incoming HTTP request by extracting the submitted token from
     * headers or parameters and comparing it against the session token.
     *
     * @param request the current {@link HttpServletRequest}
     * @return true if valid; false if session missing, token missing, or mismatch
     */
    public static boolean validateRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionToken == null || sessionToken.isBlank()) {
            return false;
        }

        // 1. Check HTTP header first (for AJAX requests)
        String requestToken = request.getHeader(CSRF_HEADER_NAME);

        // 2. Fall back to form parameter 'csrfToken'
        if (requestToken == null || requestToken.isBlank()) {
            requestToken = request.getParameter(CSRF_PARAM_NAME);
        }

        // 3. Fall back to alternative parameter '_csrf'
        if (requestToken == null || requestToken.isBlank()) {
            requestToken = request.getParameter(CSRF_PARAM_NAME_ALT);
        }

        return validateToken(requestToken, sessionToken);
    }
}
