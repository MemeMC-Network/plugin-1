package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.Kit;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Handles Kit command functionality
 */
public class KitCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public KitCommand(SurvivalCore plugin) {
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
        
        if (!plugin.getConfigManager().isKitEnabled()) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.feature-disabled", "This feature is currently disabled!"));
            return true;
        }
        
        if (args.length == 0) {
            // Open kit GUI
            if (player.hasPermission("survivalcore.kit.gui")) {
                plugin.getKitManager().openKitGUI(player);
            } else {
                listKits(player);
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                return handleListCommand(player);
            case "reload":
                return handleReloadCommand(player);
            default:
                // Try to give the kit
                return handleKitClaim(player, args[0]);
        }
    }
    
    private boolean handleKitClaim(Player player, String kitName) {
        if (!player.hasPermission("survivalcore.kit.use")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        plugin.getKitManager().giveKit(player, kitName);
        return true;
    }
    
    private boolean handleListCommand(Player player) {
        if (!player.hasPermission("survivalcore.kit.list")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        listKits(player);
        return true;
    }
    
    private boolean handleReloadCommand(Player player) {
        if (!player.hasPermission("survivalcore.kit.reload")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        plugin.getKitManager().reloadKits();
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
            .getString("kit.reloaded", "{prefix}&aKits reloaded successfully!"));
        return true;
    }
    
    private void listKits(Player player) {
        Collection<Kit> availableKits = plugin.getKitManager().getAvailableKits(player);
        
        if (availableKits.isEmpty()) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("kit.no-kits", "{prefix}&cNo kits available!"));
            return;
        }
        
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
            .getString("kit.list-header", "{prefix}&aAvailable Kits:"));
        
        for (Kit kit : availableKits) {
            String status = "";
            
            if (kit.isOneTime() && plugin.getKitManager().hasClaimedKit(player, kit.getName())) {
                status = " §c(Claimed)";
            } else if (kit.getCooldown() > 0 && plugin.getCooldownManager().hasCooldown(player, "kit_" + kit.getName())) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "kit_" + kit.getName());
                status = " §e(Cooldown: " + plugin.getCooldownManager().formatTime(remaining) + ")";
            } else {
                status = " §a(Available)";
            }
            
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("kit.list-item", "&7- &e{kit}&7: {description}{status}")
                .replace("{kit}", kit.getDisplayName())
                .replace("{description}", kit.getDescription().isEmpty() ? "No description" : kit.getDescription())
                .replace("{status}", status);
            
            MessageUtils.sendMessage(player, message);
        }
        
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
            .getString("kit.list-footer", "&7Use &e/kit <name> &7to claim a kit"));
    }
}