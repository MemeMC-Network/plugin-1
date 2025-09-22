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
     * Open the categories selection menu
     */
    public void openCategoriesMenu(Player player) {
        String title = plugin.getConfigManager().getShopConfig().getString("gui.title", "Shop") + " - Categories";
        int size = 27; // 3 rows for categories
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        ConfigurationSection categoriesSection = plugin.getConfigManager().getShopConfig()
            .getConfigurationSection("categories");
        
        if (categoriesSection != null) {
            Set<String> categoryKeys = categoriesSection.getKeys(false);
            int slot = 10; // Start at slot 10 for better layout
            
            for (String categoryKey : categoryKeys) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryKey);
                if (categorySection != null) {
                    String iconMaterial = categorySection.getString("icon", "STONE");
                    String displayName = categorySection.getString("display-name", categoryKey);
                    List<String> description = categorySection.getStringList("description");
                    
                    try {
                        Material material = Material.valueOf(iconMaterial.toUpperCase());
                        ItemStack categoryItem = new ItemStack(material);
                        ItemMeta meta = categoryItem.getItemMeta();
                        
                        if (meta != null) {
                            meta.setDisplayName(displayName.replace("&", "§"));
                            
                            List<String> lore = new ArrayList<>();
                            for (String line : description) {
                                lore.add(line.replace("&", "§"));
                            }
                            lore.add("");
                            lore.add("§7Click to browse " + displayName.replace("&", ""));
                            
                            meta.setLore(lore);
                            categoryItem.setItemMeta(meta);
                        }
                        
                        inventory.setItem(slot, categoryItem);
                        slot += 2; // Skip one slot for spacing
                        
                        if (slot >= 17) { // Move to next row
                            slot = slot - 9 + 2;
                        }
                        
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material for category " + categoryKey + ": " + iconMaterial);
                    }
                }
            }
        }
        
        // Add close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose Shop");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to close the shop");
            closeMeta.setLore(lore);
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(22, close);
        
        player.openInventory(inventory);
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
        
        // Add balance display
        addBalanceDisplay(inventory, player);
        
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
            
            // Dynamically load categories
            List<String> availableCategories = getAvailableCategories();
            for (String cat : availableCategories) {
                ConfigurationSection catSection = plugin.getConfigManager().getShopConfig()
                    .getConfigurationSection("categories." + cat);
                if (catSection != null) {
                    String displayName = catSection.getString("display-name", cat);
                    lore.add("§7- " + displayName.replace("&", ""));
                }
            }
            
            lore.add("");
            lore.add("§7Click to view all categories");
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
        
        // Search button (slot 52)
        ItemStack search = new ItemStack(Material.SPYGLASS);
        ItemMeta searchMeta = search.getItemMeta();
        if (searchMeta != null) {
            searchMeta.setDisplayName("§b§lSearch Items");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to search for specific items");
            lore.add("§7Type item name in chat");
            searchMeta.setLore(lore);
            search.setItemMeta(searchMeta);
        }
        inventory.setItem(52, search);
        
        // Transaction History button (slot 51)
        ItemStack history = new ItemStack(Material.BOOK);
        ItemMeta historyMeta = history.getItemMeta();
        if (historyMeta != null) {
            historyMeta.setDisplayName("§d§lTransaction History");
            List<String> lore = new ArrayList<>();
            lore.add("§7View your recent transactions");
            lore.add("§7Buy and sell history");
            historyMeta.setLore(lore);
            history.setItemMeta(historyMeta);
        }
        inventory.setItem(51, history);
        
        // Favorites button (slot 46)
        ItemStack favorites = new ItemStack(Material.NETHER_STAR);
        ItemMeta favoritesMeta = favorites.getItemMeta();
        if (favoritesMeta != null) {
            favoritesMeta.setDisplayName("§5§lFavorites");
            List<String> lore = new ArrayList<>();
            lore.add("§7View your favorite items");
            lore.add("§7Quick access to preferred items");
            favoritesMeta.setLore(lore);
            favorites.setItemMeta(favoritesMeta);
        }
        inventory.setItem(46, favorites);
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
    
    /**
     * Get player balance (placeholder for economy integration)
     */
    public double getPlayerBalance(Player player) {
        // TODO: Implement with economy plugin integration
        return 1000.0; // Placeholder balance
    }
    
    /**
     * Add a balance display item to the shop inventory
     */
    private void addBalanceDisplay(Inventory inventory, Player player) {
        ItemStack balance = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balance.getItemMeta();
        if (balanceMeta != null) {
            balanceMeta.setDisplayName("§6§lYour Balance");
            List<String> lore = new ArrayList<>();
            lore.add("§7Current balance: §6$" + String.format("%.2f", getPlayerBalance(player)));
            lore.add("");
            lore.add("§7This updates when you buy/sell items");
            balanceMeta.setLore(lore);
            balance.setItemMeta(balanceMeta);
        }
        inventory.setItem(48, balance);
    }
    
    /**
     * Open favorites shop for a player
     */
    public void openFavoritesShop(Player player) {
        String title = plugin.getConfigManager().getShopConfig().getString("gui.title", "Shop") + " - Favorites";
        int size = 54;
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Add some example favorite items (in a real implementation, this would be stored per player)
        ItemStack favoriteItem1 = createExampleFavoriteItem(Material.DIAMOND_SWORD, "§fFavorite: Diamond Sword", 500.0, 250.0);
        ItemStack favoriteItem2 = createExampleFavoriteItem(Material.ENCHANTED_BOOK, "§fFavorite: Enchanted Book", 100.0, 50.0);
        ItemStack favoriteItem3 = createExampleFavoriteItem(Material.GOLDEN_APPLE, "§fFavorite: Golden Apple", 100.0, 50.0);
        
        if (favoriteItem1 != null) inventory.setItem(10, favoriteItem1);
        if (favoriteItem2 != null) inventory.setItem(11, favoriteItem2);
        if (favoriteItem3 != null) inventory.setItem(12, favoriteItem3);
        
        // Add info item
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§e§lFavorites Info");
            List<String> lore = new ArrayList<>();
            lore.add("§7Right-click items in the shop");
            lore.add("§7while sneaking to add them");
            lore.add("§7to your favorites!");
            lore.add("");
            lore.add("§7Your favorites appear here");
            lore.add("§7for quick access.");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(40, info);
        
        // Add close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose Favorites");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to close favorites");
            closeMeta.setLore(lore);
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(49, close);
        
        player.openInventory(inventory);
    }
    
    private ItemStack createExampleFavoriteItem(Material material, String name, double buyPrice, double sellPrice) {
        try {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName(name.replace("&", "§"));
                
                List<String> lore = new ArrayList<>();
                lore.add("§aBuy: §f1x for §6$" + buyPrice);
                lore.add("§cSell: §f1x for §6$" + sellPrice);
                lore.add("");
                lore.add("§7Left-click to buy");
                lore.add("§7Right-click to sell");
                lore.add("§7Shift+Right-click to remove from favorites");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            return item;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error creating favorite item: " + e.getMessage());
            return null;
        }
    }
}