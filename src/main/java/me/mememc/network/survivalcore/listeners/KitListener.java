package me.mememc.network.survivalcore.listeners;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles Kit GUI interactions
 */
public class KitListener implements Listener {
    
    private final SurvivalCore plugin;
    
    public KitListener(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        
        // Handle Kit GUI
        if (title.equals("§6§lKits")) {
            event.setCancelled(true);
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            
            // Check if close button
            if (clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            
            // Extract kit name from item lore or display name
            String itemName = clickedItem.getItemMeta().getDisplayName();
            if (itemName != null && itemName.contains("§l")) {
                String kitName = itemName.replace("§e§l", "").replace("§", "");
                
                // Find the kit by display name
                plugin.getKitManager().getAllKits().forEach(kit -> {
                    if (kit.getDisplayName().replace("&", "§").equals(itemName)) {
                        player.closeInventory();
                        plugin.getKitManager().giveKit(player, kit.getName());
                    }
                });
            }
            return;
        }
        
        // Handle Stats GUI
        if (title.startsWith("§6§lStats: ") || title.startsWith("§6§lLeaderboard: ")) {
            event.setCancelled(true);
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            
            // Check if close button
            if (clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
        }
    }
}