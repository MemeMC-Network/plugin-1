package me.mememc.network.survivalcore.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages cooldowns for various plugin features
 */
public class CooldownManager {
    
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    
    /**
     * Set a cooldown for a player and feature
     */
    public void setCooldown(Player player, String feature, int seconds) {
        setCooldown(player.getUniqueId(), feature, seconds);
    }
    
    /**
     * Set a cooldown for a player UUID and feature
     */
    public void setCooldown(UUID playerUuid, String feature, int seconds) {
        cooldowns.computeIfAbsent(playerUuid, k -> new HashMap<>())
                .put(feature, System.currentTimeMillis() + (seconds * 1000L));
    }
    
    /**
     * Check if a player has a cooldown for a feature
     */
    public boolean hasCooldown(Player player, String feature) {
        return hasCooldown(player.getUniqueId(), feature);
    }
    
    /**
     * Check if a player UUID has a cooldown for a feature
     */
    public boolean hasCooldown(UUID playerUuid, String feature) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns == null) {
            return false;
        }
        
        Long cooldownEnd = playerCooldowns.get(feature);
        if (cooldownEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= cooldownEnd) {
            playerCooldowns.remove(feature);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(playerUuid);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Get remaining cooldown time in seconds
     */
    public int getRemainingCooldown(Player player, String feature) {
        return getRemainingCooldown(player.getUniqueId(), feature);
    }
    
    /**
     * Get remaining cooldown time in seconds for a UUID
     */
    public int getRemainingCooldown(UUID playerUuid, String feature) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns == null) {
            return 0;
        }
        
        Long cooldownEnd = playerCooldowns.get(feature);
        if (cooldownEnd == null) {
            return 0;
        }
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }
    
    /**
     * Remove a cooldown for a player and feature
     */
    public void removeCooldown(Player player, String feature) {
        removeCooldown(player.getUniqueId(), feature);
    }
    
    /**
     * Remove a cooldown for a player UUID and feature
     */
    public void removeCooldown(UUID playerUuid, String feature) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(feature);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(playerUuid);
            }
        }
    }
    
    /**
     * Clear all cooldowns for a player
     */
    public void clearCooldowns(Player player) {
        clearCooldowns(player.getUniqueId());
    }
    
    /**
     * Clear all cooldowns for a player UUID
     */
    public void clearCooldowns(UUID playerUuid) {
        cooldowns.remove(playerUuid);
    }
    
    /**
     * Format remaining time as a readable string
     */
    public String formatTime(int seconds) {
        if (seconds <= 0) {
            return "0s";
        }
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("s");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Cleanup expired cooldowns
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        
        cooldowns.entrySet().removeIf(playerEntry -> {
            Map<String, Long> playerCooldowns = playerEntry.getValue();
            playerCooldowns.entrySet().removeIf(cooldownEntry -> 
                currentTime >= cooldownEntry.getValue());
            return playerCooldowns.isEmpty();
        });
    }
}