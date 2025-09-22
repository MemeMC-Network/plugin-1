package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.Home;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages home system functionality
 */
public class HomeManager {
    
    private final SurvivalCore plugin;
    private final Map<UUID, Map<String, Home>> playerHomes = new HashMap<>();
    
    public HomeManager(SurvivalCore plugin) {
        this.plugin = plugin;
        loadAllHomes();
    }
    
    /**
     * Set a home for a player
     */
    public boolean setHome(Player player, String homeName, Location location) {
        UUID playerUuid = player.getUniqueId();
        
        // Validate home name
        if (!isValidHomeName(homeName)) {
            return false;
        }
        
        // Check if player has reached max homes
        int maxHomes = getMaxHomes(player);
        Map<String, Home> homes = playerHomes.computeIfAbsent(playerUuid, k -> new HashMap<>());
        
        if (!homes.containsKey(homeName) && homes.size() >= maxHomes) {
            return false; // Max homes reached
        }
        
        Home home = new Home(homeName, playerUuid.toString(), location);
        
        try {
            // Save to database
            if (homes.containsKey(homeName)) {
                // Update existing home
                plugin.getDatabaseManager().executeUpdate(
                    "UPDATE sc_homes SET world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE player_uuid = ? AND home_name = ?",
                    home.getWorldName(), home.getX(), home.getY(), home.getZ(), 
                    home.getYaw(), home.getPitch(), home.getPlayerUuid(), home.getName()
                );
            } else {
                // Insert new home
                plugin.getDatabaseManager().executeUpdate(
                    "INSERT INTO sc_homes (player_uuid, home_name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    home.getPlayerUuid(), home.getName(), home.getWorldName(), 
                    home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch()
                );
            }
            
            // Update cache
            homes.put(homeName, home);
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving home to database", e);
            return false;
        }
    }
    
    /**
     * Delete a home for a player
     */
    public boolean deleteHome(Player player, String homeName) {
        UUID playerUuid = player.getUniqueId();
        Map<String, Home> homes = playerHomes.get(playerUuid);
        
        if (homes == null || !homes.containsKey(homeName)) {
            return false; // Home doesn't exist
        }
        
        try {
            // Delete from database
            plugin.getDatabaseManager().executeUpdate(
                "DELETE FROM sc_homes WHERE player_uuid = ? AND home_name = ?",
                playerUuid.toString(), homeName
            );
            
            // Remove from cache
            homes.remove(homeName);
            if (homes.isEmpty()) {
                playerHomes.remove(playerUuid);
            }
            
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting home from database", e);
            return false;
        }
    }
    
    /**
     * Get a specific home for a player
     */
    public Home getHome(Player player, String homeName) {
        UUID playerUuid = player.getUniqueId();
        Map<String, Home> homes = playerHomes.get(playerUuid);
        
        if (homes == null) {
            return null;
        }
        
        return homes.get(homeName);
    }
    
    /**
     * Get all homes for a player
     */
    public Map<String, Home> getPlayerHomes(Player player) {
        UUID playerUuid = player.getUniqueId();
        return playerHomes.getOrDefault(playerUuid, new HashMap<>());
    }
    
    /**
     * Get the default home for a player (first home or "home")
     */
    public Home getDefaultHome(Player player) {
        Map<String, Home> homes = getPlayerHomes(player);
        
        if (homes.isEmpty()) {
            return null;
        }
        
        // Check for "home" first
        if (homes.containsKey("home")) {
            return homes.get("home");
        }
        
        // Return first home
        return homes.values().iterator().next();
    }
    
    /**
     * Check if a player has a specific home
     */
    public boolean hasHome(Player player, String homeName) {
        return getHome(player, homeName) != null;
    }
    
    /**
     * Get the maximum number of homes a player can have
     */
    public int getMaxHomes(Player player) {
        // Check for permission-based limits
        Map<String, Object> limits = plugin.getConfigManager().getConfig().getConfigurationSection("home-limits").getValues(false);
        
        int maxHomes = plugin.getConfigManager().getMaxHomes(); // Default from config
        
        for (Map.Entry<String, Object> entry : limits.entrySet()) {
            String permission = entry.getKey();
            int limit = (Integer) entry.getValue();
            
            if (player.hasPermission(permission)) {
                if (limit == -1) { // Unlimited
                    return Integer.MAX_VALUE;
                }
                maxHomes = Math.max(maxHomes, limit);
            }
        }
        
        return maxHomes;
    }
    
    /**
     * Load all homes from database
     */
    private void loadAllHomes() {
        try {
            ResultSet rs = plugin.getDatabaseManager().executeQuery("SELECT * FROM sc_homes");
            
            while (rs.next()) {
                String playerUuid = rs.getString("player_uuid");
                String homeName = rs.getString("home_name");
                String worldName = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");
                long createdAt = rs.getTimestamp("created_at").getTime();
                
                Home home = new Home(homeName, playerUuid, worldName, x, y, z, yaw, pitch, createdAt);
                
                UUID uuid = UUID.fromString(playerUuid);
                playerHomes.computeIfAbsent(uuid, k -> new HashMap<>()).put(homeName, home);
            }
            
            rs.close();
            plugin.getLogger().info("Loaded " + getTotalHomes() + " homes from database");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading homes from database", e);
        }
    }
    
    /**
     * Save all homes to database
     */
    public void saveAllData() {
        // Data is saved immediately when modified, so this is mainly for cleanup
        plugin.getLogger().info("Home data is automatically saved to database");
    }
    
    /**
     * Get total number of homes across all players
     */
    private int getTotalHomes() {
        return playerHomes.values().stream()
                .mapToInt(Map::size)
                .sum();
    }
    
    /**
     * Validate home name
     */
    private boolean isValidHomeName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Only allow alphanumeric characters and underscores
        return name.matches("^[a-zA-Z0-9_]+$") && name.length() <= 32;
    }
    
    /**
     * Get homes list as formatted strings
     */
    public List<String> getHomesListFormatted(Player player) {
        Map<String, Home> homes = getPlayerHomes(player);
        List<String> formatted = new ArrayList<>();
        
        for (Home home : homes.values()) {
            if (home.isValidLocation()) {
                String format = String.format("%s in %s at %d, %d, %d", 
                              home.getName(), 
                              home.getWorldName(),
                              (int) home.getX(),
                              (int) home.getY(), 
                              (int) home.getZ());
                formatted.add(format);
            }
        }
        
        return formatted;
    }
}