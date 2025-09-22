package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles Player Warp command functionality
 */
public class PlayerWarpCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public PlayerWarpCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement Player Warp commands
        sender.sendMessage("Player Warp command not yet implemented");
        return true;
    }
}