package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.Warp;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages warp system functionality
 */
public class WarpManager {
    
    private final SurvivalCore plugin;
    private final Map<String, Warp> warps = new HashMap<>();
    
    public WarpManager(SurvivalCore plugin) {
        this.plugin = plugin;
        loadAllWarps();
    }
    
    /**
     * Create a new warp
     */
    public boolean createWarp(String warpName, Location location, String createdBy) {
        // Validate warp name
        if (!isValidWarpName(warpName)) {
            return false;
        }
        
        // Check if warp already exists
        if (warps.containsKey(warpName.toLowerCase())) {
            return false; // Warp already exists
        }
        
        Warp warp = new Warp(warpName, location, createdBy);
        
        try {
            // Save to database
            plugin.getDatabaseManager().executeUpdate(
                "INSERT INTO sc_warps (warp_name, world, x, y, z, yaw, pitch, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                warp.getName(), warp.getWorldName(), warp.getX(), warp.getY(), warp.getZ(), 
                warp.getYaw(), warp.getPitch(), warp.getCreatedBy()
            );
            
            // Update cache
            warps.put(warpName.toLowerCase(), warp);
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving warp to database", e);
            return false;
        }
    }
    
    /**
     * Delete a warp
     */
    public boolean deleteWarp(String warpName) {
        String key = warpName.toLowerCase();
        
        if (!warps.containsKey(key)) {
            return false; // Warp doesn't exist
        }
        
        try {
            // Delete from database
            plugin.getDatabaseManager().executeUpdate(
                "DELETE FROM sc_warps WHERE warp_name = ?",
                warpName
            );
            
            // Remove from cache
            warps.remove(key);
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting warp from database", e);
            return false;
        }
    }
    
    /**
     * Get a specific warp
     */
    public Warp getWarp(String warpName) {
        return warps.get(warpName.toLowerCase());
    }
    
    /**
     * Get all warps
     */
    public Collection<Warp> getAllWarps() {
        return warps.values();
    }
    
    /**
     * Get all warp names
     */
    public Set<String> getWarpNames() {
        return new HashSet<>(warps.keySet());
    }
    
    /**
     * Check if a warp exists
     */
    public boolean warpExists(String warpName) {
        return warps.containsKey(warpName.toLowerCase());
    }
    
    /**
     * Get the number of warps
     */
    public int getWarpCount() {
        return warps.size();
    }
    
    /**
     * Load all warps from database
     */
    private void loadAllWarps() {
        try {
            ResultSet rs = plugin.getDatabaseManager().executeQuery("SELECT * FROM sc_warps");
            
            while (rs.next()) {
                String warpName = rs.getString("warp_name");
                String worldName = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");
                String createdBy = rs.getString("created_by");
                long createdAt = rs.getTimestamp("created_at").getTime();
                
                Warp warp = new Warp(warpName, worldName, x, y, z, yaw, pitch, createdBy, createdAt);
                warps.put(warpName.toLowerCase(), warp);
            }
            
            rs.close();
            plugin.getLogger().info("Loaded " + warps.size() + " warps from database");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading warps from database", e);
        }
    }
    
    /**
     * Save all warps to database
     */
    public void saveAllData() {
        // Data is saved immediately when modified, so this is mainly for cleanup
        plugin.getLogger().info("Warp data is automatically saved to database");
    }
    
    /**
     * Validate warp name
     */
    private boolean isValidWarpName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Only allow alphanumeric characters and underscores
        return name.matches("^[a-zA-Z0-9_]+$") && name.length() <= 32;
    }
    
    /**
     * Get warps list as formatted strings
     */
    public List<String> getWarpsListFormatted() {
        List<String> formatted = new ArrayList<>();
        
        for (Warp warp : warps.values()) {
            if (warp.isValidLocation()) {
                String format = String.format("%s in %s at %d, %d, %d", 
                              warp.getName(), 
                              warp.getWorldName(),
                              (int) warp.getX(),
                              (int) warp.getY(), 
                              (int) warp.getZ());
                formatted.add(format);
            }
        }
        
        return formatted;
    }
    
    /**
     * Get warps created by a specific player
     */
    public List<Warp> getWarpsByCreator(String playerUuid) {
        List<Warp> playerWarps = new ArrayList<>();
        
        for (Warp warp : warps.values()) {
            if (warp.getCreatedBy().equals(playerUuid)) {
                playerWarps.add(warp);
            }
        }
        
        return playerWarps;
    }
}