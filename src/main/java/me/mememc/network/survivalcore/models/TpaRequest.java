package me.mememc.network.survivalcore.models;

import org.bukkit.entity.Player;

/**
 * Represents a teleport request between players
 */
public class TpaRequest {
    
    private final Player requester;
    private final Player target;
    private final TpaType type;
    private final long createdAt;
    private final long expiresAt;
    
    public enum TpaType {
        TPA,        // Requester wants to teleport to target
        TPA_HERE    // Requester wants target to teleport to them
    }
    
    public TpaRequest(Player requester, Player target, TpaType type, int timeoutSeconds) {
        this.requester = requester;
        this.target = target;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + (timeoutSeconds * 1000L);
    }
    
    public Player getRequester() {
        return requester;
    }
    
    public Player getTarget() {
        return target;
    }
    
    public TpaType getType() {
        return type;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }
    
    public int getRemainingTime() {
        long remaining = expiresAt - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }
    
    public boolean isValid() {
        return !isExpired() && 
               requester.isOnline() && 
               target.isOnline();
    }
    
    @Override
    public String toString() {
        return String.format("TpaRequest{requester='%s', target='%s', type=%s, remaining=%ds}", 
                           requester.getName(), target.getName(), type, getRemainingTime());
    }
}