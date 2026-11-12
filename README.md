# JobTrack &mdash; Enterprise Job Application & Interview Tracking Platform

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10%20(Servlet%206.0)-blue.svg?logo=eclipse&logoColor=white)](https://jakarta.ee/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![HikariCP](https://img.shields.io/badge/HikariCP-5.1.0-brightgreen.svg)](https://github.com/brettwooldridge/HikariCP)
[![Tomcat](https://img.shields.io/badge/Apache%20Tomcat-10.1+-F8DC75.svg?logo=apachetomcat&logoColor=black)](https://tomcat.apache.org/)
[![Build](https://img.shields.io/badge/Maven-Build%20Success-success.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

---

## 1. Project Overview & Key Features

**JobTrack** is a production-grade, secure, multi-tenant web application engineered to help career seekers manage, organize, and accelerate their hiring pipeline. Built with **modern Java 21 LTS** and **Jakarta EE 10**, it provides an intuitive, high-performance interface with enterprise-class architectural patterns, robust connection pooling, and multi-layered defense-in-depth security.

### Key Capabilities
- **Multi-Tenant User Isolation:** Secure authentication and complete data isolation preventing Insecure Direct Object Reference (IDOR) attacks across all user records.
- **Dynamic Application Pipeline:** Track job applications through full lifecycle workflows: `APPLIED`, `ASSESSMENT`, `INTERVIEW`, `SELECTED`, and `REJECTED`.
- **Multi-Round Interview Tracker:** Schedule and monitor multiple technical, behavioral, and HR interview stages per application with timestamps and actionable notes.
- **Real-Time Analytics Dashboard:** Instant KPI metric cards displaying live application counts, status breakdowns, and chronologically sorted upcoming interview alerts.
- **Search & Filter Engine:** Real-time parameterized searching across company names and job titles with granular status filtering.
- **Post-Redirect-Get (PRG) Pattern:** Seamless user experience preventing duplicate form submissions upon page reload.
- **Zero-Dependency Modern UI:** High-performance, responsive SaaS interface styled with CSS custom properties and lightweight Vanilla JavaScript without bloated third-party frameworks.

---

## 2. Technology Stack

| Layer | Technology | Specification / Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | **Java 21 LTS** | Core backend logic, modern pattern matching, and strong typing |
| **Web Platform** | Jakarta EE | **Jakarta Servlet 6.0 / JSP 3.1** | Non-blocking MVC controllers and templating engine |
| **Tag Library** | Jakarta JSTL | **JSTL 3.0** (`jakarta.tags.core`, `fmt`) | Secure view rendering, data formatting, and XSS defense |
| **Database** | MySQL | **MySQL 8.0+** | Relational data persistence with InnoDB engine & UTF-8 (utf8mb4) |
| **Connection Pool** | HikariCP | **HikariCP 5.1.0** | High-throughput, zero-overhead JDBC connection management |
| **Security** | jBCrypt | **jBCrypt 0.4** | Adaptive password hashing with cost factor (12 log rounds) |
| **Application Server** | Apache Tomcat | **Tomcat 10.1.x+** | Jakarta EE 10 compliant Servlet container |
| **Build Tool** | Apache Maven | **Maven 3.9+** | Dependency management, build automation, and WAR packaging |
| **Frontend** | CSS3 & HTML5 | **Vanilla CSS & JS** | Responsive Flexbox/Grid dashboard with dark/light visual harmony |

---

## 3. System Architecture

The application implements a clean **Model-View-Controller (MVC)** architectural pattern combined with a **Data Access Object (DAO)** abstraction layer and an **Ordered Filter Chain**.

```
                           HTTP Request (Browser / Client)
                                         │
                                         ▼
 ┌───────────────────────────────────────────────────────────────────────────────┐
 │                            ORDERED SECURITY FILTERS                           │
 │                                                                               │
 │  1. AuthenticationFilter (com.jobtrack.filter.AuthenticationFilter)           │
 │     • Route guarding: Whitelists /login, /register, and static assets (/css) │
 │     • Session validation: Redirects unauthenticated access to /login          │
 │     • Header injection: Cache-Control: no-cache, no-store, must-revalidate    │
 │                                                                               │
 │  2. CsrfFilter (com.jobtrack.filter.CsrfFilter)                               │
 │     • GET requests: Auto-attaches cryptographically secure CSRF session token │
 │     • POST/PUT/DELETE: Constant-time validation (HTTP 403 Forbidden on check) │
 └───────────────────────────────────────┬───────────────────────────────────────┘
                                         │
                                         ▼
 ┌───────────────────────────────────────────────────────────────────────────────┐
 │                           SERVLET CONTROLLER LAYER                            │
 │                        (com.jobtrack.controller)                              │
 │                                                                               │
 │   • RegisterServlet    (/register)     • LoginServlet      (/login)           │
 │   • LogoutServlet      (/logout)       • DashboardServlet  (/dashboard)       │
 │   • ApplicationServlet (/applications) • InterviewServlet  (/interviews)      │
 └───────────────────────┬───────────────────────────────▲───────────────────────┘
                         │                               │
        Business Logic & │               Model Injection │ Post-Redirect-Get
        Input Validation ▼                               │ Forward to Views
 ┌───────────────────────────────────────┐       ┌───────┴───────────────────────┐
 │            UTILITY & CORE             │       │         VIEW LAYER            │
 │          (com.jobtrack.util)          │       │    (src/main/webapp/WEB-INF)  │
 │  • ValidationUtil  • CsrfUtil         │       │  • dashboard.jsp              │
 │  • PasswordUtil    • DBConnection     │       │  • application-form.jsp       │
 │  • DatabaseContextListener            │       │  • application-details.jsp    │
 └───────────────────┬───────────────────┘       │  • login.jsp / register.jsp   │
                     │                           │  • includes/header & footer   │
                     ▼                           └───────────────────────────────┘
 ┌───────────────────────────────────────┐
 │        DATA ACCESS OBJECTS (DAO)      │
 │          (com.jobtrack.dao)           │
 │  • UserDAO        • ApplicationDAO    │
 │  • InterviewDAO   • Domain Models     │
 └───────────────────┬───────────────────┘
                     │
                     ▼ Pooled JDBC Connections (HikariCP)
 ┌───────────────────────────────────────────────────────────────────────────────┐
 │                           DATABASE LAYER (MySQL 8.0)                          │
 │                                                                               │
 │      ┌─────────────────┐       ┌─────────────────┐      ┌─────────────────┐   │
 │      │      users      │ 1   * │  applications   │ 1  * │   interviews    │   │
 │      │─────────────────│───────│─────────────────│──────│─────────────────│   │
 │      │ PK  user_id     │◄──────│ FK  user_id     │◄─────│ FK  app_id      │   │
 │      │     email (UQ)  │  ON   │ PK  app_id      │  ON  │ PK  interview_id│   │
 │      │     password    │ DELETE│     status      │DELETE│     round       │   │
 │      │     created_at  │CASCADE│     salary      │CASCADE     date        │   │
 │      └─────────────────┘       └─────────────────┘      └─────────────────┘   │
 └───────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Enterprise Security Highlights

| Vulnerability Vector | Defense Mechanism | Implementation Details |
| :--- | :--- | :--- |
| **Credential Storage** | **BCrypt Salted Hashes** | User passwords are never stored in plain text. `PasswordUtil` applies **12 log rounds** (~4,096 iterations) of BCrypt salting and hashing. |
| **SQL Injection (SQLi)** | **100% PreparedStatements** | All database queries across `UserDAO`, `ApplicationDAO`, and `InterviewDAO` use parameterized `PreparedStatement` interfaces. String concatenation in SQL statements is strictly avoided. |
| **Cross-Site Request Forgery (CSRF)** | **Synchronizer Token Pattern** | `CsrfUtil` generates 256-bit cryptographically random tokens via `SecureRandom`. Validations use constant-time byte comparisons (`MessageDigest.isEqual`) to neutralize timing attacks. |
| **Cross-Site Scripting (XSS)** | **JSTL Contextual Escaping** | All dynamic user data in JSPs is wrapped in `<c:out value="${...}" />` to encode HTML control characters (`<`, `>`, `&`, `"`, `'`). |
| **Session Fixation** | **Session Invalidation** | Upon successful authentication, `LoginServlet` invalidates the client's old HTTP session, issues a fresh session identifier, and binds a new CSRF token. |
| **Insecure Direct Object Reference (IDOR)** | **Tenant Ownership Scoping** | All application and interview mutations are strictly checked against `session.getAttribute("userId")` via `WHERE application_id = ? AND user_id = ?`. Unauthorized queries return 0 affected rows. |
| **Cache Leakage** | **HTTP Anti-Caching Headers** | `AuthenticationFilter` sets `Cache-Control: no-cache, no-store, must-revalidate`, preventing browser history or back-button caching post-logout. |

---

## 5. Database Schema & Relational Design

The database schema is defined in [`database/jobtrack_schema.sql`](database/jobtrack_schema.sql):

```sql
-- Database Initialization
CREATE DATABASE IF NOT EXISTS `jobtrack_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `jobtrack_db`;
```

### Table Definitions

#### 1. `users`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `user_id` | `INT` | `PRIMARY KEY AUTO_INCREMENT` | Unique user identifier |
| `name` | `VARCHAR(100)` | `NOT NULL` | User's full name |
| `email` | `VARCHAR(150)` | `NOT NULL UNIQUE` | User's email (login username) |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | Salted BCrypt password hash |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Account registration timestamp |

#### 2. `applications`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `application_id` | `INT` | `PRIMARY KEY AUTO_INCREMENT` | Unique application identifier |
| `user_id` | `INT` | `NOT NULL, FK -> users` | Foreign key reference (**ON DELETE CASCADE**) |
| `company_name` | `VARCHAR(150)` | `NOT NULL` | Organization name |
| `job_role` | `VARCHAR(150)` | `NOT NULL` | Job role or title |
| `location` | `VARCHAR(100)` | `NOT NULL` | Job location (e.g. Remote, City) |
| `salary` | `DECIMAL(10,2)`| `NULL` | Compensation amount |
| `job_description`| `TEXT` | `NULL` | Full description & requirements |
| `job_url` | `VARCHAR(500)` | `NULL` | External job posting link |
| `applied_date` | `DATE` | `NOT NULL` | Submission date |
| `status` | `ENUM` | `DEFAULT 'APPLIED'` | `'APPLIED'`,`'ASSESSMENT'`,`'INTERVIEW'`,`'SELECTED'`,`'REJECTED'` |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | `ON UPDATE CURRENT_TIMESTAMP` | Last updated timestamp |

*Indexes:* `idx_applications_user_id`, `idx_applications_status`, `idx_applications_user_status`.

#### 3. `interviews`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `interview_id` | `INT` | `PRIMARY KEY AUTO_INCREMENT` | Unique interview round identifier |
| `application_id` | `INT` | `NOT NULL, FK -> applications` | Foreign key reference (**ON DELETE CASCADE**) |
| `interview_round`| `VARCHAR(100)` | `NOT NULL` | Round name (e.g. "System Design Screen") |
| `interview_date` | `DATETIME` | `NOT NULL` | Scheduled interview date and time |
| `remarks` | `TEXT` | `NULL` | Preparation notes, links, or feedback |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Scheduling timestamp |

*Indexes:* `idx_interviews_application_id`.

---

## 6. Local Setup & Deployment Guide

### Prerequisites
- **JDK 21 LTS** installed and configured on your `PATH` (`java -version`).
- **Apache Maven 3.9+** (`mvn -v`).
- **MySQL Server 8.0+** running locally or remotely.
- **Apache Tomcat 10.1+** (supporting Jakarta EE 10 / Servlet 6.0).

---

### Step 1: Clone or Open Project
```powershell
git clone https://github.com/aagamjain137/JobTrack.git
cd JobTrack
```

---

### Step 2: Initialize MySQL Database
Execute the database initialization script using MySQL CLI or MySQL Workbench:

```powershell
mysql -u root -p < database\jobtrack_schema.sql
```
*Or execute inside the MySQL interactive shell:*
```sql
SOURCE database/jobtrack_schema.sql;
```

> **Default Seed User Account:**
> - **Email:** `demo@jobtrack.com`
> - **Password:** `Password123` *(stored strictly as salted BCrypt hash `$2a$10$...`)*

---

### Step 3: Configure Database Connection
Copy `db.properties.example` to `src/main/resources/db.properties` and provide your MySQL credentials:

```properties
# src/main/resources/db.properties
db.url=jdbc:mysql://localhost:3306/jobtrack_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
db.username=root
db.password=YOUR_MYSQL_PASSWORD_HERE
db.driver=com.mysql.cj.jdbc.Driver

# HikariCP Pool Configuration
hikari.poolName=JobTrackHikariPool
hikari.maximumPoolSize=10
hikari.minimumIdle=2
hikari.idleTimeout=30000
hikari.connectionTimeout=20000
hikari.maxLifetime=1800000
```

> `db.properties` is configured in `.gitignore` to prevent committing private credentials.

---

### Step 4: Build Application WAR with Maven
Compile, validate, and package the WAR file using Java 21:

```powershell
mvn clean package
```
*A successful build generates `target/jobtrack.war`.*

---

### Step 5: Deploy to Apache Tomcat 10.1+

#### Option A: Deploy via Tomcat `webapps` Directory
1. Copy the generated `target/jobtrack.war` file to your Tomcat `webapps` folder:
   ```powershell
   Copy-Item target\jobtrack.war -Destination "$TOMCAT_HOME\webapps\"
   ```
2. Start Apache Tomcat:
   ```powershell
   & "$TOMCAT_HOME\bin\startup.bat"
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:8080/jobtrack
   ```

#### Option B: Deploy in IntelliJ IDEA / Eclipse
1. Create a new **Tomcat 10.1+** Run/Debug Configuration.
2. In the **Deployment** tab, add `jobtrack:war` or `jobtrack:war exploded`.
3. Set the Application context path to `/jobtrack` (or `/`).
4. Click **Run** or **Debug**.

---

## 7. Project Directory Structure

```
D:\JobTrack/
├── pom.xml                               # Maven project configuration (Java 21, Jakarta EE 10)
├── README.md                             # Architecture, schema, and deployment manual
├── database/
│   └── jobtrack_schema.sql               # MySQL 8.0 schema & seed data
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── jobtrack/
        │           ├── controller/       # MVC Servlet Controllers (PRG pattern)
        │           │   ├── ApplicationServlet.java
        │           │   ├── DashboardServlet.java
        │           │   ├── InterviewServlet.java
        │           │   ├── LoginServlet.java
        │           │   ├── LogoutServlet.java
        │           │   └── RegisterServlet.java
        │           ├── dao/              # JDBC Data Access Objects (PreparedStatements)
        │           │   ├── ApplicationDAO.java
        │           │   ├── InterviewDAO.java
        │           │   └── UserDAO.java
        │           ├── filter/           # Security & Request Lifecycle Filters
        │           │   ├── AuthenticationFilter.java
        │           │   └── CsrfFilter.java
        │           ├── model/            # Domain POJO Model Entities
        │           │   ├── Interview.java
        │           │   ├── JobApplication.java
        │           │   └── User.java
        │           └── util/             # Security, Validation & Connection Utilities
        │               ├── CsrfUtil.java
        │               ├── DatabaseContextListener.java
        │               ├── DBConnection.java
        │               ├── PasswordUtil.java
        │               └── ValidationUtil.java
        ├── resources/
        │   ├── db.properties             # Local database configuration (git-ignored)
        │   └── db.properties.example     # Template for database configuration
        └── webapp/
            ├── index.jsp                 # Landing page
            ├── css/
            │   └── style.css             # SaaS design system & responsive styling
            ├── js/
            │   └── app.js                # Form validation & confirmation scripts
            └── WEB-INF/
                ├── web.xml               # Servlet 6.0 deployment descriptor
                └── views/                # Protected JSP View Templates
                    ├── application-details.jsp
                    ├── application-form.jsp
                    ├── dashboard.jsp
                    ├── login.jsp
                    ├── register.jsp
                    └── includes/
                        ├── footer.jsp
                        └── header.jsp
```

---

## 8. License & Author

- **Author:** Aagam Jain
- **License:** Open Source under the [MIT License](https://opensource.org/licenses/MIT).
