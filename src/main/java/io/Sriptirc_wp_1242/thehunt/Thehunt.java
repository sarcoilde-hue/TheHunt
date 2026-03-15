package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.plugin.java.JavaPlugin;

public final class Thehunt extends JavaPlugin {
    
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private MapManager mapManager;
    private ItemManager itemManager;
    private GameManager gameManager;
    private GameListener gameListener;
    private CommandHandler commandHandler;
    
    @Override
    public void onEnable() {
        // 初始化管理器
        configManager = new ConfigManager(this);
        playerManager = new PlayerManager();
        mapManager = new MapManager(this, configManager);
        itemManager = new ItemManager(this);
        gameManager = new GameManager(this, configManager, playerManager, mapManager, itemManager);
        
        // 初始化监听器
        gameListener = new GameListener(this, gameManager, playerManager);
        getServer().getPluginManager().registerEvents(gameListener, this);
        
        // 初始化命令处理器
        commandHandler = new CommandHandler(this, gameManager, playerManager, mapManager, configManager);
        getCommand("hunt").setExecutor(commandHandler);
        getCommand("hunt").setTabCompleter(commandHandler);
        getCommand("huntadmin").setExecutor(commandHandler);
        getCommand("huntadmin").setTabCompleter(commandHandler);
        
        // 保存默认配置文件
        saveDefaultConfig();
        
        getLogger().info("The Hunt 插件已启用！");
        getLogger().info("游戏时间: " + configManager.getGameTime() + " 秒");
        getLogger().info("玩家范围: " + configManager.getMinPlayers() + " - " + configManager.getMaxPlayers());
    }

    @Override
    public void onDisable() {
        // 停止所有游戏
        if (gameManager != null) {
            // 这里可以添加游戏强制停止的逻辑
        }
        
        // 恢复所有玩家状态
        if (playerManager != null) {
            playerManager.clearAllPlayers();
        }
        
        getLogger().info("The Hunt 插件已禁用！");
    }
    
    // Getter 方法（用于其他类访问）
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public MapManager getMapManager() { return mapManager; }
    public ItemManager getItemManager() { return itemManager; }
    public GameManager getGameManager() { return gameManager; }
}
