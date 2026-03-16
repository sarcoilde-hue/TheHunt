package io.Sriptirc_wp_1242.stupidcheck;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Stupidcheck extends JavaPlugin {
    
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private CheckTask checkTask;
    private BukkitTask taskId;
    
    @Override
    public void onEnable() {
        // 初始化管理器
        configManager = new ConfigManager(this);
        playerManager = new PlayerManager();
        
        // 注册事件监听器
        PlayerListener playerListener = new PlayerListener(this, configManager, playerManager);
        getServer().getPluginManager().registerEvents(playerListener, this);
        
        // 注册命令处理器
        CommandHandler commandHandler = new CommandHandler(this, configManager, playerManager);
        getCommand("stupidcheck").setExecutor(commandHandler);
        getCommand("stupidcheck").setTabCompleter(commandHandler);
        
        // 启动定时检查任务（每秒检查一次）
        checkTask = new CheckTask(this, configManager, playerManager);
        taskId = checkTask.runTaskTimer(this, 20L, 20L); // 20 ticks = 1秒
        
        // 为当前在线玩家初始化（插件重载时）
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(configManager.getExemptPermission())) {
                playerManager.playerJoined(player);
                getLogger().info("为在线玩家 " + player.getName() + " 初始化检查");
            }
        }
        
        getLogger().info("StupidCheck 插件已启用！");
        getLogger().info("要求消息: " + configManager.getRequiredMessage());
        getLogger().info("时间限制: " + configManager.getTimeLimit() + " 秒");
    }

    @Override
    public void onDisable() {
        // 取消定时任务
        if (taskId != null) {
            taskId.cancel();
        }
        
        // 清理数据
        playerManager.clearAll();
        
        getLogger().info("StupidCheck 插件已禁用！");
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
