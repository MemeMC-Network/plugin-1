package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Manages chat formatting, filters, and moderation features
 */
public class ChatManager implements Listener {
    
    private final SurvivalCore plugin;
    private final Set<UUID> mutedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastMessage = new ConcurrentHashMap<>();
    private final Set<String> bannedWords = new HashSet<>();
    private final List<Pattern> urlPatterns = new ArrayList<>();
    
    public ChatManager(SurvivalCore plugin) {
        this.plugin = plugin;
        loadBannedWords();
        loadUrlPatterns();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void loadBannedWords() {
        bannedWords.clear();
        List<String> words = plugin.getConfigManager().getConfig().getStringList("chat.banned-words");
        bannedWords.addAll(words);
    }
    
    private void loadUrlPatterns() {
        urlPatterns.clear();
        urlPatterns.add(Pattern.compile("(?i)https?://[\\w\\.-]+\\.[a-z]{2,}"));
        urlPatterns.add(Pattern.compile("(?i)www\\.[\\w\\.-]+\\.[a-z]{2,}"));
        urlPatterns.add(Pattern.compile("(?i)[\\w\\.-]+\\.(com|net|org|edu|gov|mil|int|co|uk|de|fr|it|es|nl|au|ca|jp|ru|br|in|mx|ch|se|no|dk|fi|pl|cz|hu|ro|bg|hr|si|sk|lt|lv|ee|ie|mt|cy|lu|is|li|mc|sm|va|ad|md|by|ua|mk|al|me|rs|ba|xk|tr|ge|am|az|kz|kg|uz|tm|tj|mn|cn|kp|kr|jp|ph|th|vn|my|sg|id|bn|kh|la|mm|bd|lk|np|bt|mv|af|pk|ir|iq|sy|lb|jo|il|ps|sa|ye|om|ae|qa|bh|kw|eg|ly|tn|dz|ma|sd|ss|et|so|dj|er|ke|ug|tz|rw|bi|mw|zm|zw|bw|sz|ls|za|na|mg|mu|sc|km|mz|ao|cd|cg|cf|td|cm|gq|ga|st|cv|gw|gm|sn|ml|bf|ne|ng|bj|tg|gh|ci|lr|sl|gn)");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Check if chat is globally enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("chat.enabled", true) &&
            !player.hasPermission("survivalcore.chat.bypass.disabled")) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("chat.disabled-for-player", "{prefix}&cChat is currently disabled!"));
            return;
        }
        
        // Check if player is muted
        if (mutedPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("chat.muted", "{prefix}&cYou are currently muted and cannot speak in chat!"));
            return;
        }
        
        // Chat cooldown check
        if (!player.hasPermission("survivalcore.chat.bypass.cooldown")) {
            int cooldown = plugin.getConfigManager().getConfig().getInt("chat.cooldown", 3);
            if (cooldown > 0) {
                Long lastTime = lastMessageTime.get(player.getUniqueId());
                if (lastTime != null && System.currentTimeMillis() - lastTime < cooldown * 1000L) {
                    event.setCancelled(true);
                    long remaining = (cooldown * 1000L - (System.currentTimeMillis() - lastTime)) / 1000L;
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                        .getString("chat.cooldown", "{prefix}&cYou must wait {time} seconds before sending another message!")
                        .replace("{time}", String.valueOf(remaining + 1)));
                    return;
                }
            }
            lastMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
        
        // Anti-spam check (repeated messages)
        if (!player.hasPermission("survivalcore.chat.bypass.spam")) {
            String lastMsg = lastMessage.get(player.getUniqueId());
            if (lastMsg != null && lastMsg.equalsIgnoreCase(message.trim())) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("chat.spam", "{prefix}&cPlease don't repeat the same message!"));
                return;
            }
            lastMessage.put(player.getUniqueId(), message.trim());
        }
        
        // Profanity filter
        if (plugin.getConfigManager().getConfig().getBoolean("chat.filter.enabled", true) && 
            !player.hasPermission("survivalcore.chat.bypass.filter")) {
            
            String filteredMessage = filterProfanity(message);
            if (!filteredMessage.equals(message)) {
                if (plugin.getConfigManager().getConfig().getBoolean("chat.filter.block-message", false)) {
                    event.setCancelled(true);
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                        .getString("chat.filtered", "{prefix}&cYour message contains inappropriate language and was blocked!"));
                    return;
                } else {
                    message = filteredMessage;
                    event.setMessage(message);
                }
            }
        }
        
        // URL filter
        if (plugin.getConfigManager().getConfig().getBoolean("chat.block-urls", true) && 
            !player.hasPermission("survivalcore.chat.bypass.urls")) {
            
            if (containsURL(message)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("chat.no-urls", "{prefix}&cYou cannot send URLs in chat!"));
                return;
            }
        }
        
        // Caps filter
        if (plugin.getConfigManager().getConfig().getBoolean("chat.caps-filter", true) && 
            !player.hasPermission("survivalcore.chat.bypass.caps")) {
            
            message = filterCaps(message);
            event.setMessage(message);
        }
        
        // Apply chat format
        if (plugin.getConfigManager().getConfig().getBoolean("chat.format.enabled", true)) {
            String format = getChatFormat(player);
            event.setFormat(format);
        }
        
        // Color codes permission
        if (player.hasPermission("survivalcore.chat.color")) {
            message = MessageUtils.colorize(message);
            event.setMessage(message);
        }
    }
    
    private String filterProfanity(String message) {
        String filtered = message;
        String replacement = plugin.getConfigManager().getConfig().getString("chat.filter.replacement", "***");
        
        for (String word : bannedWords) {
            filtered = filtered.replaceAll("(?i)" + Pattern.quote(word), replacement);
        }
        
        return filtered;
    }
    
    private boolean containsURL(String message) {
        for (Pattern pattern : urlPatterns) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }
        return false;
    }
    
    private String filterCaps(String message) {
        int maxCaps = plugin.getConfigManager().getConfig().getInt("chat.max-caps-percent", 50);
        if (maxCaps >= 100) return message;
        
        int totalLetters = 0;
        int capsLetters = 0;
        
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                totalLetters++;
                if (Character.isUpperCase(c)) {
                    capsLetters++;
                }
            }
        }
        
        if (totalLetters > 0 && (capsLetters * 100 / totalLetters) > maxCaps) {
            return message.toLowerCase();
        }
        
        return message;
    }
    
    private String getChatFormat(Player player) {
        String format = plugin.getConfigManager().getConfig().getString("chat.format.default", "&7{player}&f: {message}");
        
        // Check for group-specific formats
        Map<String, Object> groupFormats = plugin.getConfigManager().getConfig().getConfigurationSection("chat.format.groups").getValues(false);
        for (Map.Entry<String, Object> entry : groupFormats.entrySet()) {
            String permission = "survivalcore.chat.format." + entry.getKey();
            if (player.hasPermission(permission)) {
                format = (String) entry.getValue();
                break; // Use the first matching format
            }
        }
        
        // Replace placeholders
        format = format.replace("{player}", player.getDisplayName())
                      .replace("{name}", player.getName())
                      .replace("{world}", player.getWorld().getName());
        
        // Add color codes
        format = MessageUtils.colorize(format);
        
        return format;
    }
    
    /**
     * Mute a player
     */
    public void mutePlayer(UUID playerUuid) {
        mutedPlayers.add(playerUuid);
    }
    
    /**
     * Unmute a player
     */
    public void unmutePlayer(UUID playerUuid) {
        mutedPlayers.remove(playerUuid);
    }
    
    /**
     * Check if a player is muted
     */
    public boolean isMuted(UUID playerUuid) {
        return mutedPlayers.contains(playerUuid);
    }
    
    /**
     * Clear chat for all players
     */
    public void clearChat() {
        for (int i = 0; i < 100; i++) {
            Bukkit.broadcastMessage(" ");
        }
        Bukkit.broadcastMessage(MessageUtils.colorize(plugin.getConfigManager().getMessagesConfig()
            .getString("chat.cleared", "{prefix}&aChat has been cleared!")));
    }
    
    /**
     * Toggle chat for the server
     */
    public void toggleChat(boolean enabled) {
        plugin.getConfigManager().getConfig().set("chat.enabled", enabled);
        
        if (enabled) {
            Bukkit.broadcastMessage(MessageUtils.colorize(plugin.getConfigManager().getMessagesConfig()
                .getString("chat.enabled", "{prefix}&aChat has been enabled!")));
        } else {
            Bukkit.broadcastMessage(MessageUtils.colorize(plugin.getConfigManager().getMessagesConfig()
                .getString("chat.disabled", "{prefix}&cChat has been disabled!")));
        }
    }
    
    /**
     * Reload chat configuration
     */
    public void reloadConfig() {
        loadBannedWords();
        loadUrlPatterns();
    }
    
    /**
     * Get muted players count
     */
    public int getMutedPlayersCount() {
        return mutedPlayers.size();
    }
}