package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 游戏实例类
 */
public class Game {
    
    public enum GameState {
        WAITING,      // 等待中（大厅）
        PREPARATION,  // 准备阶段
        HUNTING,      // 猎杀阶段
        ENDING        // 结束阶段
    }
    
    private final String name;
    private final YamlConfiguration config;
    private final YamlConfiguration classesConfig;
    private final YamlConfiguration suppliesConfig;
    
    private GameState state = GameState.WAITING;
    private final Set<UUID> players = new HashSet<>(); // 所有玩家
    private final Set<UUID> survivors = new HashSet<>(); // 求生者
    private final Set<UUID> killers = new HashSet<>(); // 杀手
    private final Set<UUID> spectators = new HashSet<>(); // 观战者
    private final Map<UUID, Integer> scores = new HashMap<>(); // 玩家得分
    private final Map<UUID, String> selectedClasses = new HashMap<>(); // 玩家选择的职业
    
    // 游戏区域
    private GameManager.Region arena;
    private final List<Location> survivorSpawns = new ArrayList<>();
    private final List<Location> killerSpawns = new ArrayList<>();
    private Location killerWaiting;
    private final List<Location> supplyPoints = new ArrayList<>();
    
    // 游戏计时器
    private int gameTime = 0;
    private int preparationTime = 0;
    private int huntingTime = 0;
    
    public Game(String name, YamlConfiguration config, YamlConfiguration classesConfig, YamlConfiguration suppliesConfig) {
        this.name = name;
        this.config = config;
        this.classesConfig = classesConfig;
        this.suppliesConfig = suppliesConfig;
        
        loadFromConfig();
    }
    
    /**
     * 从配置加载
     */
    private void loadFromConfig() {
        // 加载区域
        if (config.contains("arena.pos1") && config.contains("arena.pos2")) {
            Location pos1 = stringToLocation(config.getString("arena.pos1"));
            Location pos2 = stringToLocation(config.getString("arena.pos2"));
            if (pos1 != null && pos2 != null) {
                arena = new GameManager.Region(pos1, pos2);
            }
        }
        
        // 加载出生点
        if (config.contains("survivor-spawns")) {
            for (String spawnStr : config.getStringList("survivor-spawns")) {
                Location loc = stringToLocation(spawnStr);
                if (loc != null) {
                    survivorSpawns.add(loc);
                }
            }
        }
        
        if (config.contains("killer-spawns")) {
            for (String spawnStr : config.getStringList("killer-spawns")) {
                Location loc = stringToLocation(spawnStr);
                if (loc != null) {
                    killerSpawns.add(loc);
                }
            }
        }
        
        if (config.contains("killer-waiting")) {
            killerWaiting = stringToLocation(config.getString("killer-waiting"));
        }
        
        if (config.contains("supply-points")) {
            for (String pointStr : config.getStringList("supply-points")) {
                Location loc = stringToLocation(pointStr);
                if (loc != null) {
                    supplyPoints.add(loc);
                }
            }
        }
    }
    
    /**
     * 保存到配置
     */
    public void saveToConfig() {
        // 保存区域
        if (arena != null) {
            config.set("arena.pos1", locationToString(arena.getPos1()));
            config.set("arena.pos2", locationToString(arena.getPos2()));
        }
        
        // 保存出生点
        List<String> survivorSpawnStrs = survivorSpawns.stream()
            .map(this::locationToString)
            .collect(Collectors.toList());
        config.set("survivor-spawns", survivorSpawnStrs);
        
        List<String> killerSpawnStrs = killerSpawns.stream()
            .map(this::locationToString)
            .collect(Collectors.toList());
        config.set("killer-spawns", killerSpawnStrs);
        
        if (killerWaiting != null) {
            config.set("killer-waiting", locationToString(killerWaiting));
        }
        
        List<String> supplyPointStrs = supplyPoints.stream()
            .map(this::locationToString)
            .collect(Collectors.toList());
        config.set("supply-points", supplyPointStrs);
    }
    
    /**
     * 添加玩家
     */
    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        scores.put(player.getUniqueId(), 0);
        
        // 发送加入消息
        player.sendMessage("§a你已加入游戏: §e" + name);
        player.sendMessage("§7等待其他玩家加入...");
    }
    
    /**
     * 移除玩家
     */
    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        players.remove(uuid);
        survivors.remove(uuid);
        killers.remove(uuid);
        scores.remove(uuid);
        selectedClasses.remove(uuid);
        
        // 如果是观战者，也从观战列表移除
        spectators.remove(uuid);
    }
    
    /**
     * 添加观战者
     */
    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        player.sendMessage("§a你正在观战游戏: §e" + name);
    }
    
    /**
     * 移除观战者
     */
    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());
    }
    
    /**
     * 开始游戏
     */
    public boolean start() {
        if (state != GameState.WAITING) {
            return false;
        }
        
        if (players.size() < config.getInt("min-players", 2)) {
            return false;
        }
        
        if (arena == null || survivorSpawns.isEmpty() || killerSpawns.isEmpty() || killerWaiting == null) {
            return false;
        }
        
        // 选择杀手
        selectKillers();
        
        // 设置状态
        state = GameState.PREPARATION;
        preparationTime = 0;
        
        // 传送玩家
        teleportPlayers();
        
        // 应用游戏规则
        applyGameRules();
        
        // 发送开始消息
        broadcastMessage("§6游戏开始！");
        broadcastMessage("§e准备阶段: 60秒");
        
        return true;
    }
    
    /**
     * 选择杀手
     */
    private void selectKillers() {
        List<UUID> playerList = new ArrayList<>(players);
        Collections.shuffle(playerList);
        
        int killerCount = getKillerCount();
        for (int i = 0; i < Math.min(killerCount, playerList.size()); i++) {
            UUID killerId = playerList.get(i);
            killers.add(killerId);
        }
        
        // 剩余玩家为求生者
        for (UUID playerId : players) {
            if (!killers.contains(playerId)) {
                survivors.add(playerId);
            }
        }
    }
    
    /**
     * 获取杀手数量
     */
    private int getKillerCount() {
        if (config.getBoolean("killer-adjustment.enabled", false)) {
            // 动态调整
            int survivorCount = survivors.size();
            List<String> rules = config.getStringList("killer-adjustment.rules");
            
            for (String rule : rules) {
                String[] parts = rule.split(",");
                if (parts.length == 2) {
                    String survivorRange = parts[0].split(":")[1];
                    String killerCount = parts[1].split(":")[1];
                    
                    String[] range = survivorRange.split("-");
                    if (range.length == 2) {
                        int min = Integer.parseInt(range[0]);
                        int max = Integer.parseInt(range[1]);
                        
                        if (survivorCount >= min && survivorCount <= max) {
                            return Integer.parseInt(killerCount);
                        }
                    }
                }
            }
        }
        
        // 固定数量
        return config.getInt("killer-count", 1);
    }
    
    /**
     * 传送玩家
     */
    private void teleportPlayers() {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;
            
            if (killers.contains(playerId)) {
                // 杀手传送到等待点
                player.teleport(killerWaiting);
                player.sendMessage("§c你是杀手！请在等待区域等待准备阶段结束。");
            } else {
                // 求生者随机传送到出生点
                if (!survivorSpawns.isEmpty()) {
                    Location spawn = survivorSpawns.get(new Random().nextInt(survivorSpawns.size()));
                    player.teleport(spawn);
                    player.sendMessage("§a你是求生者！快找地方躲起来！");
                }
            }
        }
    }
    
    /**
     * 应用游戏规则
     */
    private void applyGameRules() {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;
            
            // 应用职业
            applyClass(player);
            
            // 设置游戏模式
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            
            // 清除效果
            player.getActivePotionEffects().forEach(effect -> 
                player.removePotionEffect(effect.getType()));
        }
    }
    
    /**
     * 应用职业
     */
    private void applyClass(Player player) {
        String className = selectedClasses.get(player.getUniqueId());
        if (className == null) {
            // 使用默认职业
            if (killers.contains(player.getUniqueId())) {
                className = "default_killer";
            } else {
                className = "default_survivor";
            }
        }
        
        // TODO: 从classesConfig加载职业配置并应用
        // 这里需要实现职业物品、效果等的应用
    }
    
    /**
     * 结束游戏
     */
    public void end() {
        state = GameState.ENDING;
        
        // 计算获胜者
        calculateWinner();
        
        // 发放奖励
        distributeRewards();
        
        // 发送结束消息
        broadcastMessage("§6游戏结束！");
        
        // 10秒后清理
        Bukkit.getScheduler().runTaskLater(Thehunt.getInstance(), () -> {
            reset();
        }, 200L); // 10秒 = 200 ticks
    }
    
    /**
     * 计算获胜者
     */
    private void calculateWinner() {
        // TODO: 根据游戏状态计算获胜阵营
    }
    
    /**
     * 发放奖励
     */
    private void distributeRewards() {
        // TODO: 根据得分发放金钱和物品奖励
    }
    
    /**
     * 重置游戏
     */
    public void reset() {
        state = GameState.WAITING;
        survivors.clear();
        killers.clear();
        gameTime = 0;
        preparationTime = 0;
        huntingTime = 0;
        
        // 传送玩家回大厅
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                // TODO: 传送回大厅
            }
        }
    }
    
    /**
     * 更新游戏状态
     */
    public void update() {
        if (state == GameState.PREPARATION) {
            preparationTime++;
            
            if (preparationTime >= 60) { // 60秒准备时间
                state = GameState.HUNTING;
                huntingTime = 0;
                
                // 传送杀手到随机出生点
                for (UUID killerId : killers) {
                    Player killer = Bukkit.getPlayer(killerId);
                    if (killer != null && !killerSpawns.isEmpty()) {
                        Location spawn = killerSpawns.get(new Random().nextInt(killerSpawns.size()));
                        killer.teleport(spawn);
                        killer.sendMessage("§c猎杀开始！找到并消灭所有求生者！");
                    }
                }
                
                broadcastMessage("§c猎杀阶段开始！");
            }
        } else if (state == GameState.HUNTING) {
            huntingTime++;
            gameTime++;
            
            // 检查获胜条件
            checkWinConditions();
            
            // 更新计分
            updateScores();
        }
    }
    
    /**
     * 检查获胜条件
     */
    private void checkWinConditions() {
        // 求生者获胜条件：生存时间达到
        int winTime = Thehunt.getInstance().getConfig().getInt("game.survivor-win-time", 300);
        if (gameTime >= winTime) {
            // 求生者获胜
            broadcastMessage("§a求生者获胜！成功生存了 " + winTime + " 秒！");
            end();
            return;
        }
        
        // 杀手获胜条件：杀光所有求生者
        if (survivors.isEmpty()) {
            broadcastMessage("§c杀手获胜！所有求生者已被消灭！");
            end();
            return;
        }
        
        // 求生者获胜条件：杀死所有杀手
        if (killers.isEmpty()) {
            broadcastMessage("§a求生者获胜！所有杀手已被消灭！");
            end();
            return;
        }
    }
    
    /**
     * 更新得分
     */
    private void updateScores() {
        // 求生者每生存60秒获得1分
        if (huntingTime % 60 == 0) {
            for (UUID survivorId : survivors) {
                int currentScore = scores.getOrDefault(survivorId, 0);
                scores.put(survivorId, currentScore + 1);
                
                Player survivor = Bukkit.getPlayer(survivorId);
                if (survivor != null) {
                    survivor.sendMessage("§a+1 分（生存时间）");
                }
            }
        }
    }
    
    /**
     * 玩家死亡处理
     */
    public void onPlayerDeath(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (survivors.contains(uuid)) {
            survivors.remove(uuid);
            
            // 杀手获得分数
            Player killer = player.getKiller();
            if (killer != null && killers.contains(killer.getUniqueId())) {
                int currentScore = scores.getOrDefault(killer.getUniqueId(), 0);
                int killScore = Thehunt.getInstance().getConfig().getInt("scoring.killer-per-kill", 5);
                scores.put(killer.getUniqueId(), currentScore + killScore);
                
                killer.sendMessage("§c+5 分（击杀求生者）");
            }
            
            // 进入观战模式
            setSpectator(player);
            
            broadcastMessage("§c求生者 " + player.getName() + " 已被淘汰！");
            
        } else if (killers.contains(uuid)) {
            killers.remove(uuid);
            
            // 进入观战模式
            setSpectator(player);
            
            broadcastMessage("§c杀手 " + player.getName() + " 已被淘汰！");
        }
    }
    
    /**
     * 设置观战模式
     */
    private void setSpectator(Player player) {
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        player.sendMessage("§7你已进入观战模式。使用 /hunt spectate 切换观战目标。");
    }
    
    /**
     * 广播消息
     */
    public void broadcastMessage(String message) {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(message);
            }
        }
        
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer(spectatorId);
            if (spectator != null) {
                spectator.sendMessage("§8[观战] " + message);
            }
        }
    }
    
    /**
     * 检查是否可以加入
     */
    public boolean canJoin() {
        if (state != GameState.WAITING) {
            return false;
        }
        
        int maxPlayers = config.getInt("max-players", 16);
        return players.size() < maxPlayers;
    }
    
    /**
     * 检查游戏是否运行中
     */
    public boolean isRunning() {
        return state == GameState.PREPARATION || state == GameState.HUNTING;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public GameState getState() {
        return state;
    }
    
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }
    
    public Set<UUID> getSurvivors() {
        return Collections.unmodifiableSet(survivors);
    }
    
    public Set<UUID> getKillers() {
        return Collections.unmodifiableSet(killers);
    }
    
    public Set<UUID> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }
    
    public Map<UUID, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public int getSurvivorCount() {
        return survivors.size();
    }
    
    public int getKillerCount() {
        return killers.size();
    }
    
    public YamlConfiguration getConfig() {
        return config;
    }
    
    public YamlConfiguration getClassesConfig() {
        return classesConfig;
    }
    
    public YamlConfiguration getSuppliesConfig() {
        return suppliesConfig;
    }
    
    public GameManager.Region getArena() {
        return arena;
    }
    
    public void setArena(GameManager.Region arena) {
        this.arena = arena;
        saveToConfig();
    }
    
    public List<Location> getSurvivorSpawns() {
        return Collections.unmodifiableList(survivorSpawns);
    }
    
    public void addSurvivorSpawn(Location location) {
        survivorSpawns.add(location);
        saveToConfig();
    }
    
    public List<Location> getKillerSpawns() {
        return Collections.unmodifiableList(killerSpawns);
    }
    
    public void addKillerSpawn(Location location) {
        killerSpawns.add(location);
        saveToConfig();
    }
    
    public Location getKillerWaiting() {
        return killerWaiting;
    }
    
    public void setKillerWaiting(Location location) {
        this.killerWaiting = location;
        saveToConfig();
    }
    
    public List<Location> getSupplyPoints() {
        return Collections.unmodifiableList(supplyPoints);
    }
    
    public void addSupplyPoint(Location location) {
        supplyPoints.add(location);
        saveToConfig();
    }
    
    public int getGameTime() {
        return gameTime;
    }
    
    public int getPreparationTime() {
        return preparationTime;
    }
    
    public int getHuntingTime() {
        return huntingTime;
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
}