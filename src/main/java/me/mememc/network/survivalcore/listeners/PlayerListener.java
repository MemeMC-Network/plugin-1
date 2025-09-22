package me.mememc.network.survivalcore.listeners;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player-related events
 */
public class PlayerListener implements Listener {
    
    private final SurvivalCore plugin;
    
    public PlayerListener(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // TODO: Handle player join events
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // TODO: Handle player quit events (cleanup TPA requests, etc.)
    }
}