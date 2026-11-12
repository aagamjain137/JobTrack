package com.jobtrack.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a registered User in the JobTrack system.
 * Maps to the 'users' database table.
 */
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String name;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    /**
     * Default no-argument constructor.
     */
    public User() {
    }

    /**
     * Convenience constructor for registration and user creation.
     *
     * @param name         user's full name
     * @param email        unique email address
     * @param passwordHash BCrypt hashed password
     */
    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /**
     * Full parameterized constructor matching all table columns.
     *
     * @param userId       unique primary key identifier
     * @param name         user's full name
     * @param email        unique email address
     * @param passwordHash BCrypt hashed password
     * @param createdAt    account creation timestamp
     */
    public User(Integer userId, String name, String email, String passwordHash, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // =========================================================================
    // equals, hashCode, and toString
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) || Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }

    /**
     * String representation of the User.
     * NOTE: passwordHash is strictly excluded to prevent accidental credential leakage in logs.
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
