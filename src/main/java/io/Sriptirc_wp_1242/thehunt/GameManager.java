package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class GameManager {
    
    private final Thehunt plugin;
    private final ConfigManager configManager;
    private final PlayerManager playerManager;
    private final MapManager mapManager;
    private final ItemManager itemManager;
    
    private GameState gameState = GameState.DISABLED;
    private int gameTimeLeft = 0;
    private int outlineTimer = 0;
    private boolean outlineActive = false;
    
    private BukkitTask gameTimerTask;
    private BukkitTask outlineTask;
    private BukkitTask countdownTask;
    
    private final List<UUID> playersInGame = new ArrayList<>();
    private final List<UUID> killers = new ArrayList<>();
    private final List<UUID> survivors = new ArrayList<>();
    private final List<UUID> spectators = new ArrayList<>();
    
    public GameManager(Thehunt plugin, ConfigManager configManager, 
                      PlayerManager playerManager, MapManager mapManager, ItemManager itemManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerManager = playerManager;
        this.mapManager = mapManager;
        this.itemManager = itemManager;
    }
    
    public void startLobby() {
        if (gameState != GameState.DISABLED) {
            return;
        }
        
        gameState = GameState.LOBBY_WAITING;
        broadcastMessage(ChatColor.GREEN + "=== The Hunt 游戏大厅已开启 ===");
        broadcastMessage(ChatColor.YELLOW + "输入 /hunt join 加入游戏");
        broadcastMessage(ChatColor.YELLOW + "需要至少 " + configManager.getMinPlayers() + " 名玩家才能开始");
    }
    
    public void joinLobby(Player player) {
        if (gameState != GameState.LOBBY_WAITING && gameState != GameState.LOBBY_COUNTDOWN) {
            player.sendMessage(ChatColor.RED + "游戏未在等待阶段，无法加入");
            return;
        }
        
        if (playersInGame.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "你已经加入了游戏");
            return;
        }
        
        if (playersInGame.size() >= configManager.getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "游戏已满员，无法加入");
            return;
        }
        
        // 保存玩家原始状态
        playerManager.getPlayerData(player.getUniqueId()).saveOriginalState(player);
        
        // 传送到大厅
        Location lobbySpawn = mapManager.getRandomLobbySpawn();
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }
        
        // 设置玩家为大厅状态
        playerManager.setPlayerRole(player.getUniqueId(), PlayerRole.LOBBY);
        playersInGame.add(player.getUniqueId());
        
        player.sendMessage(ChatColor.GREEN + "你已加入游戏大厅！");
        broadcastMessage(ChatColor.YELLOW + player.getName() + " 加入了游戏 (" + playersInGame.size() + "/" + configManager.getMaxPlayers() + ")");
        
        // 检查是否可以开始倒计时
        checkStartCountdown();
    }
    
    public void leaveGame(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!playersInGame.contains(playerId)) {
            player.sendMessage(ChatColor.RED + "你不在游戏中");
            return;
        }
        
        // 恢复玩家原始状态
        playerManager.getPlayerData(playerId).restoreOriginalState(player);
        
        // 从列表中移除
        playersInGame.remove(playerId);
        killers.remove(playerId);
        survivors.remove(playerId);
        spectators.remove(playerId);
        
        playerManager.setPlayerRole(playerId, PlayerRole.NONE);
        
        player.sendMessage(ChatColor.YELLOW + "你已离开游戏");
        broadcastMessage(ChatColor.YELLOW + player.getName() + " 离开了游戏");
        
        // 检查游戏是否应该结束
        checkGameEnd();
    }
    
    private void checkStartCountdown() {
        if (gameState == GameState.LOBBY_WAITING && 
            playersInGame.size() >= configManager.getMinPlayers()) {
            startCountdown();
        }
    }
    
    private void startCountdown() {
        gameState = GameState.LOBBY_COUNTDOWN;
        int countdownTime = 30; // 30秒倒计时
        
        broadcastMessage(ChatColor.GREEN + "=== 游戏即将开始 ===");
        broadcastMessage(ChatColor.YELLOW + "倒计时: " + countdownTime + " 秒");
        
        countdownTask = new BukkitRunnable() {
            int timeLeft = countdownTime;
            
            @Override
            public void run() {
                if (playersInGame.size() < configManager.getMinPlayers()) {
                    broadcastMessage(ChatColor.RED + "玩家不足，倒计时取消");
                    gameState = GameState.LOBBY_WAITING;
                    this.cancel();
                    return;
                }
                
                if (timeLeft <= 0) {
                    startGame();
                    this.cancel();
                    return;
                }
                
                // 每10秒或最后10秒提示
                if (timeLeft <= 10 || timeLeft % 10 == 0) {
                    broadcastMessage(ChatColor.YELLOW + "游戏开始倒计时: " + timeLeft + " 秒");
                    playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
    }
    
    private void startGame() {
        gameState = GameState.IN_PROGRESS;
        gameTimeLeft = configManager.getGameTime();
        outlineTimer = configManager.getOutlineInterval();
        
        // 分配角色
        assignRoles();
        
        // 传送玩家到游戏场地
        teleportPlayersToGame();
        
        // 给予初始物品
        giveInitialItems();
        
        // 开始游戏计时器
        startGameTimer();
        
        // 开始轮廓显示计时器
        startOutlineTimer();
        
        broadcastMessage(ChatColor.RED + "=== 游戏开始！ ===");
        broadcastMessage(ChatColor.YELLOW + "杀手: " + getKillerNames());
        broadcastMessage(ChatColor.GREEN + "求生者: " + getSurvivorNames());
        broadcastMessage(ChatColor.AQUA + "游戏时间: " + formatTime(gameTimeLeft));
        playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f);
    }
    
    private void assignRoles() {
        List<UUID> players = new ArrayList<>(playersInGame);
        Collections.shuffle(players);
        
        // 确定杀手数量
        int killerCount = (players.size() >= configManager.getKillerThreshold()) ? 2 : 1;
        
        // 分配杀手
        for (int i = 0; i < killerCount && i < players.size(); i++) {
            UUID killerId = players.get(i);
            killers.add(killerId);
            playerManager.setPlayerRole(killerId, PlayerRole.KILLER);
            
            Player killer = Bukkit.getPlayer(killerId);
            if (killer != null) {
                killer.sendMessage(ChatColor.RED + "你是杀手！击杀所有求生者获胜！");
                playerManager.getPlayerData(killerId).applyGameSettings(killer, PlayerRole.KILLER);
            }
        }
        
        // 分配求生者
        for (int i = killerCount; i < players.size(); i++) {
            UUID survivorId = players.get(i);
            survivors.add(survivorId);
            playerManager.setPlayerRole(survivorId, PlayerRole.SURVIVOR);
            
            Player survivor = Bukkit.getPlayer(survivorId);
            if (survivor != null) {
                survivor.sendMessage(ChatColor.GREEN + "你是求生者！生存 " + formatTime(configManager.getGameTime()) + " 或击杀杀手获胜！");
                playerManager.getPlayerData(survivorId).applyGameSettings(survivor, PlayerRole.SURVIVOR);
            }
        }
    }
    
    private void teleportPlayersToGame() {
        for (UUID playerId : playersInGame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                PlayerRole role = playerManager.getPlayerRole(playerId);
                Location spawn = null;
                
                if (role == PlayerRole.KILLER) {
                    spawn = mapManager.getRandomKillerSpawn();
                } else if (role == PlayerRole.SURVIVOR) {
                    spawn = mapManager.getRandomSurvivorSpawn();
                }
                
                if (spawn != null) {
                    player.teleport(spawn);
                }
            }
        }
    }
    
    private void giveInitialItems() {
        // 给予杀手初始武器
        for (UUID killerId : killers) {
            Player killer = Bukkit.getPlayer(killerId);
            if (killer != null) {
                itemManager.giveKillerItems(killer);
            }
        }
        
        // 在资源点生成物品
        mapManager.spawnResources(itemManager);
    }
    
    private void startGameTimer() {
        gameTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.IN_PROGRESS) {
                    this.cancel();
                    return;
                }
                
                gameTimeLeft--;
                
                // 更新求生者的生存时间
                for (UUID survivorId : survivors) {
                    PlayerData data = playerManager.getPlayerData(survivorId);
                    if (data != null && data.isAlive()) {
                        data.addSurvivalTime(1);
                    }
                }
                
                // 每60秒或最后60秒提示时间
                if (gameTimeLeft <= 60 || gameTimeLeft % 60 == 0) {
                    broadcastMessage(ChatColor.AQUA + "剩余时间: " + formatTime(gameTimeLeft));
                    
                    if (gameTimeLeft <= 30) {
                        playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f);
                    }
                }
                
                // 检查游戏结束条件
                checkGameEnd();
                
                // 时间到，求生者获胜
                if (gameTimeLeft <= 0) {
                    endGame(true); // 求生者获胜
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
    }
    
    private void startOutlineTimer() {
        outlineTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.IN_PROGRESS) {
                    this.cancel();
                    return;
                }
                
                outlineTimer--;
                
                if (outlineTimer <= 0) {
                    toggleOutline();
                    outlineTimer = outlineActive ? 
                        configManager.getOutlineDuration() : 
                        configManager.getOutlineInterval();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
    }
    
    private void toggleOutline() {
        outlineActive = !outlineActive;
        
        if (outlineActive) {
            // 显示轮廓
            for (UUID killerId : killers) {
                Player killer = Bukkit.getPlayer(killerId);
                if (killer != null && killer.isOnline()) {
                    killer.setGlowing(true);
                    killer.sendMessage(ChatColor.RED + "你的轮廓已被所有玩家看到！");
                }
            }
            
            broadcastMessage(ChatColor.YELLOW + "杀手轮廓已显示，持续 " + configManager.getOutlineDuration() + " 秒");
            playSound(Sound.BLOCK_BELL_USE, 1.0f);
        } else {
            // 隐藏轮廓
            for (UUID killerId : killers) {
                Player killer = Bukkit.getPlayer(killerId);
                if (killer != null) {
                    killer.setGlowing(false);
                }
            }
            
            broadcastMessage(ChatColor.YELLOW + "杀手轮廓已隐藏");
        }
    }
    
    public void playerDeath(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData data = playerManager.getPlayerData(playerId);
        
        if (data == null || !data.isAlive()) {
            return;
        }
        
        data.setAlive(false);
        
        // 检查击杀者
        Player killer = player.getKiller();
        if (killer != null) {
            PlayerData killerData = playerManager.getPlayerData(killer.getUniqueId());
            if (killerData != null) {
                killerData.addKill();
                killer.sendMessage(ChatColor.GREEN + "你击杀了 " + player.getName());
            }
        }
        
        // 根据角色处理
        PlayerRole role = data.getRole();
        
        if (role == PlayerRole.SURVIVOR) {
            survivors.remove(playerId);
            spectators.add(playerId);
            playerManager.setPlayerRole(playerId, PlayerRole.SPECTATOR);
            
            // 设置观战模式
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.sendMessage(ChatColor.GRAY + "你已死亡，进入观战模式");
            
            // 跟随随机存活的求生者
            followRandomSurvivor(player);
            
        } else if (role == PlayerRole.KILLER) {
            killers.remove(playerId);
            spectators.add(playerId);
            playerManager.setPlayerRole(playerId, PlayerRole.SPECTATOR);
            
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.sendMessage(ChatColor.GRAY + "你已死亡，进入观战模式");
            
            // 杀手死亡，跟随随机求生者
            followRandomSurvivor(player);
        }
        
        broadcastMessage(ChatColor.RED + player.getName() + " 已被" + 
                        (killer != null ? killer.getName() + " 击杀" : "淘汰"));
        
        // 检查游戏是否结束
        checkGameEnd();
    }
    
    private void followRandomSurvivor(Player spectator) {
        List<UUID> aliveSurvivors = survivors.stream()
            .filter(id -> {
                Player p = Bukkit.getPlayer(id);
                return p != null && p.isOnline() && playerManager.getPlayerData(id).isAlive();
            })
            .collect(Collectors.toList());
        
        if (!aliveSurvivors.isEmpty()) {
            Collections.shuffle(aliveSurvivors);
            Player target = Bukkit.getPlayer(aliveSurvivors.get(0));
            if (target != null) {
                spectator.teleport(target.getLocation());
                spectator.sendMessage(ChatColor.GRAY + "正在观战: " + target.getName());
            }
        }
    }
    
    private void checkGameEnd() {
        if (gameState != GameState.IN_PROGRESS) {
            return;
        }
        
        // 检查杀手是否全部死亡
        boolean allKillersDead = true;
        for (UUID killerId : killers) {
            PlayerData data = playerManager.getPlayerData(killerId);
            if (data != null && data.isAlive()) {
                allKillersDead = false;
                break;
            }
        }
        
        // 检查求生者是否全部死亡
        boolean allSurvivorsDead = true;
        for (UUID survivorId : survivors) {
            PlayerData data = playerManager.getPlayerData(survivorId);
            if (data != null && data.isAlive()) {
                allSurvivorsDead = false;
                break;
            }
        }
        
        // 游戏结束条件
        if (allKillersDead) {
            endGame(true); // 求生者获胜（杀手全灭）
        } else if (allSurvivorsDead) {
            endGame(false); // 杀手获胜（求生者全灭）
        }
    }
    
    private void endGame(boolean survivorsWin) {
        gameState = GameState.ENDING;
        
        // 取消所有任务
        if (gameTimerTask != null) {
            gameTimerTask.cancel();
        }
        if (outlineTask != null) {
            outlineTask.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        
        // 隐藏所有轮廓
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGlowing(false);
        }
        
        // 宣布获胜者
        if (survivorsWin) {
            broadcastMessage(ChatColor.GREEN + "=== 游戏结束！求生者获胜！ ===");
            playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f);
        } else {
            broadcastMessage(ChatColor.RED + "=== 游戏结束！杀手获胜！ ===");
            playSound(Sound.ENTITY_WITHER_DEATH, 1.0f);
        }
        
        // 显示统计信息
        showGameStats(survivorsWin);
        
        // 5秒后重置游戏
        new BukkitRunnable() {
            @Override
            public void run() {
                resetGame();
            }
        }.runTaskLater(plugin, 100L); // 5秒后
    }
    
    private void showGameStats(boolean survivorsWin) {
        broadcastMessage(ChatColor.YELLOW + "=== 游戏统计 ===");
        
        // 杀手统计
        broadcastMessage(ChatColor.RED + "杀手:");
        for (UUID killerId : killers) {
            Player killer = Bukkit.getPlayer(killerId);
            PlayerData data = playerManager.getPlayerData(killerId);
            if (killer != null && data != null) {
                broadcastMessage(ChatColor.RED + "  " + killer.getName() + 
                               " - 击杀: " + data.getKills() + 
                               " 伤害: " + data.getDamageDealt());
            }
        }
        
        // 求生者统计
        broadcastMessage(ChatColor.GREEN + "求生者:");
        for (UUID survivorId : survivors) {
            Player survivor = Bukkit.getPlayer(survivorId);
            PlayerData data = playerManager.getPlayerData(survivorId);
            if (survivor != null && data != null) {
                broadcastMessage(ChatColor.GREEN + "  " + survivor.getName() + 
                               " - 生存时间: " + formatTime(data.getSurvivalTime()) + 
                               " 助攻: " + data.getAssists());
            }
        }
    }
    
    private void resetGame() {
        // 恢复所有玩家状态
        for (UUID playerId : playersInGame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                playerManager.getPlayerData(playerId).restoreOriginalState(player);
            }
        }
        
        // 清空所有列表
        playersInGame.clear();
        killers.clear();
        survivors.clear();
        spectators.clear();
        
        // 重置所有玩家角色
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerManager.setPlayerRole(player.getUniqueId(), PlayerRole.NONE);
        }
        
        // 重置游戏状态
        gameState = GameState.DISABLED;
        gameTimeLeft = 0;
        outlineTimer = 0;
        outlineActive = false;
        
        broadcastMessage(ChatColor.YELLOW + "游戏已重置，可以重新开始");
    }
    
    // 工具方法
    private void broadcastMessage(String message) {
        for (UUID playerId : playersInGame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    private void playSound(Sound sound, float volume) {
        for (UUID playerId : playersInGame) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), sound, volume, 1.0f);
            }
        }
    }
    
    private String getKillerNames() {
        return killers.stream()
            .map(id -> Bukkit.getPlayer(id))
            .filter(Objects::nonNull)
            .map(Player::getName)
            .collect(Collectors.joining(", "));
    }
    
    private String getSurvivorNames() {
        return survivors.stream()
            .map(id -> Bukkit.getPlayer(id))
            .filter(Objects::nonNull)
            .map(Player::getName)
            .collect(Collectors.joining(", "));
    }
    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    // Getter 方法
    public GameState getGameState() { return gameState; }
    public List<UUID> getPlayersInGame() { return playersInGame; }
    public List<UUID> getKillers() { return killers; }
    public List<UUID> getSurvivors() { return survivors; }
    public List<UUID> getSpectators() { return spectators; }
    public int getGameTimeLeft() { return gameTimeLeft; }
    public boolean isOutlineActive() { return outlineActive; }
}