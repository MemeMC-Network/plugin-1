package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles Home command functionality
 */
public class HomeCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public HomeCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement Home commands
        sender.sendMessage("Home command not yet implemented");
        return true;
    }
}