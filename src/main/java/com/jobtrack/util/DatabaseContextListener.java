package com.jobtrack.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * ServletContextListener that initializes the HikariCP connection pool on web application startup
 * and gracefully releases resources upon shutdown.
 * Matches registration in WEB-INF/web.xml: com.jobtrack.util.DatabaseContextListener
 */
@WebListener
public class DatabaseContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseContextListener.class);
    private static final String DB_PROPERTIES_FILE = "db.properties";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        LOGGER.info("Starting up JobTrack Application Lifecycle Listener...");

        try {
            // Load db.properties from webapp classpath
            Properties props = new Properties();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = DatabaseContextListener.class.getClassLoader();
            }

            try (InputStream is = cl.getResourceAsStream(DB_PROPERTIES_FILE)) {
                if (is == null) {
                    throw new IllegalStateException("CRITICAL: '" + DB_PROPERTIES_FILE + "' not found in application classpath.");
                }
                props.load(is);
            }

            // Extract and validate connection properties
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

            if (url == null || url.trim().isEmpty()) {
                throw new IllegalStateException("CRITICAL: 'db.url' property is required in " + DB_PROPERTIES_FILE);
            }

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url.trim());
            config.setUsername(username != null ? username.trim() : "");
            config.setPassword(password != null ? password.trim() : "");
            config.setDriverClassName(driver.trim());

            // Pool sizing and lifecycle configuration
            config.setPoolName(props.getProperty("hikari.poolName", "JobTrackHikariPool").trim());
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "10").trim()));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimumIdle", "2").trim()));
            config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idleTimeout", "30000").trim()));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout", "20000").trim()));
            config.setMaxLifetime(Long.parseLong(props.getProperty("hikari.maxLifetime", "1800000").trim()));

            // Performance optimizations for MySQL
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

            HikariDataSource ds = new HikariDataSource(config);

            // Register DataSource in DBConnection singleton and ServletContext attributes
            DBConnection.init(ds);
            context.setAttribute("dataSource", ds);

            LOGGER.info("HikariCP connection pool successfully initialized and bound to ServletContext [Pool: {}].", 
                    ds.getPoolName());

        } catch (Exception e) {
            LOGGER.error("Fatal exception during database connection pool startup", e);
            throw new RuntimeException("Application startup aborted due to database initialization failure.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Shutting down JobTrack Application Lifecycle Listener...");

        // 1. Close HikariCP connection pool cleanly
        try {
            DBConnection.shutdown();
        } catch (Exception e) {
            LOGGER.warn("Exception occurred while shutting down DBConnection pool", e);
        }

        // 2. Deregister JDBC drivers registered by this webapp ClassLoader to prevent Tomcat memory leaks
        deregisterJdbcDrivers();

        LOGGER.info("JobTrack database cleanup completed successfully.");
    }

    private void deregisterJdbcDrivers() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    DriverManager.deregisterDriver(driver);
                    LOGGER.info("Deregistered JDBC driver: {}", driver);
                } catch (SQLException e) {
                    LOGGER.warn("Error deregistering JDBC driver: {}", driver, e);
                }
            }
        }
    }
}
