package me.mememc.network.survivalcore.utils;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages all configuration files for the plugin
 */
public class ConfigManager {
    
    private final SurvivalCore plugin;
    private FileConfiguration config;
    private FileConfiguration shopConfig;
    private FileConfiguration messagesConfig;
    
    private File shopConfigFile;
    private File messagesConfigFile;
    
    public ConfigManager(SurvivalCore plugin) {
        this.plugin = plugin;
        createConfigs();
    }
    
    private void createConfigs() {
        // Main config is handled by the plugin automatically
        this.config = plugin.getConfig();
        
        // Shop config
        shopConfigFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopConfigFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
        
        // Messages config
        messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesConfigFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        this.shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
    }
    
    public void saveShopConfig() {
        try {
            shopConfig.save(shopConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save shop.yml", e);
        }
    }
    
    public void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save messages.yml", e);
        }
    }
    
    // Getters
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getShopConfig() {
        return shopConfig;
    }
    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    // Configuration value getters with defaults
    public boolean isRtpEnabled() {
        return config.getBoolean("rtp.enabled", true);
    }
    
    public int getRtpCooldown() {
        return config.getInt("rtp.cooldown", 300);
    }
    
    public int getRtpMaxDistance() {
        return config.getInt("rtp.max-distance", 10000);
    }
    
    public int getRtpMinDistance() {
        return config.getInt("rtp.min-distance", 100);
    }
    
    public boolean isTpaEnabled() {
        return config.getBoolean("tpa.enabled", true);
    }
    
    public int getTpaCooldown() {
        return config.getInt("tpa.cooldown", 10);
    }
    
    public int getTpaTimeout() {
        return config.getInt("tpa.timeout", 60);
    }
    
    public boolean isHomeEnabled() {
        return config.getBoolean("homes.enabled", true);
    }
    
    public int getMaxHomes() {
        return config.getInt("homes.max-homes", 3);
    }
    
    public int getHomeCooldown() {
        return config.getInt("homes.cooldown", 10);
    }
    
    public boolean isWarpEnabled() {
        return config.getBoolean("warps.enabled", true);
    }
    
    public int getWarpCooldown() {
        return config.getInt("warps.cooldown", 10);
    }
    
    public boolean isPlayerWarpEnabled() {
        return config.getBoolean("player-warps.enabled", true);
    }
    
    public int getMaxPlayerWarps() {
        return config.getInt("player-warps.max-warps", 2);
    }
    
    public int getPlayerWarpCooldown() {
        return config.getInt("player-warps.cooldown", 10);
    }
    
    public boolean isShopEnabled() {
        return config.getBoolean("shop.enabled", true);
    }
    
    public boolean isKitEnabled() {
        return config.getBoolean("kits.enabled", true);
    }
    
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }
    
    public String getDatabaseHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    public int getDatabasePort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    public String getDatabaseName() {
        return config.getString("database.mysql.database", "survivalcore");
    }
    
    public String getDatabaseUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    public String getDatabasePassword() {
        return config.getString("database.mysql.password", "");
    }
}