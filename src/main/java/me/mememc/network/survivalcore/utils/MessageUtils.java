package me.mememc.network.survivalcore.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Utility class for handling messages and formatting
 */
public class MessageUtils {
    
    /**
     * Send a formatted message to a command sender
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        sender.sendMessage(formatMessage(message));
    }
    
    /**
     * Format a message with color codes
     */
    public static String formatMessage(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Send a message with replacements
     */
    public static void sendMessage(CommandSender sender, String message, String... replacements) {
        String formattedMessage = message;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            formattedMessage = formattedMessage.replace(replacements[i], replacements[i + 1]);
        }
        sendMessage(sender, formattedMessage);
    }
    
    /**
     * Format time in seconds to a readable format
     */
    public static String formatTime(int seconds) {
        if (seconds <= 0) {
            return "0s";
        }
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("s");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Get a message from config with prefix
     */
    public static String getMessageWithPrefix(String configPath, String defaultMessage, ConfigManager configManager) {
        String prefix = configManager.getMessagesConfig().getString("general.prefix", "&8[&6SurvivalCore&8]&r ");
        String message = configManager.getMessagesConfig().getString(configPath, defaultMessage);
        return formatMessage(prefix + message);
    }
    
    /**
     * Colorize a message (alias for formatMessage)
     */
    public static String colorize(String message) {
        return formatMessage(message);
    }
}