package com.jobtrack.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 * Uses a work factor (log rounds) of 12 for strong brute-force resistance.
 */
public final class PasswordUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordUtil.class);

    /**
     * Default log rounds (work factor) for BCrypt key derivation.
     * 12 rounds (~4,096 iterations) provides strong defense against modern GPU brute-force attacks.
     */
    public static final int BCRYPT_LOG_ROUNDS = 12;

    private PasswordUtil() {
        // Prevent direct instantiation
    }

    /**
     * Hashes a plain-text password using BCrypt with the default cost factor (12 rounds).
     *
     * @param plainPassword the plain-text password to hash
     * @return the resulting BCrypt hash string
     * @throws IllegalArgumentException if plainPassword is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        String salt = BCrypt.gensalt(BCRYPT_LOG_ROUNDS);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Hashes a plain-text password using BCrypt with a custom cost factor.
     *
     * @param plainPassword the plain-text password to hash
     * @param logRounds the log2 of the number of rounds of hashing to apply (4 to 31)
     * @return the resulting BCrypt hash string
     * @throws IllegalArgumentException if password is null/empty or logRounds is out of bounds
     */
    public static String hashPassword(String plainPassword, int logRounds) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (logRounds < 4 || logRounds > 31) {
            throw new IllegalArgumentException("BCrypt log rounds must be between 4 and 31.");
        }
        String salt = BCrypt.gensalt(logRounds);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Verifies whether a plain-text password matches a stored BCrypt hashed password.
     *
     * @param plainPassword  the raw password supplied during authentication
     * @param hashedPassword the stored BCrypt hash string from the database
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        if (plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid BCrypt hash encountered during password verification: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error("Unexpected error during BCrypt password verification", e);
            return false;
        }
    }
}
