package me.mememc.network.survivalcore.listeners;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles events related to admin tools
 */
public class AdminToolsListener implements Listener {
    
    private final SurvivalCore plugin;
    
    public AdminToolsListener(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Handle vanished players - hide them from new joiners
        for (java.util.UUID vanishedUuid : plugin.getAdminToolsManager().getVanishedPlayers()) {
            Player vanishedPlayer = plugin.getServer().getPlayer(vanishedUuid);
            if (vanishedPlayer != null && !player.hasPermission("survivalcore.admin.see-vanished")) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
        
        // If joining player is vanished, hide them from others
        if (plugin.getAdminToolsManager().isVanished(player)) {
            for (Player other : plugin.getServer().getOnlinePlayers()) {
                if (!other.hasPermission("survivalcore.admin.see-vanished")) {
                    other.hidePlayer(plugin, player);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getAdminToolsManager().cleanupPlayer(player.getUniqueId());
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Cancel damage if player has god mode
            if (plugin.getAdminToolsManager().hasGodMode(player)) {
                event.setCancelled(true);
            }
        }
    }
}