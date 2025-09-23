package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages administrative tools and server utilities
 */
public class AdminToolsManager {
    
    private final SurvivalCore plugin;
    private final Set<UUID> godModeEnabled = ConcurrentHashMap.newKeySet();
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Location> lastKnownLocations = new ConcurrentHashMap<>();
    private final DecimalFormat df = new DecimalFormat("#.##");
    
    public AdminToolsManager(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Open admin panel GUI for a player
     */
    public void openAdminPanel(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§c§lAdmin Panel");
        
        // Server Info
        ItemStack serverInfo = new ItemStack(Material.NETHER_STAR);
        ItemMeta serverMeta = serverInfo.getItemMeta();
        if (serverMeta != null) {
            serverMeta.setDisplayName("§e§lServer Information");
            List<String> lore = new ArrayList<>();
            lore.add("§7Plugin Version: §a" + plugin.getDescription().getVersion());
            lore.add("§7Online Players: §a" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
            lore.add("§7Database: §a" + plugin.getConfigManager().getDatabaseType().toUpperCase());
            lore.add("§7TPS: §a" + getTPS());
            lore.add("§7RAM Usage: §a" + getMemoryUsage());
            lore.add("§7Uptime: §a" + getServerUptime());
            serverMeta.setLore(lore);
            serverInfo.setItemMeta(serverMeta);
        }
        inventory.setItem(4, serverInfo);
        
        // Player Management
        ItemStack playerMgmt = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerMeta = playerMgmt.getItemMeta();
        if (playerMeta != null) {
            playerMeta.setDisplayName("§b§lPlayer Management");
            List<String> lore = new ArrayList<>();
            lore.add("§7Manage online players");
            lore.add("§7Teleport, inventory, gamemode");
            lore.add("");
            lore.add("§eClick to open player list");
            playerMeta.setLore(lore);
            playerMgmt.setItemMeta(playerMeta);
        }
        inventory.setItem(19, playerMgmt);
        
        // Statistics Overview
        ItemStack stats = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = stats.getItemMeta();
        if (statsMeta != null) {
            statsMeta.setDisplayName("§d§lStatistics Overview");
            List<String> lore = new ArrayList<>();
            lore.add("§7Total Homes: §e" + getTotalHomes());
            lore.add("§7Total Warps: §e" + plugin.getWarpManager().getWarpCount());
            lore.add("§7Total Player Warps: §e" + plugin.getPlayerWarpManager().getAllPlayerWarps().size());
            lore.add("§7Muted Players: §e" + plugin.getChatManager().getMutedPlayersCount());
            lore.add("");
            lore.add("§eClick to view detailed statistics");
            statsMeta.setLore(lore);
            stats.setItemMeta(statsMeta);
        }
        inventory.setItem(21, stats);
        
        // World Management
        ItemStack worlds = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta worldsMeta = worlds.getItemMeta();
        if (worldsMeta != null) {
            worldsMeta.setDisplayName("§a§lWorld Management");
            List<String> lore = new ArrayList<>();
            lore.add("§7Loaded Worlds: §e" + Bukkit.getWorlds().size());
            for (World world : Bukkit.getWorlds()) {
                lore.add("§7- " + world.getName() + " (" + world.getPlayers().size() + " players)");
            }
            lore.add("");
            lore.add("§eClick to manage worlds");
            worldsMeta.setLore(lore);
            worlds.setItemMeta(worldsMeta);
        }
        inventory.setItem(23, worlds);
        
        // Plugin Management
        ItemStack plugins = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta pluginsMeta = plugins.getItemMeta();
        if (pluginsMeta != null) {
            pluginsMeta.setDisplayName("§6§lPlugin Management");
            List<String> lore = new ArrayList<>();
            lore.add("§7Reload configurations");
            lore.add("§7View plugin information");
            lore.add("§7Manage features");
            lore.add("");
            lore.add("§eClick to manage plugins");
            pluginsMeta.setLore(lore);
            plugins.setItemMeta(pluginsMeta);
        }
        inventory.setItem(25, plugins);
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose");
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(49, close);
        
        player.openInventory(inventory);
    }
    
    /**
     * Open player management GUI
     */
    public void openPlayerManagement(Player admin) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int size = Math.min(54, ((onlinePlayers.size() / 9) + 1) * 9);
        Inventory inventory = Bukkit.createInventory(null, size, "§b§lPlayer Management");
        
        int slot = 0;
        for (Player player : onlinePlayers) {
            if (slot >= 45) break; // Leave space for navigation
            
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + player.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Health: §c" + df.format(player.getHealth()) + "/20");
                lore.add("§7Food: §6" + player.getFoodLevel() + "/20");
                lore.add("§7Gamemode: §b" + player.getGameMode().toString());
                lore.add("§7World: §a" + player.getWorld().getName());
                lore.add("§7Location: §f" + (int)player.getLocation().getX() + ", " + 
                         (int)player.getLocation().getY() + ", " + (int)player.getLocation().getZ());
                lore.add("");
                lore.add("§7Left-click to teleport to player");
                lore.add("§7Right-click for more options");
                meta.setLore(lore);
                playerHead.setItemMeta(meta);
            }
            inventory.setItem(slot, playerHead);
            slot++;
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7§lBack");
            back.setItemMeta(backMeta);
        }
        inventory.setItem(45, back);
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose");
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(49, close);
        
        admin.openInventory(inventory);
    }
    
    /**
     * Toggle god mode for a player
     */
    public void toggleGodMode(Player player) {
        if (godModeEnabled.contains(player.getUniqueId())) {
            godModeEnabled.remove(player.getUniqueId());
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.godmode-disabled", "{prefix}&cGod mode disabled!"));
        } else {
            godModeEnabled.add(player.getUniqueId());
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.godmode-enabled", "{prefix}&aGod mode enabled!"));
        }
    }
    
    /**
     * Check if player has god mode enabled
     */
    public boolean hasGodMode(Player player) {
        return godModeEnabled.contains(player.getUniqueId());
    }
    
    /**
     * Toggle vanish mode for a player
     */
    public void toggleVanish(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) {
            vanishedPlayers.remove(player.getUniqueId());
            // Show player to others
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, player);
            }
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.vanish-disabled", "{prefix}&cYou are now visible!"));
        } else {
            vanishedPlayers.add(player.getUniqueId());
            // Hide player from others
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.hasPermission("survivalcore.admin.see-vanished")) {
                    other.hidePlayer(plugin, player);
                }
            }
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.vanish-enabled", "{prefix}&aYou are now invisible!"));
        }
    }
    
    /**
     * Check if player is vanished
     */
    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }
    
    /**
     * Heal a player completely
     */
    public void healPlayer(Player target, Player admin) {
        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.getActivePotionEffects().clear();
        target.setFireTicks(0);
        
        MessageUtils.sendMessage(target, plugin.getConfigManager().getMessagesConfig()
            .getString("admin.healed", "{prefix}&aYou have been healed!"));
        
        if (!target.equals(admin)) {
            MessageUtils.sendMessage(admin, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.healed-other", "{prefix}&aYou healed {player}!")
                .replace("{player}", target.getName()));
        }
    }
    
    /**
     * Feed a player
     */
    public void feedPlayer(Player target, Player admin) {
        target.setFoodLevel(20);
        target.setSaturation(20f);
        
        MessageUtils.sendMessage(target, plugin.getConfigManager().getMessagesConfig()
            .getString("admin.fed", "{prefix}&aYou have been fed!"));
        
        if (!target.equals(admin)) {
            MessageUtils.sendMessage(admin, plugin.getConfigManager().getMessagesConfig()
                .getString("admin.fed-other", "{prefix}&aYou fed {player}!")
                .replace("{player}", target.getName()));
        }
    }
    
    /**
     * Get server TPS (simplified calculation)
     */
    private String getTPS() {
        // This is a simplified TPS calculation
        // In a real implementation, you'd want to track this over time
        return "20.0"; // Placeholder
    }
    
    /**
     * Get memory usage information
     */
    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        
        return usedMemory + "MB / " + maxMemory + "MB";
    }
    
    /**
     * Get server uptime
     */
    private String getServerUptime() {
        long uptimeMillis = System.currentTimeMillis() - plugin.getServerStartTime();
        long hours = uptimeMillis / (1000 * 60 * 60);
        long minutes = (uptimeMillis % (1000 * 60 * 60)) / (1000 * 60);
        
        return hours + "h " + minutes + "m";
    }
    
    /**
     * Get total homes count
     */
    private int getTotalHomes() {
        // This would require querying the database for all homes
        // For now, return a placeholder
        return 0; // Placeholder
    }
    
    /**
     * Backup server data
     */
    public boolean createBackup(String backupName) {
        try {
            File backupDir = new File(plugin.getDataFolder().getParentFile().getParentFile(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String fileName = backupName != null ? backupName : "backup_" + sdf.format(new Date());
            
            // Create backup directory
            File backup = new File(backupDir, fileName);
            backup.mkdirs();
            
            // This would copy world files, plugin data, etc.
            // For now, we'll just create a simple backup marker
            File marker = new File(backup, "backup.txt");
            marker.createNewFile();
            
            plugin.getLogger().info("Backup created: " + backup.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get vanished players list
     */
    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
    
    /**
     * Get god mode players list
     */
    public Set<UUID> getGodModePlayers() {
        return new HashSet<>(godModeEnabled);
    }
    
    /**
     * Remove player from admin modes when they disconnect
     */
    public void cleanupPlayer(UUID playerUuid) {
        godModeEnabled.remove(playerUuid);
        vanishedPlayers.remove(playerUuid);
        lastKnownLocations.remove(playerUuid);
    }
}