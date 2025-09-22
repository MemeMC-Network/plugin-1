package me.mememc.network.survivalcore;

import me.mememc.network.survivalcore.commands.*;
import me.mememc.network.survivalcore.listeners.PlayerListener;
import me.mememc.network.survivalcore.managers.*;
import me.mememc.network.survivalcore.utils.ConfigManager;
import me.mememc.network.survivalcore.utils.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * SurvivalCore-V2 Main Plugin Class
 * Created by MemeMC Network
 * 
 * A comprehensive survival server plugin featuring:
 * - Random Teleportation (RTP)
 * - Teleport Accept System (TPA)
 * - Home Management System
 * - Warp System (Admin & Player)
 * - Multi-page Shop System
 * - Full Permission Integration
 * - Extensive Configuration Options
 */
public class SurvivalCore extends JavaPlugin {
    
    private static SurvivalCore instance;
    
    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CooldownManager cooldownManager;
    private TpaManager tpaManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private PlayerWarpManager playerWarpManager;
    private ShopManager shopManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        saveDefaultConfig();
        
        try {
            // Initialize managers
            this.configManager = new ConfigManager(this);
            this.databaseManager = new DatabaseManager(this);
            this.cooldownManager = new CooldownManager();
            this.tpaManager = new TpaManager(this);
            this.homeManager = new HomeManager(this);
            this.warpManager = new WarpManager(this);
            this.playerWarpManager = new PlayerWarpManager(this);
            this.shopManager = new ShopManager(this);
            
            // Initialize database
            if (!databaseManager.initialize()) {
                getLogger().severe("Failed to initialize database! Disabling plugin...");
                getPluginLoader().disablePlugin(this);
                return;
            }
            
            // Register commands
            registerCommands();
            
            // Register listeners
            registerListeners();
            
            getLogger().info("SurvivalCore-V2 has been successfully enabled!");
            getLogger().info("Version: " + getDescription().getVersion());
            getLogger().info("Author: MemeMC Network");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred while enabling SurvivalCore-V2", e);
            getPluginLoader().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            // Save all data before shutdown
            if (homeManager != null) {
                homeManager.saveAllData();
            }
            if (warpManager != null) {
                warpManager.saveAllData();
            }
            if (playerWarpManager != null) {
                playerWarpManager.saveAllData();
            }
            if (databaseManager != null) {
                databaseManager.closeConnection();
            }
            
            getLogger().info("SurvivalCore-V2 has been successfully disabled!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred while disabling SurvivalCore-V2", e);
        }
    }
    
    private void registerCommands() {
        // RTP Commands
        getCommand("rtp").setExecutor(new RtpCommand(this));
        
        // TPA Commands
        TpaCommand tpaCommand = new TpaCommand(this);
        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpaccept").setExecutor(tpaCommand);
        getCommand("tpdeny").setExecutor(tpaCommand);
        getCommand("tpahere").setExecutor(tpaCommand);
        
        // Home Commands
        HomeCommand homeCommand = new HomeCommand(this);
        getCommand("home").setExecutor(homeCommand);
        getCommand("sethome").setExecutor(homeCommand);
        getCommand("delhome").setExecutor(homeCommand);
        getCommand("homes").setExecutor(homeCommand);
        
        // Warp Commands
        WarpCommand warpCommand = new WarpCommand(this);
        getCommand("warp").setExecutor(warpCommand);
        getCommand("warps").setExecutor(warpCommand);
        getCommand("setwarp").setExecutor(warpCommand);
        getCommand("delwarp").setExecutor(warpCommand);
        
        // Player Warp Commands
        PlayerWarpCommand pwarpCommand = new PlayerWarpCommand(this);
        getCommand("pwarp").setExecutor(pwarpCommand);
        getCommand("pwarps").setExecutor(pwarpCommand);
        getCommand("setpwarp").setExecutor(pwarpCommand);
        getCommand("delpwarp").setExecutor(pwarpCommand);
        
        // Shop Commands
        getCommand("shop").setExecutor(new ShopCommand(this));
        
        // Admin Commands
        getCommand("survivalcore").setExecutor(new AdminCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    public void reloadPlugin() {
        try {
            reloadConfig();
            configManager.reloadConfigs();
            getLogger().info("SurvivalCore-V2 configuration reloaded successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error reloading SurvivalCore-V2 configuration", e);
        }
    }
    
    // Getters for managers
    public static SurvivalCore getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public TpaManager getTpaManager() {
        return tpaManager;
    }
    
    public HomeManager getHomeManager() {
        return homeManager;
    }
    
    public WarpManager getWarpManager() {
        return warpManager;
    }
    
    public PlayerWarpManager getPlayerWarpManager() {
        return playerWarpManager;
    }
    
    public ShopManager getShopManager() {
        return shopManager;
    }
}