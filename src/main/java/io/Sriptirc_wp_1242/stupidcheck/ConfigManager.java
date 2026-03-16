package io.Sriptirc_wp_1242.stupidcheck;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    // 配置项
    private String requiredMessage;
    private int timeLimit;
    private boolean broadcastDeath;
    private String broadcastMessage;
    private String deathMessage;
    private String successMessage;
    private String warningMessage;
    private String alreadyPassedMessage;
    private String exemptPermission;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // 设置配置版本
        config.addDefault("ScriptIrc-config-version", 1);
        
        // 设置默认值
        config.addDefault("required-message", "我是傻逼");
        config.addDefault("time-limit-seconds", 10);
        config.addDefault("broadcast-death", true);
        config.addDefault("broadcast-message", "&c玩家 &6{player} &c因为10秒内没有发送'我是傻逼'而被处决了！");
        config.addDefault("death-message", "&c你因为10秒内没有发送'我是傻逼'而被处决了！");
        config.addDefault("success-message", "&a验证通过！你可以继续游戏了。");
        config.addDefault("warning-message", "&e你还有 &6{time} &e秒时间发送'我是傻逼'！");
        config.addDefault("already-passed-message", "&a你已经通过验证了，无需重复发送。");
        config.addDefault("exempt-permission", "stupidcheck.bypass");
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
        
        // 加载配置值
        requiredMessage = config.getString("required-message", "我是傻逼");
        timeLimit = config.getInt("time-limit-seconds", 10);
        broadcastDeath = config.getBoolean("broadcast-death", true);
        broadcastMessage = config.getString("broadcast-message", "&c玩家 &6{player} &c因为10秒内没有发送'我是傻逼'而被处决了！");
        deathMessage = config.getString("death-message", "&c你因为10秒内没有发送'我是傻逼'而被处决了！");
        successMessage = config.getString("success-message", "&a验证通过！你可以继续游戏了。");
        warningMessage = config.getString("warning-message", "&e你还有 &6{time} &e秒时间发送'我是傻逼'！");
        alreadyPassedMessage = config.getString("already-passed-message", "&a你已经通过验证了，无需重复发送。");
        exemptPermission = config.getString("exempt-permission", "stupidcheck.bypass");
    }
    
    public void reload() {
        loadConfig();
    }
    
    // Getter方法
    public String getRequiredMessage() {
        return requiredMessage;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public boolean isBroadcastDeath() {
        return broadcastDeath;
    }
    
    public String getBroadcastMessage() {
        return broadcastMessage;
    }
    
    public String getDeathMessage() {
        return deathMessage;
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public String getWarningMessage() {
        return warningMessage;
    }
    
    public String getAlreadyPassedMessage() {
        return alreadyPassedMessage;
    }
    
    public String getExemptPermission() {
        return exemptPermission;
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}