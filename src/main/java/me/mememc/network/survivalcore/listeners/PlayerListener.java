package me.mememc.network.survivalcore.listeners;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
        // Currently no specific actions needed on join
        // Could add welcome messages, data loading, etc. in the future
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up TPA requests when player leaves
        plugin.getTpaManager().removeAllRequests(event.getPlayer());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // Check if this is a shop inventory
        if (title.contains("Shop - ")) {
            event.setCancelled(true);
            
            if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
                int slot = event.getSlot();
                
                if (title.contains("Categories")) {
                    // Handle category selection
                    handleCategorySelection(player, slot);
                } else if (title.contains("Favorites")) {
                    // Handle favorites interactions
                    if (slot == 49) { // Close
                        player.closeInventory();
                    } else if (slot < 45) { // Favorite items
                        handleFavoriteItemClick(player, slot, event.isLeftClick(), event.isShiftClick());
                    }
                } else {
                    // Handle shop navigation and item clicks
                    if (slot == 45) { // Previous page
                        handlePreviousPage(player, title);
                    } else if (slot == 46) { // Favorites
                        handleFavorites(player);
                    } else if (slot == 49) { // Categories
                        handleCategoriesMenu(player);
                    } else if (slot == 50) { // Close
                        player.closeInventory();
                    } else if (slot == 51) { // Transaction History
                        handleTransactionHistory(player);
                    } else if (slot == 52) { // Search
                        handleSearchItems(player);
                    } else if (slot == 53) { // Next page
                        handleNextPage(player, title);
                    } else if (slot < 45) { // Item slots
                        handleShopItemClick(player, title, slot, event.isLeftClick());
                    }
                }
            }
        }
    }
    
    private void handleCategorySelection(org.bukkit.entity.Player player, int slot) {
        // Map slots to categories
        String category = null;
        switch (slot) {
            case 10:
                category = "blocks";
                break;
            case 12:
                category = "tools";
                break;
            case 14:
                category = "food";
                break;
            case 16:
                category = "enchantments";
                break;
            case 19:
                category = "potions";
                break;
            case 21:
                category = "decorations";
                break;
            case 22:
                player.closeInventory();
                return;
            case 23:
                category = "redstone";
                break;
        }
        
        if (category != null) {
            plugin.getShopManager().openCategoryShop(player, category, 1);
        }
    }
    
    private void handlePreviousPage(org.bukkit.entity.Player player, String title) {
        String[] parts = title.split(" - ");
        if (parts.length >= 2) {
            String categoryPart = parts[1];
            String category = categoryPart.split(" \\(Page ")[0].toLowerCase();
            String pagePart = categoryPart.split("\\(Page ")[1];
            int currentPage = Integer.parseInt(pagePart.replace(")", ""));
            
            if (currentPage > 1) {
                plugin.getShopManager().openCategoryShop(player, category, currentPage - 1);
            }
        }
    }
    
    private void handleNextPage(org.bukkit.entity.Player player, String title) {
        String[] parts = title.split(" - ");
        if (parts.length >= 2) {
            String categoryPart = parts[1];
            String category = categoryPart.split(" \\(Page ")[0].toLowerCase();
            String pagePart = categoryPart.split("\\(Page ")[1];
            int currentPage = Integer.parseInt(pagePart.replace(")", ""));
            
            plugin.getShopManager().openCategoryShop(player, category, currentPage + 1);
        }
    }
    
    private void handleCategoriesMenu(org.bukkit.entity.Player player) {
        plugin.getShopManager().openCategoriesMenu(player);
    }
    
    private void handleShopItemClick(org.bukkit.entity.Player player, String title, int slot, boolean isLeftClick) {
        String[] parts = title.split(" - ");
        if (parts.length >= 2) {
            String categoryPart = parts[1];
            String category = categoryPart.split(" \\(Page ")[0].toLowerCase();
            String pagePart = categoryPart.split("\\(Page ")[1];
            int page = Integer.parseInt(pagePart.replace(")", ""));
            
            if (isLeftClick) {
                plugin.getShopManager().processBuy(player, category, page, slot);
            } else {
                plugin.getShopManager().processSell(player, category, page, slot);
            }
        }
    }
    
    private void handleSearchItems(org.bukkit.entity.Player player) {
        player.closeInventory();
        player.sendMessage("§b§lSearch Feature");
        player.sendMessage("§7Type the item name you want to search for in chat:");
        player.sendMessage("§7Example: 'diamond sword' or 'enchanted book'");
        player.sendMessage("§7Type 'cancel' to cancel the search.");
        
        // TODO: Implement chat listener for search functionality
        // For now, just provide information
    }
    
    private void handleTransactionHistory(org.bukkit.entity.Player player) {
        player.closeInventory();
        player.sendMessage("§d§lTransaction History");
        player.sendMessage("§7Recent transactions:");
        player.sendMessage("§a+ $50.00 §7- Sold 64x Stone");
        player.sendMessage("§c- $100.00 §7- Bought 1x Diamond Sword");
        player.sendMessage("§a+ $25.00 §7- Sold 32x Wood");
        player.sendMessage("§7Use /shop to return to the shop");
        
        // TODO: Implement actual transaction storage and history
    }
    
    private void handleFavorites(org.bukkit.entity.Player player) {
        plugin.getShopManager().openFavoritesShop(player);
    }
    
    private void handleFavoriteItemClick(org.bukkit.entity.Player player, int slot, boolean isLeftClick, boolean isShiftClick) {
        if (isShiftClick && !isLeftClick) {
            // Shift + Right-click to remove from favorites
            player.sendMessage("§cRemoved item from favorites!");
            // TODO: Implement actual favorite removal
        } else if (isLeftClick) {
            // Buy item
            player.sendMessage("§aBuy functionality will be implemented with economy integration!");
        } else {
            // Sell item
            player.sendMessage("§cSell functionality will be implemented with economy integration!");
        }
    }
}