package com.jobtrack.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe singleton providing database connection pooling backed by HikariCP.
 * Provides static access to database connections throughout the application DAOs.
 */
public final class DBConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnection.class);
    private static final String DB_PROPERTIES_FILE = "db.properties";

    private static volatile HikariDataSource dataSource;

    private DBConnection() {
        // Prevent direct instantiation
    }

    /**
     * Initializes the DBConnection with an existing HikariDataSource.
     * Called by DatabaseContextListener during servlet context startup.
     *
     * @param ds the initialized HikariDataSource
     */
    public static synchronized void init(HikariDataSource ds) {
        if (ds == null) {
            throw new IllegalArgumentException("HikariDataSource instance cannot be null.");
        }
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.warn("HikariDataSource is already initialized. Replacing existing pool.");
            dataSource.close();
        }
        dataSource = ds;
        LOGGER.info("DBConnection successfully initialized with provided DataSource (Pool: {}).", ds.getPoolName());
    }

    /**
     * Self-initializes HikariCP from the classpath 'db.properties' file.
     * Automatically invoked as fallback if getConnection() is called outside a servlet container
     * (e.g. during integration tests or command-line execution).
     */
    public static synchronized void init() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        LOGGER.info("Initializing HikariDataSource from classpath resource '{}'...", DB_PROPERTIES_FILE);
        Properties props = new Properties();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = DBConnection.class.getClassLoader();
        }

        try (InputStream is = cl.getResourceAsStream(DB_PROPERTIES_FILE)) {
            if (is == null) {
                String errorMsg = "Database properties file not found in classpath: " + DB_PROPERTIES_FILE;
                LOGGER.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            props.load(is);
        } catch (IOException e) {
            LOGGER.error("Failed to read database properties file: {}", DB_PROPERTIES_FILE, e);
            throw new IllegalStateException("Failed to load " + DB_PROPERTIES_FILE, e);
        }

        HikariConfig config = new HikariConfig();

        // Database connection settings
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("Missing required configuration 'db.url' in " + DB_PROPERTIES_FILE);
        }

        config.setJdbcUrl(url.trim());
        config.setUsername(username != null ? username.trim() : "");
        config.setPassword(password != null ? password.trim() : "");
        config.setDriverClassName(driver.trim());

        // HikariCP pool parameters with robust defaults
        config.setPoolName(props.getProperty("hikari.poolName", "JobTrackHikariPool").trim());
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "10").trim()));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimumIdle", "2").trim()));
        config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idleTimeout", "30000").trim()));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout", "20000").trim()));
        config.setMaxLifetime(Long.parseLong(props.getProperty("hikari.maxLifetime", "1800000").trim()));

        // MySQL 8.0 high-performance connection pool optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(config);
        LOGGER.info("HikariCP connection pool initialized successfully: {}", config.getPoolName());
    }

    /**
     * Obtains an active database connection from the connection pool.
     *
     * @return an open {@link Connection}
     * @throws SQLException if connection cannot be obtained or pool is exhausted
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            synchronized (DBConnection.class) {
                if (dataSource == null || dataSource.isClosed()) {
                    init();
                }
            }
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.error("Failed to retrieve connection from HikariCP pool [{}]: {}", 
                    (dataSource != null ? dataSource.getPoolName() : "unknown"), e.getMessage());
            throw e;
        }
    }

    /**
     * Returns the active {@link DataSource} instance.
     *
     * @return current DataSource or null if not yet initialized
     */
    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Checks if the pool is initialized and accepting connections.
     *
     * @return true if initialized and open, false otherwise
     */
    public static boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Closes the connection pool and releases all resources.
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("Shutting down HikariCP connection pool: {}", dataSource.getPoolName());
            dataSource.close();
            dataSource = null;
            LOGGER.info("HikariCP connection pool shutdown complete.");
        }
    }
}
