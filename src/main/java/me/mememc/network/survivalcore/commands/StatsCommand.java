package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles Stats and Leaderboard command functionality
 */
public class StatsCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public StatsCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.player-only", "This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getConfigManager().isStatsEnabled()) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.feature-disabled", "This feature is currently disabled!"));
            return true;
        }
        
        String commandName = command.getName().toLowerCase();
        
        if (commandName.equals("stats")) {
            return handleStatsCommand(player, args);
        } else if (commandName.equals("leaderboard") || commandName.equals("lb") || commandName.equals("top")) {
            return handleLeaderboardCommand(player, args);
        }
        
        return true;
    }
    
    private boolean handleStatsCommand(Player player, String[] args) {
        if (args.length == 0) {
            // Show player's own stats
            if (!player.hasPermission("survivalcore.stats.view")) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.no-permission", "You don't have permission to use this command!"));
                return true;
            }
            
            plugin.getStatsManager().showPlayerStats(player, player);
            return true;
        }
        
        // Show another player's stats
        if (!player.hasPermission("survivalcore.stats.others")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to view other players' stats!"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                .replace("{player}", targetName));
            return true;
        }
        
        plugin.getStatsManager().showPlayerStats(player, target);
        return true;
    }
    
    private boolean handleLeaderboardCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.stats.leaderboard")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        String category = "playtime"; // Default category
        
        if (args.length > 0) {
            String input = args[0].toLowerCase();
            if (isValidCategory(input)) {
                category = input;
            } else {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("stats.invalid-category", "{prefix}&cInvalid category! Available: playtime, blocks, broken, kills, pvp, deaths, travel, crafted, fish"));
                return true;
            }
        }
        
        plugin.getStatsManager().showLeaderboard(player, category);
        return true;
    }
    
    private boolean isValidCategory(String category) {
        return category.equals("playtime") || 
               category.equals("blocks") || 
               category.equals("broken") || 
               category.equals("kills") || 
               category.equals("pvp") || 
               category.equals("deaths") || 
               category.equals("travel") || 
               category.equals("crafted") || 
               category.equals("fish");
    }
}