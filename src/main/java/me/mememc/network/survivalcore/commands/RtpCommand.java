package me.mememc.network.survivalcore.commands;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Handles RTP (Random Teleport) command functionality
 */
public class RtpCommand implements CommandExecutor {
    
    private final SurvivalCore plugin;
    private final Random random = new Random();
    
    // Unsafe materials for teleportation
    private final List<Material> unsafeMaterials = Arrays.asList(
        Material.LAVA, Material.FIRE, Material.MAGMA_BLOCK, 
        Material.SWEET_BERRY_BUSH, Material.WITHER_ROSE, Material.CACTUS,
        Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.SOUL_FIRE
    );
    
    public RtpCommand(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isRtpEnabled()) {
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
        
        // Check permissions
        if (!player.hasPermission("survivalcore.rtp.use")) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("general.no-permission", "You don't have permission to use this command!"));
            return true;
        }
        
        // Check if world allows RTP
        World world = player.getWorld();
        List<String> allowedWorlds = plugin.getConfigManager().getConfig().getStringList("rtp.allowed-worlds");
        if (!allowedWorlds.contains(world.getName())) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                .getString("rtp.world-not-allowed", "Random teleport is not allowed in this world!"));
            return true;
        }
        
        // Check cooldown (unless player has bypass permission)
        if (!player.hasPermission("survivalcore.bypass.cooldown")) {
            if (plugin.getCooldownManager().hasCooldown(player, "rtp")) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player, "rtp");
                String timeFormat = plugin.getCooldownManager().formatTime(remaining);
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("general.cooldown", "You must wait {time} before using this command again!")
                    .replace("{time}", timeFormat);
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        // Handle arguments for teleporting other players (admin feature)
        Player targetPlayer = player;
        if (args.length > 0) {
            if (!player.hasPermission("survivalcore.rtp.others")) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessagesConfig()
                    .getString("general.no-permission", "You don't have permission to use this command!"));
                return true;
            }
            
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                String message = plugin.getConfigManager().getMessagesConfig()
                    .getString("general.invalid-player", "Player '{player}' not found or is offline!")
                    .replace("{player}", args[0]);
                MessageUtils.sendMessage(player, message);
                return true;
            }
        }
        
        // Start RTP process
        performRandomTeleport(player, targetPlayer);
        return true;
    }
    
    private void performRandomTeleport(Player sender, Player target) {
        World world = target.getWorld();
        
        // Set cooldown for the command sender
        if (!sender.hasPermission("survivalcore.bypass.cooldown")) {
            int cooldown = plugin.getConfigManager().getRtpCooldown();
            plugin.getCooldownManager().setCooldown(sender, "rtp", cooldown);
        }
        
        // Find safe location asynchronously to avoid blocking main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safeLocation = findSafeLocation(world);
            
            // Teleport on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (safeLocation != null) {
                    target.teleport(safeLocation);
                    
                    String message = plugin.getConfigManager().getMessagesConfig()
                        .getString("rtp.success", "Teleported to a random location! ({x}, {y}, {z})")
                        .replace("{x}", String.valueOf((int) safeLocation.getX()))
                        .replace("{y}", String.valueOf((int) safeLocation.getY()))
                        .replace("{z}", String.valueOf((int) safeLocation.getZ()));
                    
                    MessageUtils.sendMessage(target, message);
                    
                    // If sender is different from target, notify sender
                    if (!sender.equals(target)) {
                        String adminMessage = "Teleported " + target.getName() + " to a random location!";
                        MessageUtils.sendMessage(sender, adminMessage);
                    }
                    
                    // Play sound effect
                    target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    
                } else {
                    String message = plugin.getConfigManager().getMessagesConfig()
                        .getString("rtp.no-safe-location", "Could not find a safe location to teleport you to. Try again!");
                    MessageUtils.sendMessage(sender, message);
                }
            });
        });
    }
    
    private Location findSafeLocation(World world) {
        int maxDistance = plugin.getConfigManager().getRtpMaxDistance();
        int minDistance = plugin.getConfigManager().getRtpMinDistance();
        int maxAttempts = plugin.getConfig().getInt("rtp.max-attempts", 50);
        
        Location spawnLocation = world.getSpawnLocation();
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Generate random coordinates
            int x = random.nextInt(maxDistance * 2) - maxDistance;
            int z = random.nextInt(maxDistance * 2) - maxDistance;
            
            // Ensure minimum distance from spawn
            if (Math.abs(x) < minDistance && Math.abs(z) < minDistance) {
                continue;
            }
            
            // Get highest block at coordinates
            int y = world.getHighestBlockYAt(spawnLocation.getBlockX() + x, spawnLocation.getBlockZ() + z);
            Location testLocation = new Location(world, spawnLocation.getX() + x, y + 1, spawnLocation.getZ() + z);
            
            if (isSafeLocation(testLocation)) {
                return testLocation;
            }
        }
        
        return null; // No safe location found
    }
    
    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        
        Block groundBlock = world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
        Block feetBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block headBlock = world.getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());
        
        // Check if ground is solid
        if (!groundBlock.getType().isSolid()) {
            return false;
        }
        
        // Check if feet and head space are clear
        if (feetBlock.getType().isSolid() || headBlock.getType().isSolid()) {
            return false;
        }
        
        // Check for unsafe materials
        if (unsafeMaterials.contains(groundBlock.getType()) ||
            unsafeMaterials.contains(feetBlock.getType()) ||
            unsafeMaterials.contains(headBlock.getType())) {
            return false;
        }
        
        // Don't teleport into water/lava unless it's shallow
        if (feetBlock.getType() == Material.WATER || feetBlock.getType() == Material.LAVA) {
            return false;
        }
        
        // Check if location is too high or too low
        int y = location.getBlockY();
        if (y < 5 || y > world.getMaxHeight() - 10) {
            return false;
        }
        
        return true;
    }
}