package me.mememc.network.survivalcore.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a kit with items and metadata
 */
public class Kit {
    
    private final String name;
    private final String displayName;
    private final String description;
    private final List<ItemStack> items;
    private final int cooldown;
    private final double cost;
    private final String permission;
    private final boolean oneTime;
    
    public Kit(String name, String displayName, String description, List<ItemStack> items, 
               int cooldown, double cost, String permission, boolean oneTime) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.items = items;
        this.cooldown = cooldown;
        this.cost = cost;
        this.permission = permission;
        this.oneTime = oneTime;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<ItemStack> getItems() {
        return items;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public double getCost() {
        return cost;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public boolean isOneTime() {
        return oneTime;
    }
}