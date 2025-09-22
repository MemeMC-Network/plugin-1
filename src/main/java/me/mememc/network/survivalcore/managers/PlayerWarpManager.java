package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.PlayerWarp;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages player warp system functionality
 */
public class PlayerWarpManager {
    
    private final SurvivalCore plugin;
    private final Map<String, PlayerWarp> playerWarps = new HashMap<>();
    private final Map<UUID, Set<String>> playerOwnedWarps = new HashMap<>();
    
    public PlayerWarpManager(SurvivalCore plugin) {
        this.plugin = plugin;
        loadAllPlayerWarps();
    }
    
    /**
     * Create a new player warp
     */
    public boolean createPlayerWarp(Player player, String warpName, Location location) {
        UUID playerUuid = player.getUniqueId();
        
        // Validate warp name
        if (!isValidWarpName(warpName)) {
            return false;
        }
        
        // Check if warp already exists
        if (playerWarps.containsKey(warpName.toLowerCase())) {
            return false; // Warp already exists
        }
        
        // Check if player has reached max warps
        int maxWarps = getMaxPlayerWarps(player);
        Set<String> ownedWarps = playerOwnedWarps.computeIfAbsent(playerUuid, k -> new HashSet<>());
        
        if (ownedWarps.size() >= maxWarps) {
            return false; // Max warps reached
        }
        
        PlayerWarp playerWarp = new PlayerWarp(warpName, playerUuid.toString(), location);
        
        try {
            // Save to database
            plugin.getDatabaseManager().executeUpdate(
                "INSERT INTO sc_player_warps (player_uuid, warp_name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                playerWarp.getPlayerUuid(), playerWarp.getName(), playerWarp.getWorldName(), 
                playerWarp.getX(), playerWarp.getY(), playerWarp.getZ(), 
                playerWarp.getYaw(), playerWarp.getPitch()
            );
            
            // Update cache
            playerWarps.put(warpName.toLowerCase(), playerWarp);
            ownedWarps.add(warpName.toLowerCase());
            
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player warp to database", e);
            return false;
        }
    }
    
    /**
     * Delete a player warp
     */
    public boolean deletePlayerWarp(Player player, String warpName) {
        UUID playerUuid = player.getUniqueId();
        String key = warpName.toLowerCase();
        
        PlayerWarp playerWarp = playerWarps.get(key);
        if (playerWarp == null) {
            return false; // Warp doesn't exist
        }
        
        // Check if player owns the warp
        if (!playerWarp.getPlayerUuid().equals(playerUuid.toString())) {
            return false; // Player doesn't own this warp
        }
        
        try {
            // Delete from database
            plugin.getDatabaseManager().executeUpdate(
                "DELETE FROM sc_player_warps WHERE player_uuid = ? AND warp_name = ?",
                playerUuid.toString(), warpName
            );
            
            // Remove from cache
            playerWarps.remove(key);
            Set<String> ownedWarps = playerOwnedWarps.get(playerUuid);
            if (ownedWarps != null) {
                ownedWarps.remove(key);
                if (ownedWarps.isEmpty()) {
                    playerOwnedWarps.remove(playerUuid);
                }
            }
            
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting player warp from database", e);
            return false;
        }
    }
    
    /**
     * Get a specific player warp
     */
    public PlayerWarp getPlayerWarp(String warpName) {
        return playerWarps.get(warpName.toLowerCase());
    }
    
    /**
     * Get all player warps
     */
    public Collection<PlayerWarp> getAllPlayerWarps() {
        return playerWarps.values();
    }
    
    /**
     * Get player warps owned by a specific player
     */
    public List<PlayerWarp> getPlayerWarpsOwnedBy(Player player) {
        UUID playerUuid = player.getUniqueId();
        List<PlayerWarp> ownedWarps = new ArrayList<>();
        
        for (PlayerWarp warp : playerWarps.values()) {
            if (warp.getPlayerUuid().equals(playerUuid.toString())) {
                ownedWarps.add(warp);
            }
        }
        
        return ownedWarps;
    }
    
    /**
     * Check if a player warp exists
     */
    public boolean playerWarpExists(String warpName) {
        return playerWarps.containsKey(warpName.toLowerCase());
    }
    
    /**
     * Check if a player owns a specific warp
     */
    public boolean playerOwnsWarp(Player player, String warpName) {
        PlayerWarp warp = getPlayerWarp(warpName);
        return warp != null && warp.getPlayerUuid().equals(player.getUniqueId().toString());
    }
    
    /**
     * Get the maximum number of player warps a player can have
     */
    public int getMaxPlayerWarps(Player player) {
        // Check for permission-based limits
        Map<String, Object> limits = plugin.getConfigManager().getConfig().getConfigurationSection("pwarp-limits").getValues(false);
        
        int maxWarps = plugin.getConfigManager().getMaxPlayerWarps(); // Default from config
        
        for (Map.Entry<String, Object> entry : limits.entrySet()) {
            String permission = entry.getKey();
            int limit = (Integer) entry.getValue();
            
            if (player.hasPermission(permission)) {
                if (limit == -1) { // Unlimited
                    return Integer.MAX_VALUE;
                }
                maxWarps = Math.max(maxWarps, limit);
            }
        }
        
        return maxWarps;
    }
    
    /**
     * Get the number of player warps owned by a player
     */
    public int getPlayerWarpCount(Player player) {
        Set<String> ownedWarps = playerOwnedWarps.get(player.getUniqueId());
        return ownedWarps != null ? ownedWarps.size() : 0;
    }
    
    /**
     * Load all player warps from database
     */
    private void loadAllPlayerWarps() {
        try {
            ResultSet rs = plugin.getDatabaseManager().executeQuery("SELECT * FROM sc_player_warps");
            
            while (rs.next()) {
                String playerUuid = rs.getString("player_uuid");
                String warpName = rs.getString("warp_name");
                String worldName = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");
                long createdAt = rs.getTimestamp("created_at").getTime();
                
                PlayerWarp playerWarp = new PlayerWarp(warpName, playerUuid, worldName, x, y, z, yaw, pitch, createdAt);
                
                // Add to main map
                playerWarps.put(warpName.toLowerCase(), playerWarp);
                
                // Add to player owned warps map
                UUID uuid = UUID.fromString(playerUuid);
                playerOwnedWarps.computeIfAbsent(uuid, k -> new HashSet<>()).add(warpName.toLowerCase());
            }
            
            rs.close();
            plugin.getLogger().info("Loaded " + playerWarps.size() + " player warps from database");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading player warps from database", e);
        }
    }
    
    /**
     * Save all player warps to database
     */
    public void saveAllData() {
        // Data is saved immediately when modified, so this is mainly for cleanup
        plugin.getLogger().info("Player warp data is automatically saved to database");
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
     * Get player warps list as formatted strings for all warps
     */
    public List<String> getPlayerWarpsListFormatted() {
        List<String> formatted = new ArrayList<>();
        
        for (PlayerWarp warp : playerWarps.values()) {
            if (warp.isValidLocation()) {
                String ownerName = plugin.getServer().getOfflinePlayer(UUID.fromString(warp.getPlayerUuid())).getName();
                String format = String.format("%s by %s in %s", 
                              warp.getName(), 
                              ownerName != null ? ownerName : "Unknown",
                              warp.getWorldName());
                formatted.add(format);
            }
        }
        
        return formatted;
    }
    
    /**
     * Get player warps list as formatted strings for owned warps
     */
    public List<String> getOwnedPlayerWarpsListFormatted(Player player) {
        List<String> formatted = new ArrayList<>();
        
        for (PlayerWarp warp : getPlayerWarpsOwnedBy(player)) {
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
}