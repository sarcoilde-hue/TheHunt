package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapManager {
    
    private final Thehunt plugin;
    private final ConfigManager configManager;
    private final Random random = new Random();
    
    private List<Location> lobbySpawns = new ArrayList<>();
    private List<Location> killerSpawns = new ArrayList<>();
    private List<Location> survivorSpawns = new ArrayList<>();
    private List<Location> resourcePoints = new ArrayList<>();
    
    public MapManager(Thehunt plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadSpawnPoints();
    }
    
    private void loadSpawnPoints() {
        // 从配置加载大厅出生点
        for (String locStr : configManager.getKillerSpawnPoints()) {
            Location loc = parseLocation(locStr);
            if (loc != null) {
                killerSpawns.add(loc);
            }
        }
        
        // 从配置加载求生者出生点
        for (String locStr : configManager.getSurvivorSpawnPoints()) {
            Location loc = parseLocation(locStr);
            if (loc != null) {
                survivorSpawns.add(loc);
            }
        }
        
        // 从配置加载资源点
        for (String locStr : configManager.getResourcePoints()) {
            Location loc = parseLocation(locStr);
            if (loc != null) {
                resourcePoints.add(loc);
            }
        }
        
        // 如果没有出生点，创建一些默认的
        if (killerSpawns.isEmpty()) {
            createDefaultSpawns();
        }
    }
    
    private Location parseLocation(String locationString) {
        try {
            String[] parts = locationString.split(",");
            if (parts.length >= 4) {
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) {
                    world = Bukkit.getWorlds().get(0);
                }
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
                float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;
                
                return new Location(world, x, y, z, yaw, pitch);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("无法解析位置: " + locationString);
        }
        return null;
    }
    
    private String locationToString(Location location) {
        if (location == null) return "";
        return location.getWorld().getName() + "," +
               location.getX() + "," +
               location.getY() + "," +
               location.getZ() + "," +
               location.getYaw() + "," +
               location.getPitch();
    }
    
    private void createDefaultSpawns() {
        World world = Bukkit.getWorlds().get(0);
        
        // 创建默认大厅出生点
        lobbySpawns.add(new Location(world, 0, 64, 0, 0, 0));
        
        // 创建默认杀手出生点
        killerSpawns.add(new Location(world, 10, 64, 10, 180, 0));
        killerSpawns.add(new Location(world, -10, 64, 10, 180, 0));
        
        // 创建默认求生者出生点
        survivorSpawns.add(new Location(world, 20, 64, 20, 0, 0));
        survivorSpawns.add(new Location(world, -20, 64, 20, 0, 0));
        survivorSpawns.add(new Location(world, 20, 64, -20, 0, 0));
        survivorSpawns.add(new Location(world, -20, 64, -20, 0, 0));
        
        // 创建默认资源点
        resourcePoints.add(new Location(world, 30, 64, 30));
        resourcePoints.add(new Location(world, -30, 64, 30));
        resourcePoints.add(new Location(world, 30, 64, -30));
        resourcePoints.add(new Location(world, -30, 64, -30));
        
        // 保存到配置
        saveSpawnPointsToConfig();
    }
    
    private void saveSpawnPointsToConfig() {
        // 保存杀手出生点
        List<String> killerStrings = new ArrayList<>();
        for (Location loc : killerSpawns) {
            killerStrings.add(locationToString(loc));
        }
        configManager.getKillerSpawnPoints().clear();
        configManager.getKillerSpawnPoints().addAll(killerStrings);
        
        // 保存求生者出生点
        List<String> survivorStrings = new ArrayList<>();
        for (Location loc : survivorSpawns) {
            survivorStrings.add(locationToString(loc));
        }
        configManager.getSurvivorSpawnPoints().clear();
        configManager.getSurvivorSpawnPoints().addAll(survivorStrings);
        
        // 保存资源点
        List<String> resourceStrings = new ArrayList<>();
        for (Location loc : resourcePoints) {
            resourceStrings.add(locationToString(loc));
        }
        configManager.getResourcePoints().clear();
        configManager.getResourcePoints().addAll(resourceStrings);
        
        configManager.saveConfig();
    }
    
    public Location getRandomLobbySpawn() {
        if (lobbySpawns.isEmpty()) {
            World world = Bukkit.getWorld(configManager.getLobbyWorld());
            if (world == null) world = Bukkit.getWorlds().get(0);
            return world.getSpawnLocation();
        }
        return lobbySpawns.get(random.nextInt(lobbySpawns.size()));
    }
    
    public Location getRandomKillerSpawn() {
        if (killerSpawns.isEmpty()) {
            return getRandomLobbySpawn();
        }
        return killerSpawns.get(random.nextInt(killerSpawns.size()));
    }
    
    public Location getRandomSurvivorSpawn() {
        if (survivorSpawns.isEmpty()) {
            return getRandomLobbySpawn();
        }
        return survivorSpawns.get(random.nextInt(survivorSpawns.size()));
    }
    
    public void spawnResources(ItemManager itemManager) {
        for (Location location : resourcePoints) {
            // 确保位置安全（不是空中或液体中）
            Location safeLocation = findSafeLocation(location);
            if (safeLocation != null) {
                // 随机生成资源
                spawnRandomResource(safeLocation, itemManager);
            }
        }
    }
    
    private Location findSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // 向下寻找固体方块
        for (int i = y; i > 0; i--) {
            Block block = world.getBlockAt(x, i, z);
            Block above = world.getBlockAt(x, i + 1, z);
            
            if (block.getType().isSolid() && 
                !above.getType().isSolid() && 
                !above.isLiquid()) {
                return new Location(world, x + 0.5, i + 1, z + 0.5);
            }
        }
        
        // 向上寻找固体方块
        for (int i = y; i < world.getMaxHeight(); i++) {
            Block block = world.getBlockAt(x, i, z);
            Block above = world.getBlockAt(x, i + 1, z);
            
            if (block.getType().isSolid() && 
                !above.getType().isSolid() && 
                !above.isLiquid()) {
                return new Location(world, x + 0.5, i + 1, z + 0.5);
            }
        }
        
        return null;
    }
    
    private void spawnRandomResource(Location location, ItemManager itemManager) {
        World world = location.getWorld();
        int randomType = random.nextInt(100);
        
        if (randomType < 30) { // 30% 武器
            ItemStack weapon = itemManager.createRandomWeapon();
            world.dropItem(location, weapon);
        } else if (randomType < 50) { // 20% 工具
            ItemStack tool = itemManager.createRandomTool();
            world.dropItem(location, tool);
        } else if (randomType < 70) { // 20% 药水
            ItemStack potion = itemManager.createRandomPotion();
            world.dropItem(location, potion);
        } else if (randomType < 85) { // 15% 食物
            ItemStack food = itemManager.createRandomFood();
            world.dropItem(location, food);
        } else { // 15% 特殊道具
            ItemStack special = itemManager.createRandomSpecialItem();
            world.dropItem(location, special);
        }
    }
    
    // 添加出生点的方法（用于命令）
    public boolean addKillerSpawn(Location location) {
        killerSpawns.add(location);
        configManager.addKillerSpawnPoint(locationToString(location));
        return true;
    }
    
    public boolean addSurvivorSpawn(Location location) {
        survivorSpawns.add(location);
        configManager.addSurvivorSpawnPoint(locationToString(location));
        return true;
    }
    
    public boolean addResourcePoint(Location location) {
        resourcePoints.add(location);
        configManager.addResourcePoint(locationToString(location));
        return true;
    }
    
    // 移除出生点的方法
    public boolean removeKillerSpawn(Location location) {
        for (Location loc : killerSpawns) {
            if (loc.distance(location) < 2.0) {
                killerSpawns.remove(loc);
                configManager.removeKillerSpawnPoint(locationToString(loc));
                return true;
            }
        }
        return false;
    }
    
    public boolean removeSurvivorSpawn(Location location) {
        for (Location loc : survivorSpawns) {
            if (loc.distance(location) < 2.0) {
                survivorSpawns.remove(loc);
                configManager.removeSurvivorSpawnPoint(locationToString(loc));
                return true;
            }
        }
        return false;
    }
    
    public boolean removeResourcePoint(Location location) {
        for (Location loc : resourcePoints) {
            if (loc.distance(location) < 2.0) {
                resourcePoints.remove(loc);
                configManager.removeResourcePoint(locationToString(loc));
                return true;
            }
        }
        return false;
    }
    
    // Getter 方法
    public List<Location> getLobbySpawns() { return lobbySpawns; }
    public List<Location> getKillerSpawns() { return killerSpawns; }
    public List<Location> getSurvivorSpawns() { return survivorSpawns; }
    public List<Location> getResourcePoints() { return resourcePoints; }
}