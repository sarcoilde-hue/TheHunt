package io.Sriptirc_wp_1242.stupidcheck;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckTask extends BukkitRunnable {
    private final Stupidcheck plugin;
    private final ConfigManager configManager;
    private final PlayerManager playerManager;
    
    public CheckTask(Stupidcheck plugin, ConfigManager configManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerManager = playerManager;
    }
    
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // 检查是否有豁免权限
            if (player.hasPermission(configManager.getExemptPermission())) {
                continue;
            }
            
            // 检查玩家是否正在计时且未通过
            if (playerManager.isPlayerTiming(player)) {
                int elapsedSeconds = playerManager.getRemainingTime(player);
                int timeLimit = configManager.getTimeLimit();
                
                // 计算剩余时间
                int remainingSeconds = timeLimit - elapsedSeconds;
                
                // 如果超时，杀死玩家
                if (remainingSeconds <= 0) {
                    killPlayer(player);
                    continue;
                }
                
                // 发送警告消息（每5秒一次，或者最后5秒每秒一次）
                if (remainingSeconds <= 5 || remainingSeconds % 5 == 0) {
                    String warningMsg = configManager.getWarningMessage()
                            .replace("{time}", String.valueOf(remainingSeconds))
                            .replace("{player}", player.getName());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', warningMsg));
                }
            }
        }
    }
    
    private void killPlayer(Player player) {
        // 设置玩家已通过（避免重复处理）
        playerManager.playerPassed(player);
        
        // 发送死亡消息给玩家
        String deathMsg = ChatColor.translateAlternateColorCodes('&', configManager.getDeathMessage());
        player.sendMessage(deathMsg);
        
        // 杀死玩家
        player.setHealth(0);
        
        // 广播死亡消息
        if (configManager.isBroadcastDeath()) {
            String broadcastMsg = configManager.getBroadcastMessage()
                    .replace("{player}", player.getName());
            broadcastMsg = ChatColor.translateAlternateColorCodes('&', broadcastMsg);
            Bukkit.broadcastMessage(broadcastMsg);
        }
        
        plugin.getLogger().info("玩家 " + player.getName() + " 因未及时发送消息而被处决");
    }
}