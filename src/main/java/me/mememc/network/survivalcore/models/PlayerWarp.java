package me.mememc.network.survivalcore.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a player-created warp location
 */
public class PlayerWarp {
    
    private final String name;
    private final String playerUuid;
    private final String worldName;
    private final double x, y, z;
    private final float yaw, pitch;
    private final long createdAt;
    
    public PlayerWarp(String name, String playerUuid, String worldName, double x, double y, double z, float yaw, float pitch, long createdAt) {
        this.name = name;
        this.playerUuid = playerUuid;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdAt = createdAt;
    }
    
    public PlayerWarp(String name, String playerUuid, Location location) {
        this.name = name;
        this.playerUuid = playerUuid;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.createdAt = System.currentTimeMillis();
    }
    
    public String getName() {
        return name;
    }
    
    public String getPlayerUuid() {
        return playerUuid;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public boolean isValidLocation() {
        return getLocation() != null;
    }
    
    @Override
    public String toString() {
        return String.format("PlayerWarp{name='%s', world='%s', x=%.1f, y=%.1f, z=%.1f}", 
                           name, worldName, x, y, z);
    }
}