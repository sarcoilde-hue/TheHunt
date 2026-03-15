package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, PlayerData::new);
    }
    
    public PlayerRole getPlayerRole(UUID playerId) {
        PlayerData data = getPlayerData(playerId);
        return data.getRole();
    }
    
    public void setPlayerRole(UUID playerId, PlayerRole role) {
        PlayerData data = getPlayerData(playerId);
        data.setRole(role);
    }
    
    public boolean isPlayerInGame(UUID playerId) {
        PlayerRole role = getPlayerRole(playerId);
        return role == PlayerRole.LOBBY || role == PlayerRole.KILLER || 
               role == PlayerRole.SURVIVOR || role == PlayerRole.SPECTATOR;
    }
    
    public boolean isPlayerAlive(UUID playerId) {
        PlayerData data = getPlayerData(playerId);
        return data != null && data.isAlive();
    }
    
    public void removePlayer(UUID playerId) {
        playerDataMap.remove(playerId);
    }
    
    public void clearAllPlayers() {
        playerDataMap.clear();
    }
    
    // 获取在线玩家的数据
    public Map<UUID, PlayerData> getOnlinePlayersData() {
        Map<UUID, PlayerData> onlineData = new HashMap<>();
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                onlineData.put(entry.getKey(), entry.getValue());
            }
        }
        return onlineData;
    }
    
    // 获取特定角色的玩家
    public Map<UUID, PlayerData> getPlayersByRole(PlayerRole role) {
        Map<UUID, PlayerData> result = new HashMap<>();
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            if (entry.getValue().getRole() == role) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}