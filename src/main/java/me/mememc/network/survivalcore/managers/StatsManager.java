package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.PlayerStats;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player statistics tracking and leaderboards
 */
public class StatsManager {
    
    private final SurvivalCore plugin;
    private final Map<UUID, PlayerStats> playerStats = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerJoinTimes = new ConcurrentHashMap<>();
    private final DecimalFormat df = new DecimalFormat("#.##");
    
    public StatsManager(SurvivalCore plugin) {
        this.plugin = plugin;
        createStatsTables();
        
        // Load stats for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerStats(player.getUniqueId(), player.getName());
        }
        
        // Start periodic save task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllStats, 6000L, 6000L); // Save every 5 minutes
    }
    
    private void createStatsTables() {
        try {
            String autoIncrement = plugin.getConfigManager().getDatabaseType().equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT";
            
            plugin.getDatabaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS sc_player_stats (" +
                "id INTEGER PRIMARY KEY " + autoIncrement + "," +
                "player_uuid VARCHAR(36) UNIQUE NOT NULL," +
                "player_name VARCHAR(16) NOT NULL," +
                "time_played BIGINT DEFAULT 0," +
                "blocks_placed INTEGER DEFAULT 0," +
                "blocks_broken INTEGER DEFAULT 0," +
                "mobs_killed INTEGER DEFAULT 0," +
                "deaths INTEGER DEFAULT 0," +
                "players_killed INTEGER DEFAULT 0," +
                "distance_traveled DOUBLE DEFAULT 0.0," +
                "items_crafted INTEGER DEFAULT 0," +
                "fish_caught INTEGER DEFAULT 0," +
                "join_date BIGINT DEFAULT 0," +
                "last_seen BIGINT DEFAULT 0" +
                ")"
            );
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create stats tables", e);
        }
    }
    
    /**
     * Load player stats from database
     */
    public void loadPlayerStats(UUID playerUuid, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                    "SELECT * FROM sc_player_stats WHERE player_uuid = ?"
                );
                stmt.setString(1, playerUuid.toString());
                
                ResultSet rs = stmt.executeQuery();
                
                PlayerStats stats;
                if (rs.next()) {
                    stats = new PlayerStats(
                        playerUuid, playerName,
                        rs.getLong("time_played"),
                        rs.getInt("blocks_placed"),
                        rs.getInt("blocks_broken"),
                        rs.getInt("mobs_killed"),
                        rs.getInt("deaths"),
                        rs.getInt("players_killed"),
                        rs.getDouble("distance_traveled"),
                        rs.getInt("items_crafted"),
                        rs.getInt("fish_caught"),
                        rs.getLong("join_date"),
                        rs.getLong("last_seen")
                    );
                } else {
                    // Create new stats entry
                    stats = new PlayerStats(playerUuid, playerName);
                    savePlayerStats(stats);
                }
                
                playerStats.put(playerUuid, stats);
                playerJoinTimes.put(playerUuid, System.currentTimeMillis());
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error loading player stats for " + playerName, e);
            }
        });
    }
    
    /**
     * Save player stats to database
     */
    public void savePlayerStats(PlayerStats stats) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getDatabaseManager().executeUpdate(
                    "INSERT OR REPLACE INTO sc_player_stats " +
                    "(player_uuid, player_name, time_played, blocks_placed, blocks_broken, " +
                    "mobs_killed, deaths, players_killed, distance_traveled, items_crafted, " +
                    "fish_caught, join_date, last_seen) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    stats.getPlayerUuid().toString(),
                    stats.getPlayerName(),
                    stats.getTimePlayed(),
                    stats.getBlocksPlaced(),
                    stats.getBlocksBroken(),
                    stats.getMobsKilled(),
                    stats.getDeaths(),
                    stats.getPlayersKilled(),
                    stats.getDistanceTraveled(),
                    stats.getItemsCrafted(),
                    stats.getFishCaught(),
                    stats.getJoinDate(),
                    stats.getLastSeen()
                );
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error saving player stats", e);
            }
        });
    }
    
    /**
     * Save all loaded player stats
     */
    public void saveAllStats() {
        for (PlayerStats stats : playerStats.values()) {
            savePlayerStats(stats);
        }
    }
    
    /**
     * Update player session time
     */
    public void updatePlayerSession(UUID playerUuid) {
        Long joinTime = playerJoinTimes.get(playerUuid);
        if (joinTime != null) {
            PlayerStats stats = playerStats.get(playerUuid);
            if (stats != null) {
                long sessionTime = System.currentTimeMillis() - joinTime;
                stats.addTimePlayed(sessionTime);
                stats.setLastSeen(System.currentTimeMillis());
                playerJoinTimes.put(playerUuid, System.currentTimeMillis()); // Reset join time
            }
        }
    }
    
    /**
     * Get player statistics
     */
    public PlayerStats getPlayerStats(UUID playerUuid) {
        return playerStats.get(playerUuid);
    }
    
    /**
     * Display player statistics GUI
     */
    public void showPlayerStats(Player player, Player target) {
        PlayerStats stats = playerStats.get(target.getUniqueId());
        if (stats == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("stats.not-found", "{prefix}&cNo statistics found for that player!"));
            return;
        }
        
        Inventory inventory = Bukkit.createInventory(null, 54, "§6§lStats: " + target.getName());
        
        // Player head
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(target);
            skullMeta.setDisplayName("§e§l" + target.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Player Statistics");
            lore.add("");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            lore.add("§7Joined: §e" + sdf.format(new Date(stats.getJoinDate())));
            lore.add("§7Last Seen: §e" + sdf.format(new Date(stats.getLastSeen())));
            lore.add("§7Time Played: §e" + stats.formatTimePlayed());
            skullMeta.setLore(lore);
            playerHead.setItemMeta(skullMeta);
        }
        inventory.setItem(4, playerHead);
        
        // Building stats
        ItemStack building = new ItemStack(Material.BRICKS);
        ItemMeta buildingMeta = building.getItemMeta();
        if (buildingMeta != null) {
            buildingMeta.setDisplayName("§a§lBuilding Statistics");
            List<String> lore = new ArrayList<>();
            lore.add("§7Blocks Placed: §e" + stats.getBlocksPlaced());
            lore.add("§7Blocks Broken: §e" + stats.getBlocksBroken());
            lore.add("§7Items Crafted: §e" + stats.getItemsCrafted());
            buildingMeta.setLore(lore);
            building.setItemMeta(buildingMeta);
        }
        inventory.setItem(19, building);
        
        // Combat stats
        ItemStack combat = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta combatMeta = combat.getItemMeta();
        if (combatMeta != null) {
            combatMeta.setDisplayName("§c§lCombat Statistics");
            List<String> lore = new ArrayList<>();
            lore.add("§7Mobs Killed: §e" + stats.getMobsKilled());
            lore.add("§7Players Killed: §e" + stats.getPlayersKilled());
            lore.add("§7Deaths: §e" + stats.getDeaths());
            lore.add("§7K/D Ratio: §e" + df.format(stats.getKDRatio()));
            combatMeta.setLore(lore);
            combat.setItemMeta(combatMeta);
        }
        inventory.setItem(21, combat);
        
        // Travel stats
        ItemStack travel = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta travelMeta = travel.getItemMeta();
        if (travelMeta != null) {
            travelMeta.setDisplayName("§b§lTravel Statistics");
            List<String> lore = new ArrayList<>();
            lore.add("§7Distance Traveled: §e" + df.format(stats.getDistanceTraveled()) + " blocks");
            travelMeta.setLore(lore);
            travel.setItemMeta(travelMeta);
        }
        inventory.setItem(23, travel);
        
        // Survival stats
        ItemStack survival = new ItemStack(Material.FISHING_ROD);
        ItemMeta survivalMeta = survival.getItemMeta();
        if (survivalMeta != null) {
            survivalMeta.setDisplayName("§6§lSurvival Statistics");
            List<String> lore = new ArrayList<>();
            lore.add("§7Fish Caught: §e" + stats.getFishCaught());
            survivalMeta.setLore(lore);
            survival.setItemMeta(survivalMeta);
        }
        inventory.setItem(25, survival);
        
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
     * Show leaderboard GUI
     */
    public void showLeaderboard(Player player, String category) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§6§lLeaderboard: " + category);
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String orderBy = getOrderByColumn(category);
                if (orderBy == null) {
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                        .getString("stats.invalid-category", "{prefix}&cInvalid leaderboard category!"));
                    return;
                }
                
                PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                    "SELECT player_name, " + orderBy + " FROM sc_player_stats ORDER BY " + orderBy + " DESC LIMIT 45"
                );
                
                ResultSet rs = stmt.executeQuery();
                
                List<ItemStack> items = new ArrayList<>();
                int position = 1;
                
                while (rs.next() && position <= 45) {
                    String playerName = rs.getString("player_name");
                    long value = rs.getLong(orderBy);
                    
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName("§e#" + position + " " + playerName);
                        List<String> lore = new ArrayList<>();
                        lore.add("§7" + formatCategoryName(category) + ": §e" + formatValue(category, value));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    
                    items.add(item);
                    position++;
                }
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (int i = 0; i < items.size() && i < 45; i++) {
                        inventory.setItem(i, items.get(i));
                    }
                    
                    // Close button
                    ItemStack close = new ItemStack(Material.BARRIER);
                    ItemMeta closeMeta = close.getItemMeta();
                    if (closeMeta != null) {
                        closeMeta.setDisplayName("§c§lClose");
                        close.setItemMeta(closeMeta);
                    }
                    inventory.setItem(49, close);
                    
                    player.openInventory(inventory);
                });
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error loading leaderboard", e);
            }
        });
    }
    
    private String getOrderByColumn(String category) {
        switch (category.toLowerCase()) {
            case "playtime": return "time_played";
            case "blocks": return "blocks_placed";
            case "broken": return "blocks_broken";
            case "kills": return "mobs_killed";
            case "pvp": return "players_killed";
            case "deaths": return "deaths";
            case "travel": return "distance_traveled";
            case "crafted": return "items_crafted";
            case "fish": return "fish_caught";
            default: return null;
        }
    }
    
    private String formatCategoryName(String category) {
        switch (category.toLowerCase()) {
            case "playtime": return "Time Played";
            case "blocks": return "Blocks Placed";
            case "broken": return "Blocks Broken";
            case "kills": return "Mobs Killed";
            case "pvp": return "Players Killed";
            case "deaths": return "Deaths";
            case "travel": return "Distance Traveled";
            case "crafted": return "Items Crafted";
            case "fish": return "Fish Caught";
            default: return category;
        }
    }
    
    private String formatValue(String category, long value) {
        if (category.equals("playtime")) {
            long seconds = value / 1000;
            long hours = seconds / 3600;
            return hours + "h";
        } else if (category.equals("travel")) {
            return df.format(value) + " blocks";
        }
        return String.valueOf(value);
    }
    
    /**
     * Remove player stats from memory when they disconnect
     */
    public void unloadPlayerStats(UUID playerUuid) {
        updatePlayerSession(playerUuid); // Update final session time
        PlayerStats stats = playerStats.get(playerUuid);
        if (stats != null) {
            savePlayerStats(stats);
            playerStats.remove(playerUuid);
        }
        playerJoinTimes.remove(playerUuid);
    }
    
    // Event methods for tracking stats
    public void onBlockPlace(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null) {
            stats.incrementBlocksPlaced();
        }
    }
    
    public void onBlockBreak(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null) {
            stats.incrementBlocksBroken();
        }
    }
    
    public void onMobKill(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null) {
            stats.incrementMobsKilled();
        }
    }
    
    public void onPlayerDeath(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null) {
            stats.incrementDeaths();
        }
    }
    
    public void onPlayerKill(Player killer) {
        PlayerStats stats = playerStats.get(killer.getUniqueId());
        if (stats != null) {
            stats.incrementPlayersKilled();
        }
    }
    
    public void onPlayerMove(Player player, double distance) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null && distance > 0) {
            stats.addDistanceTraveled(distance);
        }
    }
    
    public void onItemCraft(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null) {
            stats.incrementItemsCrafted();
        }
    }
    
    public void onFishCatch(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null) {
            stats.incrementFishCaught();
        }
    }
}