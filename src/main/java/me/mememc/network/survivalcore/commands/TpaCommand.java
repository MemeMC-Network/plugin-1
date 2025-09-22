package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.TpaRequest;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles TPA (Teleport Accept) command functionality
 */
public class TpaCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public TpaCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isTpaEnabled()) {
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
            case "tpa":
                return handleTpaCommand(player, args);
            case "tpahere":
                return handleTpaHereCommand(player, args);
            case "tpaccept":
                return handleTpAcceptCommand(player, args);
            case "tpdeny":
                return handleTpDenyCommand(player, args);
            default:
                return false;
        }
    }
    
    private boolean handleTpaCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.tpa.send")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /tpa <player>")
                .replace("{usage}", "/tpa <player>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                .replace("{player}", args[0]);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        if (target.equals(player)) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.cannot-request-self", "You cannot send a teleport request to yourself!"));
            return true;
        }
        
        // Check cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            if (plugin.getCooldownManager().hasCooldown(player, "tpa")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "tpa");
                String timeFormat = plugin.getCooldownManager().formatTime(remaining);
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("general.cooldown", "You must wait {time} before using this command again!")
                    .replace("{time}", timeFormat);
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        // Send request
        if (plugin.getTpaManager().sendRequest(player, target, TpaRequest.TpaType.TPA)) {
            // Set cooldown
            if (!player.hasPermission("survivalcore.bypass.cooldown")) {
                int cooldown = plugin.getConfigManager().getTpaCooldown();
                plugin.getCooldownManager().setCooldown(player, "tpa", cooldown);
            }
            
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.request-sent", "Teleport request sent to {player}!")
                .replace("{player}", target.getName());
            MessageUtils.sendMessage(player, message);
            
            String targetMessage = plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.request-received", "{player} wants to teleport to you. /tpaccept or /tpdeny")
                .replace("{player}", player.getName());
            MessageUtils.sendMessage(target, targetMessage);
            
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        } else {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.request-already-exists", "You already have a pending request to this player!"));
        }
        
        return true;
    }
    
    private boolean handleTpaHereCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.tpa.here")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /tpahere <player>")
                .replace("{usage}", "/tpahere <player>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                .replace("{player}", args[0]);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        if (target.equals(player)) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.cannot-request-self", "You cannot send a teleport request to yourself!"));
            return true;
        }
        
        // Check cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            if (plugin.getCooldownManager().hasCooldown(player, "tpa")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "tpa");
                String timeFormat = plugin.getCooldownManager().formatTime(remaining);
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("general.cooldown", "You must wait {time} before using this command again!")
                    .replace("{time}", timeFormat);
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        // Send request
        if (plugin.getTpaManager().sendRequest(player, target, TpaRequest.TpaType.TPA_HERE)) {
            // Set cooldown
            if (!player.hasPermission("survivalcore.bypass.cooldown")) {
                int cooldown = plugin.getConfigManager().getTpaCooldown();
                plugin.getCooldownManager().setCooldown(player, "tpa", cooldown);
            }
            
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.request-sent", "Teleport request sent to {player}!")
                .replace("{player}", target.getName());
            MessageUtils.sendMessage(player, message);
            
            String targetMessage = plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.request-received-here", "{player} wants you to teleport to them. /tpaccept or /tpdeny")
                .replace("{player}", player.getName());
            MessageUtils.sendMessage(target, targetMessage);
            
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        } else {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.request-already-exists", "You already have a pending request to this player!"));
        }
        
        return true;
    }
    
    private boolean handleTpAcceptCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.tpa.accept")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        TpaRequest request = plugin.getTpaManager().acceptRequest(player);
        if (request == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.no-pending-requests", "You have no pending teleport requests!"));
            return true;
        }
        
        Player requester = request.getRequester();
        Player target = request.getTarget();
        
        String message = plugin.getConfigManager().getMessagesConfig()
            .getString("tpa.request-accepted", "You accepted {player}'s teleport request!")
            .replace("{player}", requester.getName());
        MessageUtils.sendMessage(player, message);
        
        String requesterMessage = plugin.getConfigManager().getMessagesConfig()
            .getString("tpa.request-accepted-sender", "{player} accepted your teleport request!")
            .replace("{player}", player.getName());
        MessageUtils.sendMessage(requester, requesterMessage);
        
        // Perform teleportation with delay
        performTeleportation(request);
        
        return true;
    }
    
    private boolean handleTpDenyCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.tpa.deny")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        TpaRequest request = plugin.getTpaManager().denyRequest(player);
        if (request == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("tpa.no-pending-requests", "You have no pending teleport requests!"));
            return true;
        }
        
        Player requester = request.getRequester();
        
        String message = plugin.getConfigManager().getMessagesConfig()
            .getString("tpa.request-denied", "You denied {player}'s teleport request!")
            .replace("{player}", requester.getName());
        MessageUtils.sendMessage(player, message);
        
        String requesterMessage = plugin.getConfigManager().getMessagesConfig()
            .getString("tpa.request-denied-sender", "{player} denied your teleport request!")
            .replace("{player}", player.getName());
        MessageUtils.sendMessage(requester, requesterMessage);
        
        return true;
    }
    
    private void performTeleportation(TpaRequest request) {
        Player requester = request.getRequester();
        Player target = request.getTarget();
        
        int delay = plugin.getConfigManager().getConfig().getInt("tpa.teleport-delay", 3);
        boolean cancelOnMove = plugin.getConfigManager().getConfig().getBoolean("tpa.cancel-on-move", true);
        
        if (delay <= 0) {
            // Instant teleport
            executeTeleport(request);
            return;
        }
        
        // Store initial location for movement check
        Location initialLocation = requester.getLocation().clone();
        
        String delayMessage = plugin.getConfigManager().getMessagesConfig()
            .getString("general.teleporting", "Teleporting in {delay} seconds... Don't move!")
            .replace("{delay}", String.valueOf(delay));
        MessageUtils.sendMessage(requester, delayMessage);
        
        new BukkitRunnable() {
            private int countdown = delay;
            
            @Override
            public void run() {
                if (!requester.isOnline() || !target.isOnline()) {
                    cancel();
                    return;
                }
                
                // Check if player moved (if enabled)
                if (cancelOnMove && hasPlayerMoved(requester.getLocation(), initialLocation)) {
                    MessageUtils.sendMessage(requester, plugin.getConfigManager().getMessagesConfig()
                        .getString("general.teleport-cancelled", "Teleportation cancelled because you moved!"));
                    cancel();
                    return;
                }
                
                countdown--;
                
                if (countdown <= 0) {
                    executeTeleport(request);
                    cancel();
                } else if (countdown <= 3) {
                    MessageUtils.sendMessage(requester, "&e" + countdown + "...");
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private void executeTeleport(TpaRequest request) {
        Player requester = request.getRequester();
        Player target = request.getTarget();
        
        if (!requester.isOnline() || !target.isOnline()) {
            return;
        }
        
        Location teleportLocation;
        
        if (request.getType() == TpaRequest.TpaType.TPA) {
            // Requester teleports to target
            teleportLocation = target.getLocation();
            requester.teleport(teleportLocation);
            requester.playSound(requester.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else {
            // Target teleports to requester
            teleportLocation = requester.getLocation();
            target.teleport(teleportLocation);
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }
    
    private boolean hasPlayerMoved(Location current, Location initial) {
        return current.getBlockX() != initial.getBlockX() ||
               current.getBlockY() != initial.getBlockY() ||
               current.getBlockZ() != initial.getBlockZ();
    }
}