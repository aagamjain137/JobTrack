-- ==============================================================================
-- JobTrack - Application Tracking Platform Database Schema
-- RDBMS        : MySQL 8.0+
-- Storage Engine: InnoDB
-- Character Set: utf8mb4
-- Collation    : utf8mb4_unicode_ci
-- ==============================================================================

-- 1. Create database if it does not already exist
CREATE DATABASE IF NOT EXISTS `jobtrack_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `jobtrack_db`;

-- Optional table cleanup for clean re-runs
-- Disabled checks ensure cascading drops execute without foreign key conflicts
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `interviews`;
DROP TABLE IF EXISTS `applications`;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================
-- Table: users
-- Description: Stores registered user accounts with credentials and profile info.
-- Security: Passwords MUST be hashed (e.g. BCrypt) before storage. Plain-text is prohibited.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS `users` (
    `user_id` INT AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `pk_users` PRIMARY KEY (`user_id`),
    CONSTRAINT `uq_users_email` UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- Table: applications
-- Description: Stores job applications created by users with status workflow.
-- Relational constraints: ON DELETE CASCADE with parent user.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS `applications` (
    `application_id` INT AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `company_name` VARCHAR(150) NOT NULL,
    `job_role` VARCHAR(150) NOT NULL,
    `location` VARCHAR(100) NOT NULL,
    `salary` DECIMAL(10, 2) DEFAULT NULL,
    `job_description` TEXT DEFAULT NULL,
    `job_url` VARCHAR(500) DEFAULT NULL,
    `applied_date` DATE NOT NULL,
    `status` ENUM('APPLIED', 'ASSESSMENT', 'INTERVIEW', 'SELECTED', 'REJECTED') NOT NULL DEFAULT 'APPLIED',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_applications` PRIMARY KEY (`application_id`),
    CONSTRAINT `fk_applications_user` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`user_id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX `idx_applications_user_id` (`user_id`),
    INDEX `idx_applications_status` (`status`),
    INDEX `idx_applications_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- Table: interviews
-- Description: Tracks multiple interview rounds, dates, and feedback per application.
-- Relational constraints: ON DELETE CASCADE with parent job application.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS `interviews` (
    `interview_id` INT AUTO_INCREMENT,
    `application_id` INT NOT NULL,
    `interview_round` VARCHAR(100) NOT NULL,
    `interview_date` DATETIME NOT NULL,
    `remarks` TEXT DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `pk_interviews` PRIMARY KEY (`interview_id`),
    CONSTRAINT `fk_interviews_application` FOREIGN KEY (`application_id`)
        REFERENCES `applications` (`application_id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX `idx_interviews_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- Seed Data (Development / Demonstration)
-- SECURITY NOTE: Passwords are encrypted using BCrypt ($2a$10$...).
-- Plain-text passwords are NEVER stored in this schema.
-- Demo credentials:
--   Email    : demo@jobtrack.com
--   Password : Password123
-- ==============================================================================
INSERT INTO `users` (`user_id`, `name`, `email`, `password_hash`, `created_at`)
VALUES (
    1,
    'Demo User',
    'demo@jobtrack.com',
    '$2a$10$wT0YwF4E1UG.3K8y6X91.eyE6sUe7OqM0W5jHjR1u.h5J6S3Xm7C2',
    NOW()
) ON DUPLICATE KEY UPDATE `email` = `email`;

INSERT INTO `applications` (
    `application_id`,
    `user_id`,
    `company_name`,
    `job_role`,
    `location`,
    `salary`,
    `job_description`,
    `job_url`,
    `applied_date`,
    `status`
) VALUES (
    1,
    1,
    'Google',
    'Senior Software Engineer - Java / Cloud',
    'Mountain View, CA (Hybrid)',
    185000.00,
    'Design and build high-throughput backend services using modern Java and distributed systems.',
    'https://careers.google.com/jobs/results/sample-job',
    CURDATE(),
    'INTERVIEW'
) ON DUPLICATE KEY UPDATE `company_name` = `company_name`;

INSERT INTO `interviews` (
    `interview_id`,
    `application_id`,
    `interview_round`,
    `interview_date`,
    `remarks`
) VALUES (
    1,
    1,
    'Technical Screen - System Design & Concurrency',
    DATE_ADD(NOW(), INTERVAL 3 DAY),
    'Cover distributed caching, connection pooling, and multi-threading architecture.'
) ON DUPLICATE KEY UPDATE `interview_round` = `interview_round`;
