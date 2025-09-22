package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.PlayerWarp;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;

/**
 * Handles Player Warp command functionality
 */
public class PlayerWarpCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public PlayerWarpCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isPlayerWarpEnabled()) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.feature-disabled", "This feature is currently disabled!"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.player-only", "This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "pwarp":
            case "playerwarp":
                return handlePlayerWarpCommand(player, args);
            case "pwarps":
            case "listpwarps":
            case "playerwarps":
                return handlePlayerWarpsCommand(player, args);
            case "setpwarp":
            case "setplayerwarp":
                return handleSetPlayerWarpCommand(player, args);
            case "delpwarp":
            case "deleteplayerwarp":
            case "removepwarp":
                return handleDelPlayerWarpCommand(player, args);
            default:
                return false;
        }
    }
    
    private boolean handlePlayerWarpCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.pwarp.use")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /pwarp <name>")
                .replace("{usage}", "/pwarp <name>"));
            return true;
        }
        
        // Check cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            if (plugin.getCooldownManager().hasCooldown(player, "pwarp")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "pwarp");
                String timeFormat = plugin.getCooldownManager().formatTime(remaining);
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("general.cooldown", "You must wait {time} before using this command again!")
                    .replace("{time}", timeFormat);
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        String warpName = args[0];
        PlayerWarp playerWarp = plugin.getPlayerWarpManager().getPlayerWarp(warpName);
        
        if (playerWarp == null) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.not-found", "Player warp '{warp}' not found!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        if (!playerWarp.isValidLocation()) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.not-found", "Player warp '{warp}' not found!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Set cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            int cooldown = plugin.getConfigManager().getPlayerWarpCooldown();
            plugin.getCooldownManager().setCooldown(player, "pwarp", cooldown);
        }
        
        // Teleport with delay
        performTeleportation(player, playerWarp);
        
        return true;
    }
    
    private boolean handlePlayerWarpsCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.pwarp.list")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        // Check if player wants to see only their own warps
        boolean ownOnly = args.length > 0 && args[0].equalsIgnoreCase("own");
        
        if (ownOnly) {
            List<PlayerWarp> ownedWarps = plugin.getPlayerWarpManager().getPlayerWarpsOwnedBy(player);
            
            if (ownedWarps.isEmpty()) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("pwarp.no-own-warps", "You don't have any player warps!"));
                return true;
            }
            
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.list-header", "Your player warps:"));
            
            String itemFormat = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.list-own-item", "- {warp} in {world} at {x}, {y}, {z}");
            
            for (PlayerWarp warp : ownedWarps) {
                if (warp.isValidLocation()) {
                    String warpInfo = itemFormat
                        .replace("{warp}", warp.getName())
                        .replace("{world}", warp.getWorldName())
                        .replace("{x}", String.valueOf((int) warp.getX()))
                        .replace("{y}", String.valueOf((int) warp.getY()))
                        .replace("{z}", String.valueOf((int) warp.getZ()));
                    
                    MessageUtils.sendMessage(player, warpInfo);
                }
            }
        } else {
            Collection<PlayerWarp> allWarps = plugin.getPlayerWarpManager().getAllPlayerWarps();
            
            if (allWarps.isEmpty()) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("pwarp.no-warps", "No player warps available!"));
                return true;
            }
            
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.list-header", "Available player warps:"));
            
            String itemFormat = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.list-item", "- {warp} by {owner} in {world}");
            
            for (PlayerWarp warp : allWarps) {
                if (warp.isValidLocation()) {
                    String ownerName = plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(warp.getPlayerUuid())).getName();
                    String warpInfo = itemFormat
                        .replace("{warp}", warp.getName())
                        .replace("{owner}", ownerName != null ? ownerName : "Unknown")
                        .replace("{world}", warp.getWorldName());
                    
                    MessageUtils.sendMessage(player, warpInfo);
                }
            }
        }
        
        return true;
    }
    
    private boolean handleSetPlayerWarpCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.pwarp.set")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /setpwarp <name>")
                .replace("{usage}", "/setpwarp <name>"));
            return true;
        }
        
        String warpName = args[0];
        
        // Validate warp name
        if (!warpName.matches("^[a-zA-Z0-9_]+$") || warpName.length() > 32) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.invalid-name", "Invalid warp name! Use only letters, numbers, and underscores."));
            return true;
        }
        
        // Check if warp already exists
        if (plugin.getPlayerWarpManager().playerWarpExists(warpName)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.already-exists", "You already have a player warp named '{warp}'!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Check max warps limit
        int maxWarps = plugin.getPlayerWarpManager().getMaxPlayerWarps(player);
        int currentWarps = plugin.getPlayerWarpManager().getPlayerWarpCount(player);
        
        if (currentWarps >= maxWarps) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.max-warps-reached", "You have reached the maximum number of player warps ({max})!")
                .replace("{max}", String.valueOf(maxWarps));
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Create the player warp
        Location location = player.getLocation();
        if (plugin.getPlayerWarpManager().createPlayerWarp(player, warpName, location)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.created", "Player warp '{warp}' has been created!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        } else {
            MessageUtils.sendMessage(player, "&cFailed to create player warp. Please try again.");
        }
        
        return true;
    }
    
    private boolean handleDelPlayerWarpCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.pwarp.delete")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /delpwarp <name>")
                .replace("{usage}", "/delpwarp <name>"));
            return true;
        }
        
        String warpName = args[0];
        
        // Check if player owns the warp
        if (!plugin.getPlayerWarpManager().playerOwnsWarp(player, warpName)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.not-owner", "You don't own the player warp '{warp}'!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        if (plugin.getPlayerWarpManager().deletePlayerWarp(player, warpName)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.deleted", "Player warp '{warp}' has been deleted!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
        } else {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("pwarp.not-found", "Player warp '{warp}' not found!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
        }
        
        return true;
    }
    
    private void performTeleportation(Player player, PlayerWarp playerWarp) {
        int delay = plugin.getConfigManager().getConfig().getInt("player-warps.teleport-delay", 3);
        boolean cancelOnMove = plugin.getConfigManager().getConfig().getBoolean("player-warps.cancel-on-move", true);
        
        if (delay <= 0) {
            // Instant teleport
            executeTeleport(player, playerWarp);
            return;
        }
        
        // Store initial location for movement check
        Location initialLocation = player.getLocation().clone();
        
        String delayMessage = plugin.getConfigManager().getMessagesConfig()
            .getString("general.teleporting", "Teleporting in {delay} seconds... Don't move!")
            .replace("{delay}", String.valueOf(delay));
        MessageUtils.sendMessage(player, delayMessage);
        
        new BukkitRunnable() {
            private int countdown = delay;
            
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Check if player moved (if enabled)
                if (cancelOnMove && hasPlayerMoved(player.getLocation(), initialLocation)) {
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                        .getString("general.teleport-cancelled", "Teleportation cancelled because you moved!"));
                    cancel();
                    return;
                }
                
                countdown--;
                
                if (countdown <= 0) {
                    executeTeleport(player, playerWarp);
                    cancel();
                } else if (countdown <= 3) {
                    MessageUtils.sendMessage(player, "&e" + countdown + "...");
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private void executeTeleport(Player player, PlayerWarp playerWarp) {
        Location location = playerWarp.getLocation();
        if (location == null) {
            MessageUtils.sendMessage(player, "&cPlayer warp location is invalid!");
            return;
        }
        
        player.teleport(location);
        
        String ownerName = plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(playerWarp.getPlayerUuid())).getName();
        String message = plugin.getConfigManager().getMessagesConfig()
            .getString("pwarp.teleported", "Teleported to player warp '{warp}' owned by {owner}!")
            .replace("{warp}", playerWarp.getName())
            .replace("{owner}", ownerName != null ? ownerName : "Unknown");
        MessageUtils.sendMessage(player, message);
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
    
    private boolean hasPlayerMoved(Location current, Location initial) {
        return current.getBlockX() != initial.getBlockX() ||
               current.getBlockY() != initial.getBlockY() ||
               current.getBlockZ() != initial.getBlockZ();
    }
}