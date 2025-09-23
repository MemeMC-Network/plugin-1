package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles Enhanced Admin command functionality
 */
public class EnhancedAdminCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public EnhancedAdminCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "adminpanel":
                return handleAdminPanel(sender, args);
            case "god":
            case "godmode":
                return handleGodMode(sender, args);
            case "vanish":
            case "v":
                return handleVanish(sender, args);
            case "heal":
                return handleHeal(sender, args);
            case "feed":
                return handleFeed(sender, args);
            case "gamemode":
            case "gm":
                return handleGameMode(sender, args);
            case "teleport":
            case "tp":
                return handleTeleport(sender, args);
            case "backup":
                return handleBackup(sender, args);
            default:
                return false;
        }
    }
    
    private boolean handleAdminPanel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.player-only", "This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("survivalcore.admin.panel")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        plugin.getAdminToolsManager().openAdminPanel(player);
        return true;
    }
    
    private boolean handleGodMode(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.player-only", "This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("survivalcore.admin.god")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        Player target = player;
        
        if (args.length > 0 && player.hasPermission("survivalcore.admin.god.others")) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                    .replace("{player}", args[0]));
                return true;
            }
        }
        
        plugin.getAdminToolsManager().toggleGodMode(target);
        return true;
    }
    
    private boolean handleVanish(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.player-only", "This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("survivalcore.admin.vanish")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        plugin.getAdminToolsManager().toggleVanish(player);
        return true;
    }
    
    private boolean handleHeal(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.admin.heal")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        Player target = null;
        
        if (args.length == 0) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-usage", "Usage: /heal <player>"));
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                    .replace("{player}", args[0]));
                return true;
            }
        }
        
        plugin.getAdminToolsManager().healPlayer(target, sender instanceof Player ? (Player) sender : null);
        return true;
    }
    
    private boolean handleFeed(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.admin.feed")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        Player target = null;
        
        if (args.length == 0) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-usage", "Usage: /feed <player>"));
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                    .replace("{player}", args[0]));
                return true;
            }
        }
        
        plugin.getAdminToolsManager().feedPlayer(target, sender instanceof Player ? (Player) sender : null);
        return true;
    }
    
    private boolean handleGameMode(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.admin.gamemode")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /gamemode <mode> [player]"));
            return true;
        }
        
        GameMode gameMode = parseGameMode(args[0]);
        if (gameMode == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.invalid-gamemode", "{prefix}&cInvalid gamemode! Use: survival, creative, adventure, spectator"));
            return true;
        }
        
        Player target = null;
        
        if (args.length == 1) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-usage", "Usage: /gamemode <mode> <player>"));
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                    .replace("{player}", args[1]));
                return true;
            }
        }
        
        target.setGameMode(gameMode);
        
        MessageUtils.sendMessage(target, plugin.getConfigManager().getMessagesConfig()
            .getString("admin.gamemode-changed", "{prefix}&aYour gamemode has been changed to {gamemode}!")
            .replace("{gamemode}", gameMode.toString().toLowerCase()));
        
        if (!target.equals(sender)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.gamemode-changed-other", "{prefix}&aChanged {player}'s gamemode to {gamemode}!")
                .replace("{player}", target.getName())
                .replace("{gamemode}", gameMode.toString().toLowerCase()));
        }
        
        return true;
    }
    
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.player-only", "This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("survivalcore.admin.teleport")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /teleport <player> or /teleport <x> <y> <z>"));
            return true;
        }
        
        if (args.length == 1) {
            // Teleport to player
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                    .replace("{player}", args[0]));
                return true;
            }
            
            player.teleport(target.getLocation());
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.teleported-to-player", "{prefix}&aTeleported to {player}!")
                .replace("{player}", target.getName()));
            
        } else if (args.length >= 3) {
            // Teleport to coordinates
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                
                player.teleport(new org.bukkit.Location(player.getWorld(), x, y, z));
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("admin.teleported-to-coords", "{prefix}&aTeleported to {x}, {y}, {z}!")
                    .replace("{x}", String.valueOf((int)x))
                    .replace("{y}", String.valueOf((int)y))
                    .replace("{z}", String.valueOf((int)z)));
                
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("admin.invalid-coordinates", "{prefix}&cInvalid coordinates!"));
            }
        }
        
        return true;
    }
    
    private boolean handleBackup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.admin.backup")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        String backupName = args.length > 0 ? args[0] : null;
        
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
            .getString("admin.backup-starting", "{prefix}&aStarting backup..."));
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = plugin.getAdminToolsManager().createBackup(backupName);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                        .getString("admin.backup-completed", "{prefix}&aBackup completed successfully!"));
                } else {
                    MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                        .getString("admin.backup-failed", "{prefix}&cBackup failed! Check console for errors."));
                }
            });
        });
        
        return true;
    }
    
    private GameMode parseGameMode(String input) {
        switch (input.toLowerCase()) {
            case "0":
            case "s":
            case "survival":
                return GameMode.SURVIVAL;
            case "1":
            case "c":
            case "creative":
                return GameMode.CREATIVE;
            case "2":
            case "a":
            case "adventure":
                return GameMode.ADVENTURE;
            case "3":
            case "sp":
            case "spectator":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }
}