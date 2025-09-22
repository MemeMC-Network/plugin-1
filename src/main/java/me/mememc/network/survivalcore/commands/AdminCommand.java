package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles Admin command functionality
 */
public class AdminCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public AdminCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("survivalcore.admin")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "info":
                return handleInfo(sender);
            default:
                sendUsage(sender);
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        try {
            plugin.reloadPlugin();
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.reloaded", "Configuration reloaded successfully!"));
        } catch (Exception e) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.reload-error", "Error occurred while reloading configuration!"));
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
        }
        return true;
    }
    
    private boolean handleInfo(CommandSender sender) {
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
            .getString("admin.info-header", "--- SurvivalCore-V2 Info ---"));
        
        String version = plugin.getConfigManager().getMessagesConfig()
            .getString("admin.info-version", "Version: {version}")
            .replace("{version}", plugin.getDescription().getVersion());
        MessageUtils.sendMessage(sender, version);
        
        String author = plugin.getConfigManager().getMessagesConfig()
            .getString("admin.info-author", "Author: MemeMC Network");
        MessageUtils.sendMessage(sender, author);
        
        String database = plugin.getConfigManager().getMessagesConfig()
            .getString("admin.info-database", "Database: {database}")
            .replace("{database}", plugin.getConfigManager().getDatabaseType().toUpperCase());
        MessageUtils.sendMessage(sender, database);
        
        String players = plugin.getConfigManager().getMessagesConfig()
            .getString("admin.info-players", "Online Players: {players}")
            .replace("{players}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        MessageUtils.sendMessage(sender, players);
        
        // Feature status
        MessageUtils.sendMessage(sender, "&7Features:");
        MessageUtils.sendMessage(sender, "&7- RTP: " + (plugin.getConfigManager().isRtpEnabled() ? "&aEnabled" : "&cDisabled"));
        MessageUtils.sendMessage(sender, "&7- TPA: " + (plugin.getConfigManager().isTpaEnabled() ? "&aEnabled" : "&cDisabled"));
        MessageUtils.sendMessage(sender, "&7- Homes: " + (plugin.getConfigManager().isHomeEnabled() ? "&aEnabled" : "&cDisabled"));
        MessageUtils.sendMessage(sender, "&7- Warps: " + (plugin.getConfigManager().isWarpEnabled() ? "&aEnabled" : "&cDisabled"));
        MessageUtils.sendMessage(sender, "&7- Player Warps: " + (plugin.getConfigManager().isPlayerWarpEnabled() ? "&aEnabled" : "&cDisabled"));
        MessageUtils.sendMessage(sender, "&7- Shop: " + (plugin.getConfigManager().isShopEnabled() ? "&aEnabled" : "&cDisabled"));
        
        // Statistics
        MessageUtils.sendMessage(sender, "&7Statistics:");
        MessageUtils.sendMessage(sender, "&7- Total Warps: &e" + plugin.getWarpManager().getWarpCount());
        MessageUtils.sendMessage(sender, "&7- Total Player Warps: &e" + plugin.getPlayerWarpManager().getAllPlayerWarps().size());
        
        return true;
    }
    
    private void sendUsage(CommandSender sender) {
        MessageUtils.sendMessage(sender, "&6SurvivalCore-V2 Admin Commands:");
        MessageUtils.sendMessage(sender, "&7/survivalcore reload &f- Reload configuration");
        MessageUtils.sendMessage(sender, "&7/survivalcore info &f- Show plugin information");
    }
}