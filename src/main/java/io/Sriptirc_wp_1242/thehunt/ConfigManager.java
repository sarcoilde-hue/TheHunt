package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    // 默认配置值
    private int gameTime = 600; // 默认游戏时间（秒）
    private int outlineInterval = 120; // 轮廓显示间隔（秒）
    private int outlineDuration = 10; // 轮廓显示持续时间（秒）
    private int minPlayers = 4; // 最小玩家数
    private int maxPlayers = 10; // 最大玩家数
    private int killerThreshold = 8; // 超过此人数时使用2名杀手
    
    private String lobbyWorld = "world";
    private String gameWorld = "world";
    
    private List<String> killerSpawnPoints = new ArrayList<>();
    private List<String> survivorSpawnPoints = new ArrayList<>();
    private List<String> resourcePoints = new ArrayList<>();
    
    private Map<String, Object> customItems = new HashMap<>();
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // 设置配置版本
        config.set("ScriptIrc-config-version", 1);
        
        // 读取配置值
        gameTime = config.getInt("game.time", 600);
        outlineInterval = config.getInt("outline.interval", 120);
        outlineDuration = config.getInt("outline.duration", 10);
        minPlayers = config.getInt("players.min", 4);
        maxPlayers = config.getInt("players.max", 10);
        killerThreshold = config.getInt("players.killer-threshold", 8);
        
        lobbyWorld = config.getString("worlds.lobby", "world");
        gameWorld = config.getString("worlds.game", "world");
        
        killerSpawnPoints = config.getStringList("spawns.killer");
        survivorSpawnPoints = config.getStringList("spawns.survivor");
        resourcePoints = config.getStringList("spawns.resource");
        
        // 读取自定义物品
        if (config.contains("items")) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                customItems.put(key, config.get("items." + key));
            }
        }
        
        saveConfig();
    }
    
    public void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件时出错: " + e.getMessage());
        }
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadConfig();
    }
    
    // Getter 方法
    public int getGameTime() { return gameTime; }
    public int getOutlineInterval() { return outlineInterval; }
    public int getOutlineDuration() { return outlineDuration; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getKillerThreshold() { return killerThreshold; }
    public String getLobbyWorld() { return lobbyWorld; }
    public String getGameWorld() { return gameWorld; }
    public List<String> getKillerSpawnPoints() { return killerSpawnPoints; }
    public List<String> getSurvivorSpawnPoints() { return survivorSpawnPoints; }
    public List<String> getResourcePoints() { return resourcePoints; }
    public Map<String, Object> getCustomItems() { return customItems; }
    
    // 添加出生点的方法
    public void addKillerSpawnPoint(String location) {
        killerSpawnPoints.add(location);
        config.set("spawns.killer", killerSpawnPoints);
        saveConfig();
    }
    
    public void addSurvivorSpawnPoint(String location) {
        survivorSpawnPoints.add(location);
        config.set("spawns.survivor", survivorSpawnPoints);
        saveConfig();
    }
    
    public void addResourcePoint(String location) {
        resourcePoints.add(location);
        config.set("spawns.resource", resourcePoints);
        saveConfig();
    }
    
    // 移除出生点的方法
    public void removeKillerSpawnPoint(String location) {
        killerSpawnPoints.remove(location);
        config.set("spawns.killer", killerSpawnPoints);
        saveConfig();
    }
    
    public void removeSurvivorSpawnPoint(String location) {
        survivorSpawnPoints.remove(location);
        config.set("spawns.survivor", survivorSpawnPoints);
        saveConfig();
    }
    
    public void removeResourcePoint(String location) {
        resourcePoints.remove(location);
        config.set("spawns.resource", resourcePoints);
        saveConfig();
    }
}