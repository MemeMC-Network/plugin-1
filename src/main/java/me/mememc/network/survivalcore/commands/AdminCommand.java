package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles Admin command functionality
 */
public class AdminCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public AdminCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement Admin commands
        sender.sendMessage("Admin command not yet implemented");
        return true;
    }
}