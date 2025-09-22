package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manages shop system functionality
 */
public class ShopManager {
    
    private final SurvivalCore plugin;
    
    public ShopManager(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Open the main shop GUI for a player
     */
    public void openMainShop(Player player) {
        openCategoryShop(player, "blocks", 1);
    }
    
    /**
     * Open a specific category shop
     */
    public void openCategoryShop(Player player, String category, int page) {
        ConfigurationSection categorySection = plugin.getConfigManager().getShopConfig()
            .getConfigurationSection("categories." + category);
        
        if (categorySection == null) {
            player.sendMessage("§cShop category not found!");
            return;
        }
        
        String displayName = categorySection.getString("display-name", category);
        String title = plugin.getConfigManager().getShopConfig().getString("gui.title", "Shop") 
                      + " - " + displayName + " (Page " + page + ")";
        
        int size = plugin.getConfigManager().getShopConfig().getInt("gui.size", 54);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Load items for this page
        ConfigurationSection pageSection = categorySection.getConfigurationSection("page-" + page);
        if (pageSection != null) {
            Set<String> itemKeys = pageSection.getKeys(false);
            
            for (String key : itemKeys) {
                try {
                    int slot = Integer.parseInt(key) - 1; // Convert to 0-based index
                    if (slot >= 0 && slot < 45) { // Only use first 45 slots for items
                        ItemStack item = createShopItem(pageSection.getConfigurationSection(key));
                        if (item != null) {
                            inventory.setItem(slot, item);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid slot numbers
                }
            }
        }
        
        // Add navigation items
        addNavigationItems(inventory, category, page);
        
        player.openInventory(inventory);
    }
    
    /**
     * Create a shop item from configuration
     */
    private ItemStack createShopItem(ConfigurationSection itemSection) {
        if (itemSection == null) return null;
        
        try {
            String materialName = itemSection.getString("material");
            Material material = Material.valueOf(materialName.toUpperCase());
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                String name = itemSection.getString("name");
                if (name != null) {
                    meta.setDisplayName(name.replace("&", "§"));
                }
                
                List<String> lore = new ArrayList<>();
                
                // Add price information
                double buyPrice = itemSection.getDouble("buy-price", -1);
                double sellPrice = itemSection.getDouble("sell-price", -1);
                int buyAmount = itemSection.getInt("buy-amount", 1);
                int sellAmount = itemSection.getInt("sell-amount", 1);
                
                if (buyPrice > 0) {
                    lore.add("§aBuy: §f" + buyAmount + "x for §6$" + buyPrice);
                }
                if (sellPrice > 0) {
                    lore.add("§cSell: §f" + sellAmount + "x for §6$" + sellPrice);
                }
                
                lore.add("");
                lore.add("§7Left-click to buy");
                lore.add("§7Right-click to sell");
                
                // Add custom lore if specified
                List<String> customLore = itemSection.getStringList("lore");
                if (!customLore.isEmpty()) {
                    lore.add("");
                    for (String line : customLore) {
                        lore.add(line.replace("&", "§"));
                    }
                }
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            return item;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error creating shop item: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Add navigation items to the shop inventory
     */
    private void addNavigationItems(Inventory inventory, String category, int page) {
        // Previous page button (slot 45)
        if (page > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§c§lPrevious Page");
                List<String> lore = new ArrayList<>();
                lore.add("§7Click to go to page " + (page - 1));
                meta.setLore(lore);
                prevPage.setItemMeta(meta);
            }
            inventory.setItem(45, prevPage);
        }
        
        // Categories button (slot 49)
        ItemStack categories = new ItemStack(Material.COMPASS);
        ItemMeta categoriesMeta = categories.getItemMeta();
        if (categoriesMeta != null) {
            categoriesMeta.setDisplayName("§e§lCategories");
            List<String> lore = new ArrayList<>();
            lore.add("§7Available categories:");
            lore.add("§7- Blocks");
            lore.add("§7- Tools");
            lore.add("§7- Food");
            categoriesMeta.setLore(lore);
            categories.setItemMeta(categoriesMeta);
        }
        inventory.setItem(49, categories);
        
        // Close button (slot 50)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose Shop");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to close the shop");
            closeMeta.setLore(lore);
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(50, close);
        
        // Next page button (slot 53)
        // Check if next page exists
        if (hasNextPage(category, page)) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a§lNext Page");
                List<String> lore = new ArrayList<>();
                lore.add("§7Click to go to page " + (page + 1));
                meta.setLore(lore);
                nextPage.setItemMeta(meta);
            }
            inventory.setItem(53, nextPage);
        }
    }
    
    /**
     * Check if a category has a next page
     */
    private boolean hasNextPage(String category, int currentPage) {
        ConfigurationSection categorySection = plugin.getConfigManager().getShopConfig()
            .getConfigurationSection("categories." + category);
        
        if (categorySection == null) return false;
        
        return categorySection.getConfigurationSection("page-" + (currentPage + 1)) != null;
    }
    
    /**
     * Get available shop categories
     */
    public List<String> getAvailableCategories() {
        List<String> categories = new ArrayList<>();
        ConfigurationSection categoriesSection = plugin.getConfigManager().getShopConfig()
            .getConfigurationSection("categories");
        
        if (categoriesSection != null) {
            categories.addAll(categoriesSection.getKeys(false));
        }
        
        return categories;
    }
    
    /**
     * Process a shop transaction (placeholder for future economy integration)
     */
    public boolean processBuy(Player player, String category, int page, int slot) {
        // TODO: Implement actual buying logic with economy integration
        player.sendMessage("§aBuy functionality will be implemented with economy integration!");
        return true;
    }
    
    /**
     * Process a shop sell transaction (placeholder for future economy integration)
     */
    public boolean processSell(Player player, String category, int page, int slot) {
        // TODO: Implement actual selling logic with economy integration
        player.sendMessage("§cSell functionality will be implemented with economy integration!");
        return true;
    }
}