package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles TPA command functionality
 */
public class TpaCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public TpaCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement TPA commands
        sender.sendMessage("TPA command not yet implemented");
        return true;
    }
}