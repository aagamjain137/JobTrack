package com.jobtrack.util;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Server-side input validation and sanitization utility for JobTrack.
 * Validates emails, strong passwords, non-empty text, numeric boundaries,
 * decimal currency (salary), and dates/times.
 */
public final class ValidationUtil {

    // RFC 5322 compliant email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Strong password: >=8 chars, at least one uppercase, lowercase, digit, and special char
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>\\-_=+\\[\\]~`'/\\\\]");

    // Strict date formatters (ResolverStyle.STRICT prevents overflow e.g. Feb 30 -> Mar 2)
    private static final DateTimeFormatter STRICT_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter STRICT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter STRICT_DATETIME_SECONDS_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    // Allowed Application status enum values matching MySQL schema
    private static final Set<String> ALLOWED_APPLICATION_STATUSES = Set.of(
            "APPLIED", "ASSESSMENT", "INTERVIEW", "SELECTED", "REJECTED"
    );

    // Maximum DECIMAL(10,2) bound in MySQL: 99,999,999.99
    private static final BigDecimal MAX_SALARY = new BigDecimal("99999999.99");

    private ValidationUtil() {
        // Prevent direct instantiation
    }

    // =========================================================================
    // String & Text Validations
    // =========================================================================

    /**
     * Checks if a string is non-null, non-empty, and contains at least one non-whitespace character.
     *
     * @param str the string to test
     * @return true if valid and non-blank; false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Checks if a string length falls within specified inclusive minimum and maximum bounds.
     *
     * @param str       the string to validate
     * @param minLength minimum acceptable character length
     * @param maxLength maximum acceptable character length
     * @return true if within bounds; false otherwise
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return minLength == 0;
        }
        int len = str.trim().length();
        return len >= minLength && len <= maxLength;
    }

    // =========================================================================
    // Email Validation
    // =========================================================================

    /**
     * Validates an email address against RFC 5322 regex and maximum database length (150 chars).
     *
     * @param email user supplied email string
     * @return true if valid email format; false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > 150) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }

    // =========================================================================
    // Password Strength Validation
    // =========================================================================

    /**
     * Checks whether a password meets security strength requirements:
     * - Minimum 8 characters, maximum 100 characters
     * - At least one uppercase letter (A-Z)
     * - At least one lowercase letter (a-z)
     * - At least one numerical digit (0-9)
     * - At least one special symbol (!@#$%^&*...)
     *
     * @param password the plain-text password to evaluate
     * @return true if all criteria are satisfied; false otherwise
     */
    public static boolean isStrongPassword(String password) {
        if (password == null) {
            return false;
        }
        if (password.length() < 8 || password.length() > 100) {
            return false;
        }
        return PASSWORD_UPPERCASE.matcher(password).find()
                && PASSWORD_LOWERCASE.matcher(password).find()
                && PASSWORD_DIGIT.matcher(password).find()
                && PASSWORD_SPECIAL.matcher(password).find();
    }

    /**
     * Provides user-friendly instructions on password requirements.
     *
     * @return message detailing password policy
     */
    public static String getPasswordRequirementsMessage() {
        return "Password must be 8-100 characters long and include at least one uppercase letter, " +
               "one lowercase letter, one digit, and one special symbol.";
    }

    // =========================================================================
    // Salary / Numeric Validation (BigDecimal)
    // =========================================================================

    /**
     * Validates that a {@link BigDecimal} salary is strictly positive (> 0.00),
     * has at most 2 decimal places, and fits within the DECIMAL(10,2) database column.
     *
     * @param salary the salary value to test
     * @return true if valid strictly positive salary; false otherwise
     */
    public static boolean isValidPositiveSalary(BigDecimal salary) {
        if (salary == null) {
            return false;
        }
        if (salary.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (salary.compareTo(MAX_SALARY) > 0) {
            return false;
        }
        return salary.scale() <= 2;
    }

    /**
     * Validates whether a raw string represents a valid strictly positive decimal salary.
     *
     * @param salaryStr the string representation of salary
     * @return true if parsable and positive; false otherwise
     */
    public static boolean isValidPositiveSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return false;
        }
        try {
            BigDecimal bd = new BigDecimal(salaryStr.trim());
            return isValidPositiveSalary(bd);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates an optional salary field: returns true if null/blank, or if provided,
     * validates that it is a positive amount.
     *
     * @param salaryStr string salary representation or null
     * @return true if empty or valid positive salary; false otherwise
     */
    public static boolean isValidOptionalSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return true;
        }
        return isValidPositiveSalary(salaryStr);
    }

    /**
     * Safely parses a string into a {@link BigDecimal} with scale 2.
     *
     * @param salaryStr the input string
     * @return parsed BigDecimal, or null if string is empty or invalid
     */
    public static BigDecimal parseSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(salaryStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =========================================================================
    // Date & Time Validation
    // =========================================================================

    /**
     * Validates a date string against strict ISO local date format (yyyy-MM-dd).
     *
     * @param dateStr the date string to test
     * @return true if valid calendar date; false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim(), STRICT_DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Safely parses a date string in yyyy-MM-dd format into a {@link LocalDate}.
     *
     * @param dateStr the input date string
     * @return parsed LocalDate or null if invalid
     */
    public static LocalDate parseDate(String dateStr) {
        if (!isValidDate(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), STRICT_DATE_FORMATTER);
    }

    /**
     * Validates a datetime string against HTML5 datetime-local format (yyyy-MM-dd'T'HH:mm)
     * or standard SQL datetime format (yyyy-MM-dd HH:mm:ss).
     *
     * @param dateTimeStr the datetime string to validate
     * @return true if valid datetime; false otherwise
     */
    public static boolean isValidDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return false;
        }
        String trimmed = dateTimeStr.trim();
        try {
            LocalDateTime.parse(trimmed, STRICT_DATETIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e1) {
            try {
                LocalDateTime.parse(trimmed, STRICT_DATETIME_SECONDS_FORMATTER);
                return true;
            } catch (DateTimeParseException e2) {
                return false;
            }
        }
    }

    /**
     * Safely parses a datetime string into a {@link LocalDateTime}.
     *
     * @param dateTimeStr input datetime string
     * @return parsed LocalDateTime or null if invalid
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateTimeStr.trim();
        try {
            return LocalDateTime.parse(trimmed, STRICT_DATETIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(trimmed, STRICT_DATETIME_SECONDS_FORMATTER);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    // =========================================================================
    // Domain & Application Enum Validation
    // =========================================================================

    /**
     * Validates if the given status matches one of the allowed application status values.
     *
     * @param status the status string to test
     * @return true if matches APPLIED, ASSESSMENT, INTERVIEW, SELECTED, or REJECTED
     */
    public static boolean isValidApplicationStatus(String status) {
        if (status == null) {
            return false;
        }
        return ALLOWED_APPLICATION_STATUSES.contains(status.trim().toUpperCase());
    }

    /**
     * Validates an optional URL (e.g. job posting link) up to 500 characters.
     *
     * @param url the URL string to test
     * @return true if empty/null or a valid HTTP/HTTPS URL
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true; // Optional field
        }
        String trimmed = url.trim();
        if (trimmed.length() > 500) {
            return false;
        }
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Basic sanitization helper that trims whitespace and returns empty string if null.
     *
     * @param input the input string
     * @return trimmed string or empty string
     */
    public static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }
}
