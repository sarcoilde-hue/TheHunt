package io.Sriptirc_wp_1242.stupidcheck;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final Stupidcheck plugin;
    private final ConfigManager configManager;
    private final PlayerManager playerManager;
    
    public PlayerListener(Stupidcheck plugin, ConfigManager configManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerManager = playerManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查是否有豁免权限
        if (player.hasPermission(configManager.getExemptPermission())) {
            plugin.getLogger().info("玩家 " + player.getName() + " 拥有豁免权限，跳过检查");
            return;
        }
        
        // 记录玩家加入时间
        playerManager.playerJoined(player);
        
        // 发送初始提示
        String warningMsg = configManager.getWarningMessage()
                .replace("{time}", String.valueOf(configManager.getTimeLimit()))
                .replace("{player}", player.getName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', warningMsg));
        
        plugin.getLogger().info("玩家 " + player.getName() + " 已加入，开始 " + configManager.getTimeLimit() + " 秒倒计时");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerManager.playerLeft(player);
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        
        // 检查是否有豁免权限
        if (player.hasPermission(configManager.getExemptPermission())) {
            return;
        }
        
        // 检查玩家是否正在计时
        if (!playerManager.isPlayerTiming(player)) {
            // 如果玩家已经通过验证，发送提示消息
            if (playerManager.hasPlayerPassed(player)) {
                if (message.equals(configManager.getRequiredMessage())) {
                    String alreadyPassedMsg = ChatColor.translateAlternateColorCodes('&', 
                            configManager.getAlreadyPassedMessage());
                    player.sendMessage(alreadyPassedMsg);
                    event.setCancelled(true); // 取消消息发送，避免刷屏
                }
            }
            return;
        }
        
        // 检查消息是否匹配要求
        if (message.equals(configManager.getRequiredMessage())) {
            // 标记玩家已通过
            playerManager.playerPassed(player);
            
            // 发送成功消息
            String successMsg = ChatColor.translateAlternateColorCodes('&', configManager.getSuccessMessage());
            player.sendMessage(successMsg);
            
            // 取消消息发送，避免刷屏
            event.setCancelled(true);
            
            plugin.getLogger().info("玩家 " + player.getName() + " 已通过验证");
        }
    }
}