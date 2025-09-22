package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handles Shop command functionality
 */
public class ShopCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public ShopCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isShopEnabled()) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.feature-disabled", "This feature is currently disabled!"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessagesConfig()
                .getString("general.player-only", "This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("survivalcore.shop.use")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            // Open main shop (default to blocks category)
            plugin.getShopManager().openMainShop(player);
            
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("shop.opened", "Opened the shop!"));
        } else {
            String category = args[0].toLowerCase();
            List<String> availableCategories = plugin.getShopManager().getAvailableCategories();
            
            if (!availableCategories.contains(category)) {
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("shop.category-not-found", "Shop category '{category}' not found!")
                    .replace("{category}", category);
                MessageUtils.sendMessage(player, message);
                
                // Show available categories
                MessageUtils.sendMessage(player, "&7Available categories: &e" + String.join(", ", availableCategories));
                return true;
            }
            
            // Open specific category
            plugin.getShopManager().openCategoryShop(player, category, 1);
            
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("shop.opened", "Opened the shop!"));
        }
        
        return true;
    }
}