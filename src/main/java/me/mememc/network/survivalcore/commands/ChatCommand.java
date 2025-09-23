package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles Chat management command functionality
 */
public class ChatCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public ChatCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        if (commandName.equals("mute")) {
            return handleMuteCommand(sender, args);
        } else if (commandName.equals("unmute")) {
            return handleUnmuteCommand(sender, args);
        } else if (commandName.equals("clearchat")) {
            return handleClearChatCommand(sender, args);
        } else if (commandName.equals("mutechat")) {
            return handleMuteChatCommand(sender, args);
        }
        
        return true;
    }
    
    private boolean handleMuteCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.chat.mute")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /mute <player>"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                .replace("{player}", targetName));
            return true;
        }
        
        if (target.hasPermission("survivalcore.chat.bypass.mute")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("chat.cannot-mute", "{prefix}&cYou cannot mute this player!"));
            return true;
        }
        
        if (plugin.getChatManager().isMuted(target.getUniqueId())) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("chat.already-muted", "{prefix}&cPlayer {player} is already muted!")
                .replace("{player}", target.getName()));
            return true;
        }
        
        plugin.getChatManager().mutePlayer(target.getUniqueId());
        
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
            .getString("chat.player-muted", "{prefix}&aPlayer {player} has been muted!")
            .replace("{player}", target.getName()));
        
        MessageUtils.sendMessage(target, plugin.getConfigManager().getMessagesConfig()
            .getString("chat.you-were-muted", "{prefix}&cYou have been muted by {staff}!")
            .replace("{staff}", sender.getName()));
        
        return true;
    }
    
    private boolean handleUnmuteCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.chat.unmute")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /unmute <player>"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                .replace("{player}", targetName));
            return true;
        }
        
        if (!plugin.getChatManager().isMuted(target.getUniqueId())) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("chat.not-muted", "{prefix}&cPlayer {player} is not muted!")
                .replace("{player}", target.getName()));
            return true;
        }
        
        plugin.getChatManager().unmutePlayer(target.getUniqueId());
        
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
            .getString("chat.player-unmuted", "{prefix}&aPlayer {player} has been unmuted!")
            .replace("{player}", target.getName()));
        
        MessageUtils.sendMessage(target, plugin.getConfigManager().getMessagesConfig()
            .getString("chat.you-were-unmuted", "{prefix}&aYou have been unmuted by {staff}!")
            .replace("{staff}", sender.getName()));
        
        return true;
    }
    
    private boolean handleClearChatCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.chat.clear")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        plugin.getChatManager().clearChat();
        
        if (sender instanceof Player) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("chat.cleared-by-you", "{prefix}&aYou cleared the chat!"));
        }
        
        return true;
    }
    
    private boolean handleMuteChatCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survivalcore.chat.toggle")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        boolean currentState = plugin.getConfigManager().getConfig().getBoolean("chat.enabled", true);
        boolean newState = !currentState;
        
        plugin.getChatManager().toggleChat(newState);
        
        String message = newState 
            ? plugin.getConfigManager().getMessagesConfig().getString("chat.enabled-by-staff", "{prefix}&aYou enabled chat for everyone!")
            : plugin.getConfigManager().getMessagesConfig().getString("chat.disabled-by-staff", "{prefix}&cYou disabled chat for everyone!");
        
        MessageUtils.sendMessage(sender, message);
        
        return true;
    }
}