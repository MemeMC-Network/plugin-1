package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.Home;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

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
        if (!plugin.getConfigManager().isHomeEnabled()) {
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
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "home":
                return handleHomeCommand(player, args);
            case "sethome":
                return handleSetHomeCommand(player, args);
            case "delhome":
            case "deletehome":
            case "removehome":
                return handleDelHomeCommand(player, args);
            case "homes":
            case "listhomes":
                return handleHomesCommand(player, args);
            default:
                return false;
        }
    }
    
    private boolean handleHomeCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.home.use")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        // Check cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            if (plugin.getCooldownManager().hasCooldown(player, "home")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "home");
                String timeFormat = plugin.getCooldownManager().formatTime(remaining);
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("general.cooldown", "You must wait {time} before using this command again!")
                    .replace("{time}", timeFormat);
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        String homeName = "home"; // Default home name
        if (args.length > 0) {
            homeName = args[0];
        }
        
        Home home;
        if (args.length == 0) {
            // Get default home
            home = plugin.getHomeManager().getDefaultHome(player);
        } else {
            // Get specific home
            home = plugin.getHomeManager().getHome(player, homeName);
        }
        
        if (home == null) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("home.not-found", "Home '{home}' not found!")
                .replace("{home}", homeName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        if (!home.isValidLocation()) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("home.not-found", "Home '{home}' not found!")
                .replace("{home}", homeName);
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Set cooldown
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            int cooldown = plugin.getConfigManager().getHomeCooldown();
            plugin.getCooldownManager().setCooldown(player, "home", cooldown);
        }
        
        // Teleport with delay
        performTeleportation(player, home);
        
        return true;
    }
    
    private boolean handleSetHomeCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.home.set")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        String homeName = "home"; // Default home name
        if (args.length > 0) {
            homeName = args[0];
        }
        
        // Validate home name
        if (!homeName.matches("^[a-zA-Z0-9_]+$") || homeName.length() > 32) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("home.invalid-name", "Invalid home name! Use only letters, numbers, and underscores."));
            return true;
        }
        
        // Check if home already exists
        boolean homeExists = plugin.getHomeManager().hasHome(player, homeName);
        
        // Check max homes limit (only for new homes)
        if (!homeExists) {
            int maxHomes = plugin.getHomeManager().getMaxHomes(player);
            Map<String, Home> currentHomes = plugin.getHomeManager().getPlayerHomes(player);
            
            if (currentHomes.size() >= maxHomes) {
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("home.max-homes-reached", "You have reached the maximum number of homes ({max})!")
                    .replace("{max}", String.valueOf(maxHomes));
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        // Set the home
        Location location = player.getLocation();
        if (plugin.getHomeManager().setHome(player, homeName, location)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("home.set", "Home '{home}' has been set!")
                .replace("{home}", homeName);
            MessageUtils.sendMessage(player, message);
            
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        } else {
            MessageUtils.sendMessage(player, "&cFailed to set home. Please try again.");
        }
        
        return true;
    }
    
    private boolean handleDelHomeCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.home.delete")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.invalid-usage", "Usage: /delhome <name>")
                .replace("{usage}", "/delhome <name>"));
            return true;
        }
        
        String homeName = args[0];
        
        if (plugin.getHomeManager().deleteHome(player, homeName)) {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("home.deleted", "Home '{home}' has been deleted!")
                .replace("{home}", homeName);
            MessageUtils.sendMessage(player, message);
            
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
        } else {
            String message = plugin.getConfigManager().getMessagesConfig()
                .getString("home.not-found", "Home '{home}' not found!")
                .replace("{home}", homeName);
            MessageUtils.sendMessage(player, message);
        }
        
        return true;
    }
    
    private boolean handleHomesCommand(Player player, String[] args) {
        if (!player.hasPermission("survivalcore.home.list")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        Map<String, Home> homes = plugin.getHomeManager().getPlayerHomes(player);
        
        if (homes.isEmpty()) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("home.no-homes", "You don't have any homes set!"));
            return true;
        }
        
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
            .getString("home.list-header", "Your homes:"));
        
        String itemFormat = plugin.getConfigManager().getMessagesConfig()
            .getString("home.list-item", "- {home} in {world} at {x}, {y}, {z}");
        
        for (Home home : homes.values()) {
            if (home.isValidLocation()) {
                String homeInfo = itemFormat
                    .replace("{home}", home.getName())
                    .replace("{world}", home.getWorldName())
                    .replace("{x}", String.valueOf((int) home.getX()))
                    .replace("{y}", String.valueOf((int) home.getY()))
                    .replace("{z}", String.valueOf((int) home.getZ()));
                
                MessageUtils.sendMessage(player, homeInfo);
            }
        }
        
        return true;
    }
    
    private void performTeleportation(Player player, Home home) {
        int delay = plugin.getConfigManager().getConfig().getInt("homes.teleport-delay", 3);
        boolean cancelOnMove = plugin.getConfigManager().getConfig().getBoolean("homes.cancel-on-move", true);
        
        if (delay <= 0) {
            // Instant teleport
            executeTeleport(player, home);
            return;
        }
        
        // Store initial location for movement check
        Location initialLocation = player.getLocation().clone();
        
        String delayMessage = plugin.getConfigManager().getMessagesConfig()
            .getString("general.teleporting", "Teleporting in {delay} seconds... Don't move!")
            .replace("{delay}", String.valueOf(delay));
        MessageUtils.sendMessage(player, delayMessage);
        
        new BukkitRunnable() {
            private int countdown = delay;
            
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Check if player moved (if enabled)
                if (cancelOnMove && hasPlayerMoved(player.getLocation(), initialLocation)) {
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                        .getString("general.teleport-cancelled", "Teleportation cancelled because you moved!"));
                    cancel();
                    return;
                }
                
                countdown--;
                
                if (countdown <= 0) {
                    executeTeleport(player, home);
                    cancel();
                } else if (countdown <= 3) {
                    MessageUtils.sendMessage(player, "&e" + countdown + "...");
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private void executeTeleport(Player player, Home home) {
        Location location = home.getLocation();
        if (location == null) {
            MessageUtils.sendMessage(player, "&cHome location is invalid!");
            return;
        }
        
        player.teleport(location);
        
        String message = plugin.getConfigManager().getMessagesConfig()
            .getString("home.teleported", "Teleported to home '{home}'!")
            .replace("{home}", home.getName());
        MessageUtils.sendMessage(player, message);
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
    
    private boolean hasPlayerMoved(Location current, Location initial) {
        return current.getBlockX() != initial.getBlockX() ||
               current.getBlockY() != initial.getBlockY() ||
               current.getBlockZ() != initial.getBlockZ();
    }
}