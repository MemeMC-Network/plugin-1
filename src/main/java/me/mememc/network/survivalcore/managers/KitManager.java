package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.Kit;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages kit system functionality
 */
public class KitManager {
    
    private final SurvivalCore plugin;
    private final Map<String, Kit> kits = new HashMap<>();
    private File kitConfigFile;
    private FileConfiguration kitConfig;
    
    public KitManager(SurvivalCore plugin) {
        this.plugin = plugin;
        createKitConfig();
        loadKits();
        createKitTables();
    }
    
    private void createKitConfig() {
        kitConfigFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitConfigFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        kitConfig = YamlConfiguration.loadConfiguration(kitConfigFile);
    }
    
    private void createKitTables() {
        try {
            plugin.getDatabaseManager().executeUpdate(
                "CREATE TABLE IF NOT EXISTS sc_kit_claims (" +
                "id INTEGER PRIMARY KEY " + (plugin.getConfigManager().getDatabaseType().equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "kit_name VARCHAR(32) NOT NULL," +
                "claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(player_uuid, kit_name)" +
                ")"
            );
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create kit tables", e);
        }
    }
    
    private void loadKits() {
        kits.clear();
        
        ConfigurationSection kitsSection = kitConfig.getConfigurationSection("kits");
        if (kitsSection == null) return;
        
        for (String kitName : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);
            if (kitSection == null) continue;
            
            try {
                String displayName = kitSection.getString("display-name", kitName);
                String description = kitSection.getString("description", "");
                int cooldown = kitSection.getInt("cooldown", 0);
                double cost = kitSection.getDouble("cost", 0.0);
                String permission = kitSection.getString("permission", "");
                boolean oneTime = kitSection.getBoolean("one-time", false);
                
                List<ItemStack> items = new ArrayList<>();
                ConfigurationSection itemsSection = kitSection.getConfigurationSection("items");
                if (itemsSection != null) {
                    for (String itemKey : itemsSection.getKeys(false)) {
                        ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
                        if (itemSection != null) {
                            ItemStack item = createItemFromConfig(itemSection);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }
                }
                
                Kit kit = new Kit(kitName, displayName, description, items, cooldown, cost, permission, oneTime);
                kits.put(kitName.toLowerCase(), kit);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit '" + kitName + "': " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + kits.size() + " kits");
    }
    
    private ItemStack createItemFromConfig(ConfigurationSection itemSection) {
        try {
            String materialName = itemSection.getString("material");
            if (materialName == null) return null;
            
            Material material = Material.valueOf(materialName.toUpperCase());
            int amount = itemSection.getInt("amount", 1);
            
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                String name = itemSection.getString("name");
                if (name != null) {
                    meta.setDisplayName(name.replace("&", "§"));
                }
                
                List<String> lore = itemSection.getStringList("lore");
                if (!lore.isEmpty()) {
                    List<String> formattedLore = new ArrayList<>();
                    for (String line : lore) {
                        formattedLore.add(line.replace("&", "§"));
                    }
                    meta.setLore(formattedLore);
                }
                
                item.setItemMeta(meta);
            }
            
            return item;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error creating item from config: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Give a kit to a player
     */
    public boolean giveKit(Player player, String kitName) {
        Kit kit = kits.get(kitName.toLowerCase());
        if (kit == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("kit.not-found", "{prefix}&cKit '{kit}' not found!")
                .replace("{kit}", kitName));
            return false;
        }
        
        // Check permission
        if (!kit.getPermission().isEmpty() && !player.hasPermission(kit.getPermission())) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "{prefix}&cYou don't have permission to use this command!"));
            return false;
        }
        
        // Check if one-time kit and already claimed
        if (kit.isOneTime() && hasClaimedKit(player, kitName)) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("kit.already-claimed", "{prefix}&cYou have already claimed this kit!")
                .replace("{kit}", kit.getDisplayName()));
            return false;
        }
        
        // Check cooldown
        if (kit.getCooldown() > 0 && !player.hasPermission("survivalcore.bypass.cooldown")) {
            if (plugin.getCooldownManager().hasCooldown(player, "kit_" + kitName)) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "kit_" + kitName);
                String timeFormat = plugin.getCooldownManager().formatTime(remaining);
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("kit.cooldown", "{prefix}&cYou must wait {time} before claiming this kit again!")
                    .replace("{time}", timeFormat)
                    .replace("{kit}", kit.getDisplayName()));
                return false;
            }
        }
        
        // Check cost (TODO: Implement economy integration)
        if (kit.getCost() > 0) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-economy", "{prefix}&cEconomy plugin not found! This feature requires Vault."));
            return false;
        }
        
        // Give items
        for (ItemStack item : kit.getItems()) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }
        
        // Set cooldown
        if (kit.getCooldown() > 0) {
            plugin.getCooldownManager().setCooldown(player, "kit_" + kitName, kit.getCooldown());
        }
        
        // Mark as claimed for one-time kits
        if (kit.isOneTime()) {
            markKitClaimed(player, kitName);
        }
        
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
            .getString("kit.received", "{prefix}&aYou received the '{kit}' kit!")
            .replace("{kit}", kit.getDisplayName()));
        
        return true;
    }
    
    /**
     * Open kit selection GUI
     */
    public void openKitGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§6§lKits");
        
        int slot = 0;
        for (Kit kit : kits.values()) {
            if (slot >= 45) break; // Leave space for navigation
            
            // Check if player can see this kit
            if (!kit.getPermission().isEmpty() && !player.hasPermission(kit.getPermission())) {
                continue;
            }
            
            ItemStack displayItem = createKitDisplayItem(player, kit);
            inventory.setItem(slot, displayItem);
            slot++;
        }
        
        // Add close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to close this menu");
            closeMeta.setLore(lore);
            close.setItemMeta(closeMeta);
        }
        inventory.setItem(49, close);
        
        player.openInventory(inventory);
    }
    
    private ItemStack createKitDisplayItem(Player player, Kit kit) {
        Material displayMaterial = kit.getItems().isEmpty() ? Material.CHEST : kit.getItems().get(0).getType();
        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e§l" + kit.getDisplayName());
            
            List<String> lore = new ArrayList<>();
            if (!kit.getDescription().isEmpty()) {
                lore.add("§7" + kit.getDescription());
                lore.add("");
            }
            
            lore.add("§7Items: §e" + kit.getItems().size());
            
            if (kit.getCooldown() > 0) {
                if (plugin.getCooldownManager().hasCooldown(player, "kit_" + kit.getName())) {
                    int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "kit_" + kit.getName());
                    lore.add("§7Cooldown: §c" + plugin.getCooldownManager().formatTime(remaining));
                } else {
                    lore.add("§7Cooldown: §a" + plugin.getCooldownManager().formatTime(kit.getCooldown()));
                }
            }
            
            if (kit.getCost() > 0) {
                lore.add("§7Cost: §6$" + kit.getCost());
            }
            
            if (kit.isOneTime()) {
                if (hasClaimedKit(player, kit.getName())) {
                    lore.add("§c§lALREADY CLAIMED");
                } else {
                    lore.add("§e§lONE-TIME KIT");
                }
            }
            
            lore.add("");
            
            boolean canClaim = true;
            if (kit.isOneTime() && hasClaimedKit(player, kit.getName())) {
                canClaim = false;
                lore.add("§c✗ Already claimed");
            } else if (kit.getCooldown() > 0 && plugin.getCooldownManager().hasCooldown(player, "kit_" + kit.getName())) {
                canClaim = false;
                lore.add("§c✗ On cooldown");
            } else if (!kit.getPermission().isEmpty() && !player.hasPermission(kit.getPermission())) {
                canClaim = false;
                lore.add("§c✗ No permission");
            } else {
                lore.add("§a✓ Click to claim");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Check if player has claimed a one-time kit
     */
    public boolean hasClaimedKit(Player player, String kitName) {
        try {
            PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "SELECT 1 FROM sc_kit_claims WHERE player_uuid = ? AND kit_name = ?"
            );
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, kitName);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error checking kit claim status", e);
            return false;
        }
    }
    
    /**
     * Mark a kit as claimed by player
     */
    private void markKitClaimed(Player player, String kitName) {
        try {
            plugin.getDatabaseManager().executeUpdate(
                "INSERT OR REPLACE INTO sc_kit_claims (player_uuid, kit_name) VALUES (?, ?)",
                player.getUniqueId().toString(), kitName
            );
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error marking kit as claimed", e);
        }
    }
    
    /**
     * Get all available kits
     */
    public Collection<Kit> getAllKits() {
        return kits.values();
    }
    
    /**
     * Get kit by name
     */
    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }
    
    /**
     * Get available kits for a player (based on permissions)
     */
    public List<Kit> getAvailableKits(Player player) {
        List<Kit> available = new ArrayList<>();
        for (Kit kit : kits.values()) {
            if (kit.getPermission().isEmpty() || player.hasPermission(kit.getPermission())) {
                available.add(kit);
            }
        }
        return available;
    }
    
    /**
     * Reload kits from configuration
     */
    public void reloadKits() {
        kitConfig = YamlConfiguration.loadConfiguration(kitConfigFile);
        loadKits();
    }
    
    /**
     * Save kit configuration
     */
    public void saveKitConfig() {
        try {
            kitConfig.save(kitConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save kit configuration", e);
        }
    }
}