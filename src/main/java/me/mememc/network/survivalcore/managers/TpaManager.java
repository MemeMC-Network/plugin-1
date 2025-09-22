package me.mememc.network.survivalcore.managers;

import me.mememc.network.survivalcore.SurvivalCore;
import me.mememc.network.survivalcore.models.TpaRequest;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages teleport accept (TPA) requests between players
 */
public class TpaManager {
    
    private final SurvivalCore plugin;
    private final Map<UUID, TpaRequest> incomingRequests = new HashMap<>();
    private final Map<UUID, TpaRequest> outgoingRequests = new HashMap<>();
    
    public TpaManager(SurvivalCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Send a TPA request
     */
    public boolean sendRequest(Player requester, Player target, TpaRequest.TpaType type) {
        // Check if requester already has an outgoing request
        if (hasOutgoingRequest(requester)) {
            return false;
        }
        
        // Check if target already has an incoming request from this player
        TpaRequest existingRequest = incomingRequests.get(target.getUniqueId());
        if (existingRequest != null && existingRequest.getRequester().equals(requester)) {
            return false;
        }
        
        int timeout = plugin.getConfigManager().getTpaTimeout();
        TpaRequest request = new TpaRequest(requester, target, type, timeout);
        
        // Remove any existing requests
        removeRequest(requester, target);
        
        // Add new request
        outgoingRequests.put(requester.getUniqueId(), request);
        incomingRequests.put(target.getUniqueId(), request);
        
        return true;
    }
    
    /**
     * Accept a TPA request
     */
    public TpaRequest acceptRequest(Player target) {
        TpaRequest request = incomingRequests.get(target.getUniqueId());
        if (request == null || !request.isValid()) {
            if (request != null) {
                removeRequest(request.getRequester(), target);
            }
            return null;
        }
        
        removeRequest(request.getRequester(), target);
        return request;
    }
    
    /**
     * Deny a TPA request
     */
    public TpaRequest denyRequest(Player target) {
        TpaRequest request = incomingRequests.get(target.getUniqueId());
        if (request == null) {
            return null;
        }
        
        removeRequest(request.getRequester(), target);
        return request;
    }
    
    /**
     * Get incoming request for a player
     */
    public TpaRequest getIncomingRequest(Player player) {
        TpaRequest request = incomingRequests.get(player.getUniqueId());
        if (request != null && !request.isValid()) {
            removeRequest(request.getRequester(), player);
            return null;
        }
        return request;
    }
    
    /**
     * Get outgoing request for a player
     */
    public TpaRequest getOutgoingRequest(Player player) {
        TpaRequest request = outgoingRequests.get(player.getUniqueId());
        if (request != null && !request.isValid()) {
            removeRequest(player, request.getTarget());
            return null;
        }
        return request;
    }
    
    /**
     * Check if player has an incoming request
     */
    public boolean hasIncomingRequest(Player player) {
        return getIncomingRequest(player) != null;
    }
    
    /**
     * Check if player has an outgoing request
     */
    public boolean hasOutgoingRequest(Player player) {
        return getOutgoingRequest(player) != null;
    }
    
    /**
     * Remove a request between two players
     */
    public void removeRequest(Player requester, Player target) {
        outgoingRequests.remove(requester.getUniqueId());
        incomingRequests.remove(target.getUniqueId());
    }
    
    /**
     * Remove all requests for a player (when they disconnect)
     */
    public void removeAllRequests(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Remove outgoing request
        outgoingRequests.remove(uuid);
        
        // Remove incoming request
        TpaRequest incomingRequest = incomingRequests.remove(uuid);
        if (incomingRequest != null) {
            outgoingRequests.remove(incomingRequest.getRequester().getUniqueId());
        }
    }
    
    /**
     * Clean up expired requests
     */
    public void cleanupExpiredRequests() {
        incomingRequests.entrySet().removeIf(entry -> {
            TpaRequest request = entry.getValue();
            if (request.isExpired()) {
                outgoingRequests.remove(request.getRequester().getUniqueId());
                return true;
            }
            return false;
        });
    }
}