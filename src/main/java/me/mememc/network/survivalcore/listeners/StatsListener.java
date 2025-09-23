package me.mememc.network.survivalcore.listeners;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles events for player statistics tracking
 */
public class StatsListener implements Listener {
    
    private final SurvivalCore plugin;
    
    public StatsListener(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().loadPlayerStats(player.getUniqueId(), player.getName());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().unloadPlayerStats(player.getUniqueId());
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            plugin.getStatsManager().onBlockPlace(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            plugin.getStatsManager().onBlockBreak(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer != null && entity.getType() != EntityType.PLAYER) {
            plugin.getStatsManager().onMobKill(killer);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getStatsManager().onPlayerDeath(player);
        
        // Track PvP kills
        Player killer = player.getKiller();
        if (killer != null && killer instanceof Player) {
            plugin.getStatsManager().onPlayerKill(killer);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("stats.track-movement", true)) {
            return;
        }
        
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return; // Different world, don't count
        }
        
        double distance = event.getFrom().distance(event.getTo());
        double minDistance = plugin.getConfigManager().getConfig().getDouble("stats.min-move-distance", 1.0);
        
        if (distance >= minDistance) {
            plugin.getStatsManager().onPlayerMove(event.getPlayer(), distance);
        }
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player && !event.isCancelled()) {
            plugin.getStatsManager().onItemCraft((Player) event.getWhoClicked());
        }
    }
    
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            plugin.getStatsManager().onFishCatch(event.getPlayer());
        }
    }
}