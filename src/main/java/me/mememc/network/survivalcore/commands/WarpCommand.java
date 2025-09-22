package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles Warp command functionality
 */
public class WarpCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public WarpCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement Warp commands
        sender.sendMessage("Warp command not yet implemented");
        return true;
    }
}