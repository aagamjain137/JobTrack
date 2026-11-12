package com.jobtrack.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing an Interview round associated with a Job Application.
 * Maps to the 'interviews' database table.
 */
public class Interview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer interviewId;
    private Integer applicationId;
    private String interviewRound;
    private LocalDateTime interviewDate;
    private String remarks;
    private LocalDateTime createdAt;

    // Joined display fields (from applications table)
    private String companyName;
    private String jobRole;

    /**
     * Default no-argument constructor.
     */
    public Interview() {
    }

    /**
     * Convenience constructor for scheduling a new interview.
     *
     * @param applicationId  foreign key reference to parent JobApplication
     * @param interviewRound title or stage (e.g. "Technical Round 1", "HR Screen", "System Design")
     * @param interviewDate  scheduled date and time of the interview
     * @param remarks        notes, interviewer feedback, or preparation instructions (nullable)
     */
    public Interview(Integer applicationId, String interviewRound, LocalDateTime interviewDate, String remarks) {
        this.applicationId = applicationId;
        this.interviewRound = interviewRound;
        this.interviewDate = interviewDate;
        this.remarks = remarks;
    }

    /**
     * Full parameterized constructor matching all table columns.
     *
     * @param interviewId    unique primary key identifier
     * @param applicationId  foreign key reference to parent JobApplication
     * @param interviewRound title or stage
     * @param interviewDate  scheduled date and time of the interview
     * @param remarks        notes, interviewer feedback, or preparation instructions
     * @param createdAt      record creation timestamp
     */
    public Interview(Integer interviewId, Integer applicationId, String interviewRound,
                     LocalDateTime interviewDate, String remarks, LocalDateTime createdAt) {
        this.interviewId = interviewId;
        this.applicationId = applicationId;
        this.interviewRound = interviewRound;
        this.interviewDate = interviewDate;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public Integer getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Integer interviewId) {
        this.interviewId = interviewId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(String interviewRound) {
        this.interviewRound = interviewRound;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    // =========================================================================
    // equals, hashCode, and toString
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interview interview = (Interview) o;
        return Objects.equals(interviewId, interview.interviewId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interviewId);
    }

    @Override
    public String toString() {
        return "Interview{" +
                "interviewId=" + interviewId +
                ", applicationId=" + applicationId +
                ", interviewRound='" + interviewRound + '\'' +
                ", interviewDate=" + interviewDate +
                ", remarks='" + remarks + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
