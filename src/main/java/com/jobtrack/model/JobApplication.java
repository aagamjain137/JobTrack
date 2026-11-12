package com.jobtrack.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a Job Application in the JobTrack system.
 * Maps to the 'applications' database table.
 */
public class JobApplication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer applicationId;
    private Integer userId;
    private String companyName;
    private String jobRole;
    private String location;
    private BigDecimal salary;
    private String jobDescription;
    private String jobUrl;
    private LocalDate appliedDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default no-argument constructor.
     */
    public JobApplication() {
    }

    /**
     * Convenience constructor for creating a new job application.
     *
     * @param userId         foreign key reference to the owner User
     * @param companyName    name of hiring organization
     * @param jobRole        role or title applied for
     * @param location       office or remote location
     * @param salary         optional compensation amount (nullable)
     * @param jobDescription details or job posting requirements
     * @param jobUrl         external URL to the job listing (nullable)
     * @param appliedDate    date application was submitted
     * @param status         application state (e.g. APPLIED, ASSESSMENT, INTERVIEW, SELECTED, REJECTED)
     */
    public JobApplication(Integer userId, String companyName, String jobRole, String location,
                          BigDecimal salary, String jobDescription, String jobUrl,
                          LocalDate appliedDate, String status) {
        this.userId = userId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.location = location;
        this.salary = salary;
        this.jobDescription = jobDescription;
        this.jobUrl = jobUrl;
        this.appliedDate = appliedDate;
        this.status = status;
    }

    /**
     * Full parameterized constructor matching all table columns.
     *
     * @param applicationId  unique primary key identifier
     * @param userId         foreign key reference to the owner User
     * @param companyName    name of hiring organization
     * @param jobRole        role or title applied for
     * @param location       office or remote location
     * @param salary         optional compensation amount (nullable)
     * @param jobDescription details or job posting requirements
     * @param jobUrl         external URL to the job listing (nullable)
     * @param appliedDate    date application was submitted
     * @param status         application state
     * @param createdAt      record creation timestamp
     * @param updatedAt      record last updated timestamp
     */
    public JobApplication(Integer applicationId, Integer userId, String companyName, String jobRole,
                          String location, BigDecimal salary, String jobDescription, String jobUrl,
                          LocalDate appliedDate, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.location = location;
        this.salary = salary;
        this.jobDescription = jobDescription;
        this.jobUrl = jobUrl;
        this.appliedDate = appliedDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // equals, hashCode, and toString
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobApplication that = (JobApplication) o;
        return Objects.equals(applicationId, that.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId);
    }

    @Override
    public String toString() {
        return "JobApplication{" +
                "applicationId=" + applicationId +
                ", userId=" + userId +
                ", companyName='" + companyName + '\'' +
                ", jobRole='" + jobRole + '\'' +
                ", location='" + location + '\'' +
                ", salary=" + salary +
                ", jobUrl='" + jobUrl + '\'' +
                ", appliedDate=" + appliedDate +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
