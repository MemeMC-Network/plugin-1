package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
        // TODO: Implement Shop command
        sender.sendMessage("Shop command not yet implemented");
        return true;
    }
}