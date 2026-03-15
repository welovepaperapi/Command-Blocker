package net.lyndara.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private HikariDataSource dataSource;


    public void connect(String host, int port, String db, String user, String pass) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + db);
        config.setDriverClassName("net.lyndara.libs.mariadb.Driver");
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(5000);

        this.dataSource = new HikariDataSource(config);
        logger.info("Connected to database {} at {}:{}", db, host, port);

        setupTable();
    }


    private void setupTable() {
        String sql = "CREATE TABLE IF NOT EXISTS command_filters (pattern VARCHAR(255) PRIMARY KEY)";
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
            logger.info("Ensured table 'command_filters' exists.");
        } catch (SQLException e) {
            logger.error("Failed to create command_filters table.", e);
        }
    }


    public List<String> getFilters() {
        List<String> filters = new ArrayList<>();
        String sql = "SELECT pattern FROM command_filters";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filters.add(rs.getString("pattern"));
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch command filters.", e);
        }
        return filters;
    }


    public void addFilter(String pattern) {
        String sql = "INSERT IGNORE INTO command_filters (pattern) VALUES (?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("Added new filter pattern: {}", pattern);
            } else {
                logger.info("Filter pattern already exists: {}", pattern);
            }
        } catch (SQLException e) {
            logger.error("Failed to add filter '{}'", pattern, e);
        }
    }

    public void removeFilter(String pattern) {
        String sql = "DELETE FROM command_filters WHERE pattern = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("Removed filter pattern: {}", pattern);
            } else {
                logger.warn("Filter pattern not found: {}", pattern);
            }
        } catch (SQLException e) {
            logger.error("Failed to remove filter '{}'", pattern, e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }
}