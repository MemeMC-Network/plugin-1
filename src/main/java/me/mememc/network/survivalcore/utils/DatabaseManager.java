package me.mememc.network.survivalcore.utils;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

/**
 * Manages database connections and operations
 */
public class DatabaseManager {
    
    private final SurvivalCore plugin;
    private Connection connection;
    private final String databaseType;
    
    public DatabaseManager(SurvivalCore plugin) {
        this.plugin = plugin;
        this.databaseType = plugin.getConfigManager().getDatabaseType().toLowerCase();
    }
    
    public boolean initialize() {
        try {
            if (databaseType.equals("mysql")) {
                return initializeMySQL();
            } else {
                return initializeSQLite();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            return false;
        }
    }
    
    private boolean initializeMySQL() {
        try {
            String host = plugin.getConfigManager().getDatabaseHost();
            int port = plugin.getConfigManager().getDatabasePort();
            String database = plugin.getConfigManager().getDatabaseName();
            String username = plugin.getConfigManager().getDatabaseUsername();
            String password = plugin.getConfigManager().getDatabasePassword();
            
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", 
                                     host, port, database);
            
            connection = DriverManager.getConnection(url, username, password);
            
            plugin.getLogger().info("Successfully connected to MySQL database!");
            createTables();
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database", e);
            return false;
        }
    }
    
    private boolean initializeSQLite() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            File databaseFile = new File(dataFolder, "survivalcore.db");
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            
            connection = DriverManager.getConnection(url);
            
            plugin.getLogger().info("Successfully connected to SQLite database!");
            createTables();
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database", e);
            return false;
        }
    }
    
    private void createTables() throws SQLException {
        // Homes table
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS sc_homes (" +
            "id INTEGER PRIMARY KEY " + (databaseType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
            "player_uuid VARCHAR(36) NOT NULL," +
            "home_name VARCHAR(32) NOT NULL," +
            "world VARCHAR(64) NOT NULL," +
            "x DOUBLE NOT NULL," +
            "y DOUBLE NOT NULL," +
            "z DOUBLE NOT NULL," +
            "yaw FLOAT NOT NULL," +
            "pitch FLOAT NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "UNIQUE(player_uuid, home_name)" +
            ")"
        );
        
        // Warps table
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS sc_warps (" +
            "id INTEGER PRIMARY KEY " + (databaseType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
            "warp_name VARCHAR(32) NOT NULL UNIQUE," +
            "world VARCHAR(64) NOT NULL," +
            "x DOUBLE NOT NULL," +
            "y DOUBLE NOT NULL," +
            "z DOUBLE NOT NULL," +
            "yaw FLOAT NOT NULL," +
            "pitch FLOAT NOT NULL," +
            "created_by VARCHAR(36) NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        // Player warps table
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS sc_player_warps (" +
            "id INTEGER PRIMARY KEY " + (databaseType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
            "player_uuid VARCHAR(36) NOT NULL," +
            "warp_name VARCHAR(32) NOT NULL," +
            "world VARCHAR(64) NOT NULL," +
            "x DOUBLE NOT NULL," +
            "y DOUBLE NOT NULL," +
            "z DOUBLE NOT NULL," +
            "yaw FLOAT NOT NULL," +
            "pitch FLOAT NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "UNIQUE(player_uuid, warp_name)" +
            ")"
        );
        
        // Player data table
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS sc_player_data (" +
            "player_uuid VARCHAR(36) PRIMARY KEY," +
            "player_name VARCHAR(16) NOT NULL," +
            "last_rtp BIGINT DEFAULT 0," +
            "last_tpa BIGINT DEFAULT 0," +
            "last_home BIGINT DEFAULT 0," +
            "last_warp BIGINT DEFAULT 0," +
            "last_pwarp BIGINT DEFAULT 0," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        plugin.getLogger().info("Database tables created successfully!");
    }
    
    public void executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        }
    }
    
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement.executeQuery();
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed successfully!");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
        }
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initialize();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking database connection", e);
        }
        return connection;
    }
    
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}