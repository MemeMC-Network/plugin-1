package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.Warp;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

/**
 * Handles Warp command functionality
 */
public class WarpCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public WarpCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isWarpEnabled()) {
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
            case "warp":
                return handleWarpCommand(player, args);
            case "warps":
            case "listwarps":
                return handleWarpsCommand(player, args);
            case "setwarp":
                return handleSetWarpCommand(player, args);
            case "delwarp":
            case "deletewarp":
            case "removewarp":
                return handleDelWarpCommand(player, args);
            default:
                return false;
        }
    }
    
    private boolean handleWarpCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.warp.use")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /warp <name>")
                .replace("{usage}", "/warp <name>"));
            return true;
        }
        
        // Check cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            if (plugin.getCooldownManager().hasCooldown(player, "warp")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "warp");
                String timeFormat = plugin.getCooldownManager().formatTime(remaining);
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("general.cooldown", "You must wait {time} before using this command again!")
                    .replace("{time}", timeFormat);
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        String warpName = args[0];
        Warp warp = plugin.getWarpManager().getWarp(warpName);
        
        if (warp == null) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("warp.not-found", "Warp '{warp}' not found!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        if (!warp.isValidLocation()) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("warp.not-found", "Warp '{warp}' not found!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Set cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            int cooldown = plugin.getConfigManager().getWarpCooldown();
            plugin.getCooldownManager().setCooldown(player, "warp", cooldown);
        }
        
        // Teleport with delay
        performTeleportation(player, warp);
        
        return true;
    }
    
    private boolean handleWarpsCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.warp.list")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        Collection<Warp> warps = plugin.getWarpManager().getAllWarps();
        
        if (warps.isEmpty()) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("warp.no-warps", "No warps available!"));
            return true;
        }
        
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
            .getString("warp.list-header", "Available warps:"));
        
        String itemFormat = plugin.getConfigManager().getMessagesConfig()
            .getString("warp.list-item", "- {warp} in {world} at {x}, {y}, {z}");
        
        for (Warp warp : warps) {
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
        
        return true;
    }
    
    private boolean handleSetWarpCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.warp.set")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /setwarp <name>")
                .replace("{usage}", "/setwarp <name>"));
            return true;
        }
        
        String warpName = args[0];
        
        // Validate warp name
        if (!warpName.matches("^[a-zA-Z0-9_]+$") || warpName.length() > 32) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("warp.invalid-name", "Invalid warp name! Use only letters, numbers, and underscores."));
            return true;
        }
        
        // Check if warp already exists
        if (plugin.getWarpManager().warpExists(warpName)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("warp.already-exists", "Warp '{warp}' already exists!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Create the warp
        Location location = player.getLocation();
        if (plugin.getWarpManager().createWarp(warpName, location, player.getUniqueId().toString())) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("warp.created", "Warp '{warp}' has been created!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        } else {
            MessageUtils.sendMessage(player, "&cFailed to create warp. Please try again.");
        }
        
        return true;
    }
    
    private boolean handleDelWarpCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.warp.delete")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /delwarp <name>")
                .replace("{usage}", "/delwarp <name>"));
            return true;
        }
        
        String warpName = args[0];
        
        if (plugin.getWarpManager().deleteWarp(warpName)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("warp.deleted", "Warp '{warp}' has been deleted!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
            
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
        } else {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("warp.not-found", "Warp '{warp}' not found!")
                .replace("{warp}", warpName);
            MessageUtils.sendMessage(player, message);
        }
        
        return true;
    }
    
    private void performTeleportation(Player player, Warp warp) {
        int delay = plugin.getConfigManager().getConfig().getInt("warps.teleport-delay", 3);
        boolean cancelOnMove = plugin.getConfigManager().getConfig().getBoolean("warps.cancel-on-move", true);
        
        if (delay <= 0) {
            // Instant teleport
            executeTeleport(player, warp);
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
                    executeTeleport(player, warp);
                    cancel();
                } else if (countdown <= 3) {
                    MessageUtils.sendMessage(player, "&e" + countdown + "...");
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private void executeTeleport(Player player, Warp warp) {
        Location location = warp.getLocation();
        if (location == null) {
            MessageUtils.sendMessage(player, "&cWarp location is invalid!");
            return;
        }
        
        player.teleport(location);
        
        String message = plugin.getConfigManager().getMessagesConfig()
            .getString("warp.teleported", "Teleported to warp '{warp}'!")
            .replace("{warp}", warp.getName());
        MessageUtils.sendMessage(player, message);
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
    
    private boolean hasPlayerMoved(Location current, Location initial) {
        return current.getBlockX() != initial.getBlockX() ||
               current.getBlockY() != initial.getBlockY() ||
               current.getBlockZ() != initial.getBlockZ();
    }
}