package me.mememc.network.survivalcore.models;

import java.util.UUID;

/**
 * Represents player statistics data
 */
public class PlayerStats {
    
    private final UUID playerUuid;
    private final String playerName;
    private long timePlayed;
    private int blocksPlaced;
    private int blocksBroken;
    private int mobsKilled;
    private int deaths;
    private int playersKilled;
    private double distanceTraveled;
    private int itemsCrafted;
    private int fishCaught;
    private long joinDate;
    private long lastSeen;
    
    public PlayerStats(UUID playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.joinDate = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
    }
    
    public PlayerStats(UUID playerUuid, String playerName, long timePlayed, int blocksPlaced, 
                      int blocksBroken, int mobsKilled, int deaths, int playersKilled, 
                      double distanceTraveled, int itemsCrafted, int fishCaught, 
                      long joinDate, long lastSeen) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.timePlayed = timePlayed;
        this.blocksPlaced = blocksPlaced;
        this.blocksBroken = blocksBroken;
        this.mobsKilled = mobsKilled;
        this.deaths = deaths;
        this.playersKilled = playersKilled;
        this.distanceTraveled = distanceTraveled;
        this.itemsCrafted = itemsCrafted;
        this.fishCaught = fishCaught;
        this.joinDate = joinDate;
        this.lastSeen = lastSeen;
    }
    
    // Getters
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public long getTimePlayed() { return timePlayed; }
    public int getBlocksPlaced() { return blocksPlaced; }
    public int getBlocksBroken() { return blocksBroken; }
    public int getMobsKilled() { return mobsKilled; }
    public int getDeaths() { return deaths; }
    public int getPlayersKilled() { return playersKilled; }
    public double getDistanceTraveled() { return distanceTraveled; }
    public int getItemsCrafted() { return itemsCrafted; }
    public int getFishCaught() { return fishCaught; }
    public long getJoinDate() { return joinDate; }
    public long getLastSeen() { return lastSeen; }
    
    // Setters
    public void setTimePlayed(long timePlayed) { this.timePlayed = timePlayed; }
    public void setBlocksPlaced(int blocksPlaced) { this.blocksPlaced = blocksPlaced; }
    public void setBlocksBroken(int blocksBroken) { this.blocksBroken = blocksBroken; }
    public void setMobsKilled(int mobsKilled) { this.mobsKilled = mobsKilled; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void setPlayersKilled(int playersKilled) { this.playersKilled = playersKilled; }
    public void setDistanceTraveled(double distanceTraveled) { this.distanceTraveled = distanceTraveled; }
    public void setItemsCrafted(int itemsCrafted) { this.itemsCrafted = itemsCrafted; }
    public void setFishCaught(int fishCaught) { this.fishCaught = fishCaught; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    
    // Increment methods
    public void addTimePlayed(long time) { this.timePlayed += time; }
    public void incrementBlocksPlaced() { this.blocksPlaced++; }
    public void incrementBlocksBroken() { this.blocksBroken++; }
    public void incrementMobsKilled() { this.mobsKilled++; }
    public void incrementDeaths() { this.deaths++; }
    public void incrementPlayersKilled() { this.playersKilled++; }
    public void addDistanceTraveled(double distance) { this.distanceTraveled += distance; }
    public void incrementItemsCrafted() { this.itemsCrafted++; }
    public void incrementFishCaught() { this.fishCaught++; }
    
    public double getKDRatio() {
        return deaths == 0 ? playersKilled : (double) playersKilled / deaths;
    }
    
    public String formatTimePlayed() {
        long seconds = timePlayed / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}