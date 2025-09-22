package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles RTP command functionality
 */
public class RtpCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    
    public RtpCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: Implement RTP command
        sender.sendMessage("RTP command not yet implemented");
        return true;
    }
}