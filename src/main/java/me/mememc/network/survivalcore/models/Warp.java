package me.mememc.network.survivalcore.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a server warp location
 */
public class Warp {
    
    private final String name;
    private final String worldName;
    private final double x, y, z;
    private final float yaw, pitch;
    private final String createdBy;
    private final long createdAt;
    
    public Warp(String name, String worldName, double x, double y, double z, float yaw, float pitch, String createdBy, long createdAt) {
        this.name = name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
    
    public Warp(String name, Location location, String createdBy) {
        this.name = name;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
    }
    
    public String getName() {
        return name;
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
    
    public String getCreatedBy() {
        return createdBy;
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
        return String.format("Warp{name='%s', world='%s', x=%.1f, y=%.1f, z=%.1f}", 
                           name, worldName, x, y, z);
    }
}