package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * 游戏管理器 - 管理所有游戏实例
 */
public class GameManager {
    
    private final JavaPlugin plugin;
    private final Map<String, Game> games = new HashMap<>();
    private final Map<UUID, String> playerGames = new HashMap<>(); // 玩家UUID -> 游戏名
    private final Map<UUID, String> spectatorGames = new HashMap<>(); // 观战者UUID -> 游戏名
    
    // 大厅相关
    private Region lobbyRegion;
    private Location lobbySpawn;
    
    public GameManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载所有游戏
     */
    public void loadGames() {
        File gamesFolder = new File(plugin.getDataFolder(), "games");
        if (!gamesFolder.exists()) {
            gamesFolder.mkdirs();
            return;
        }
        
        File[] gameFolders = gamesFolder.listFiles(File::isDirectory);
        if (gameFolders == null) return;
        
        for (File gameFolder : gameFolders) {
            String gameName = gameFolder.getName();
            try {
                Game game = loadGame(gameName);
                if (game != null) {
                    games.put(gameName, game);
                    plugin.getLogger().info("已加载游戏: " + gameName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载游戏失败: " + gameName, e);
            }
        }
    }
    
    /**
     * 保存所有游戏
     */
    public void saveGames() {
        for (Game game : games.values()) {
            saveGame(game);
        }
    }
    
    /**
     * 加载单个游戏
     */
    private Game loadGame(String gameName) {
        File gameFolder = new File(plugin.getDataFolder(), "games/" + gameName);
        if (!gameFolder.exists()) return null;
        
        File configFile = new File(gameFolder, "config.yml");
        File classesFile = new File(gameFolder, "classes.yml");
        File suppliesFile = new File(gameFolder, "supplies.yml");
        
        if (!configFile.exists()) return null;
        
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            YamlConfiguration classes = classesFile.exists() ? 
                YamlConfiguration.loadConfiguration(classesFile) : new YamlConfiguration();
            YamlConfiguration supplies = suppliesFile.exists() ? 
                YamlConfiguration.loadConfiguration(suppliesFile) : new YamlConfiguration();
            
            return new Game(gameName, config, classes, supplies);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "加载游戏配置失败: " + gameName, e);
            return null;
        }
    }
    
    /**
     * 保存单个游戏
     */
    private void saveGame(Game game) {
        File gameFolder = new File(plugin.getDataFolder(), "games/" + game.getName());
        if (!gameFolder.exists()) {
            gameFolder.mkdirs();
        }
        
        try {
            game.getConfig().save(new File(gameFolder, "config.yml"));
            game.getClassesConfig().save(new File(gameFolder, "classes.yml"));
            game.getSuppliesConfig().save(new File(gameFolder, "supplies.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "保存游戏失败: " + game.getName(), e);
        }
    }
    
    /**
     * 创建新游戏
     */
    public boolean createGame(String name, int killerCount, int survivorCount, int maxPlayers) {
        if (games.containsKey(name)) {
            return false;
        }
        
        if (maxPlayers < 2) {
            return false;
        }
        
        if (killerCount >= survivorCount) {
            return false;
        }
        
        // 创建游戏配置
        YamlConfiguration config = new YamlConfiguration();
        config.set("name", name);
        config.set("killer-count", killerCount);
        config.set("survivor-count", survivorCount);
        config.set("max-players", maxPlayers);
        config.set("enabled", false);
        config.set("arena", null);
        config.set("survivor-spawns", new ArrayList<String>());
        config.set("killer-spawns", new ArrayList<String>());
        config.set("killer-waiting", null);
        config.set("supply-points", new ArrayList<String>());
        
        Game game = new Game(name, config, new YamlConfiguration(), new YamlConfiguration());
        games.put(name, game);
        saveGame(game);
        
        return true;
    }
    
    /**
     * 删除游戏
     */
    public boolean deleteGame(String name) {
        Game game = games.remove(name);
        if (game == null) return false;
        
        // 踢出所有玩家
        for (UUID playerId : new HashSet<>(playerGames.keySet())) {
            if (playerGames.get(playerId).equals(name)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    leaveGame(player);
                }
            }
        }
        
        // 删除文件
        File gameFolder = new File(plugin.getDataFolder(), "games/" + name);
        if (gameFolder.exists()) {
            deleteFolder(gameFolder);
        }
        
        return true;
    }
    
    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }
    
    /**
     * 加入游戏
     */
    public boolean joinGame(Player player, String gameName) {
        Game game = games.get(gameName);
        if (game == null) return false;
        
        if (playerGames.containsKey(player.getUniqueId())) {
            return false; // 已经在其他游戏中
        }
        
        if (!game.canJoin()) {
            return false;
        }
        
        // 保存玩家位置
        PlayerData.saveLocation(player);
        
        // 传送到大厅
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }
        
        playerGames.put(player.getUniqueId(), gameName);
        game.addPlayer(player);
        
        return true;
    }
    
    /**
     * 离开游戏
     */
    public boolean leaveGame(Player player) {
        String gameName = playerGames.remove(player.getUniqueId());
        if (gameName == null) return false;
        
        Game game = games.get(gameName);
        if (game != null) {
            game.removePlayer(player);
        }
        
        // 恢复玩家位置
        PlayerData.restoreLocation(player);
        
        return true;
    }
    
    /**
     * 观战游戏
     */
    public boolean spectateGame(Player player, String gameName) {
        Game game = games.get(gameName);
        if (game == null) return false;
        
        if (!game.isRunning()) {
            return false;
        }
        
        spectatorGames.put(player.getUniqueId(), gameName);
        game.addSpectator(player);
        
        return true;
    }
    
    /**
     * 停止观战
     */
    public boolean stopSpectating(Player player) {
        String gameName = spectatorGames.remove(player.getUniqueId());
        if (gameName == null) return false;
        
        Game game = games.get(gameName);
        if (game != null) {
            game.removeSpectator(player);
        }
        
        PlayerData.restoreLocation(player);
        
        return true;
    }
    
    /**
     * 获取玩家所在的游戏
     */
    public Game getPlayerGame(Player player) {
        String gameName = playerGames.get(player.getUniqueId());
        return gameName != null ? games.get(gameName) : null;
    }
    
    /**
     * 获取玩家观战的游戏
     */
    public Game getSpectatorGame(Player player) {
        String gameName = spectatorGames.get(player.getUniqueId());
        return gameName != null ? games.get(gameName) : null;
    }
    
    /**
     * 获取游戏
     */
    public Game getGame(String name) {
        return games.get(name);
    }
    
    /**
     * 获取所有游戏
     */
    public Collection<Game> getAllGames() {
        return games.values();
    }
    
    /**
     * 获取游戏名称列表
     */
    public List<String> getGameNames() {
        return new ArrayList<>(games.keySet());
    }
    
    /**
     * 设置大厅区域
     */
    public void setLobbyRegion(Location pos1, Location pos2) {
        this.lobbyRegion = new Region(pos1, pos2);
    }
    
    /**
     * 设置大厅出生点
     */
    public void setLobbySpawn(Location location) {
        this.lobbySpawn = location;
    }
    
    /**
     * 获取大厅区域
     */
    public Region getLobbyRegion() {
        return lobbyRegion;
    }
    
    /**
     * 获取大厅出生点
     */
    public Location getLobbySpawn() {
        return lobbySpawn;
    }
    
    /**
     * 检查位置是否在大厅内
     */
    public boolean isInLobby(Location location) {
        return lobbyRegion != null && lobbyRegion.contains(location);
    }
    
    /**
     * 保存大厅配置
     */
    public void saveLobbyConfig() {
        FileConfiguration config = plugin.getConfig();
        
        if (lobbyRegion != null) {
            config.set("lobby.region.pos1", locationToString(lobbyRegion.getPos1()));
            config.set("lobby.region.pos2", locationToString(lobbyRegion.getPos2()));
        }
        
        if (lobbySpawn != null) {
            config.set("lobby.spawn", locationToString(lobbySpawn));
        }
        
        plugin.saveConfig();
    }
    
    /**
     * 加载大厅配置
     */
    public void loadLobbyConfig() {
        FileConfiguration config = plugin.getConfig();
        
        if (config.contains("lobby.region.pos1") && config.contains("lobby.region.pos2")) {
            Location pos1 = stringToLocation(config.getString("lobby.region.pos1"));
            Location pos2 = stringToLocation(config.getString("lobby.region.pos2"));
            if (pos1 != null && pos2 != null) {
                lobbyRegion = new Region(pos1, pos2);
            }
        }
        
        if (config.contains("lobby.spawn")) {
            lobbySpawn = stringToLocation(config.getString("lobby.spawn"));
        }
    }
    
    /**
     * 位置转字符串
     */
    private String locationToString(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + "," + 
               location.getX() + "," + 
               location.getY() + "," + 
               location.getZ() + "," + 
               location.getYaw() + "," + 
               location.getPitch();
    }
    
    /**
     * 字符串转位置
     */
    private Location stringToLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        
        String[] parts = str.split(",");
        if (parts.length != 6) return null;
        
        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 区域类
     */
    public static class Region {
        private final Location pos1;
        private final Location pos2;
        
        public Region(Location pos1, Location pos2) {
            if (!pos1.getWorld().equals(pos2.getWorld())) {
                throw new IllegalArgumentException("两个位置必须在同一个世界");
            }
            
            this.pos1 = new Location(
                pos1.getWorld(),
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
            );
            
            this.pos2 = new Location(
                pos2.getWorld(),
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ())
            );
        }
        
        public boolean contains(Location location) {
            if (!location.getWorld().equals(pos1.getWorld())) {
                return false;
            }
            
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            
            return x >= pos1.getX() && x <= pos2.getX() &&
                   y >= pos1.getY() && y <= pos2.getY() &&
                   z >= pos1.getZ() && z <= pos2.getZ();
        }
        
        public Location getPos1() {
            return pos1.clone();
        }
        
        public Location getPos2() {
            return pos2.clone();
        }
    }
}