package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Thehunt extends JavaPlugin {
    
    private static Thehunt instance;
    private GameManager gameManager;
    private CommandHandler commandHandler;
    private EventListener eventListener;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化管理器
        gameManager = new GameManager(this);
        commandHandler = new CommandHandler(this, gameManager);
        eventListener = new EventListener(this, gameManager, commandHandler);
        
        // 加载数据
        gameManager.loadLobbyConfig();
        gameManager.loadGames();
        
        // 注册命令
        getCommand("hunt").setExecutor(commandHandler);
        getCommand("hunt").setTabCompleter(commandHandler);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(eventListener, this);
        
        // 启动游戏更新任务
        startUpdateTask();
        
        getLogger().info("The Hunt 插件已启用！");
        getLogger().info("游戏数量: " + gameManager.getAllGames().size());
    }
    
    @Override
    public void onDisable() {
        // 保存数据
        if (gameManager != null) {
            gameManager.saveGames();
        }
        
        // 清理玩家数据
        PlayerData.cleanup();
        
        getLogger().info("The Hunt 插件已禁用。");
    }
    
    /**
     * 启动游戏更新任务
     */
    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                // 更新所有游戏
                for (Game game : gameManager.getAllGames()) {
                    if (game.isRunning()) {
                        game.update();
                    }
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "游戏更新任务出错", e);
            }
        }, 20L, 20L); // 每秒更新一次
    }
    
    /**
     * 获取插件实例
     */
    public static Thehunt getInstance() {
        return instance;
    }
    
    /**
     * 获取游戏管理器
     */
    public GameManager getGameManager() {
        return gameManager;
    }
    
    /**
     * 获取命令处理器
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
}
