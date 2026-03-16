package io.Sriptirc_wp_1242.stupidcheck;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<UUID, Long> playerJoinTimes = new HashMap<>();
    private final Map<UUID, Boolean> playerPassed = new HashMap<>();
    
    public void playerJoined(Player player) {
        UUID uuid = player.getUniqueId();
        playerJoinTimes.put(uuid, System.currentTimeMillis());
        playerPassed.put(uuid, false);
    }
    
    public void playerPassed(Player player) {
        UUID uuid = player.getUniqueId();
        playerPassed.put(uuid, true);
        playerJoinTimes.remove(uuid); // 不再需要计时
    }
    
    public void playerLeft(Player player) {
        UUID uuid = player.getUniqueId();
        playerJoinTimes.remove(uuid);
        playerPassed.remove(uuid);
    }
    
    public boolean hasPlayerPassed(Player player) {
        return playerPassed.getOrDefault(player.getUniqueId(), false);
    }
    
    public long getJoinTime(Player player) {
        return playerJoinTimes.getOrDefault(player.getUniqueId(), 0L);
    }
    
    public boolean isPlayerTiming(Player player) {
        UUID uuid = player.getUniqueId();
        return playerJoinTimes.containsKey(uuid) && !playerPassed.getOrDefault(uuid, false);
    }
    
    public int getRemainingTime(Player player) {
        if (!isPlayerTiming(player)) {
            return 0;
        }
        
        long joinTime = getJoinTime(player);
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - joinTime) / 1000;
        
        return Math.max(0, (int) elapsedSeconds);
    }
    
    public void clearAll() {
        playerJoinTimes.clear();
        playerPassed.clear();
    }
}