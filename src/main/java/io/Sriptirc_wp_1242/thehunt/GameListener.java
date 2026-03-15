package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class GameListener implements Listener {
    
    private final Thehunt plugin;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    
    public GameListener(Thehunt plugin, GameManager gameManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.playerManager = playerManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerRole role = playerManager.getPlayerRole(player.getUniqueId());
        
        // 只处理游戏中的玩家
        if (!playerManager.isPlayerInGame(player.getUniqueId())) {
            return;
        }
        
        // 取消死亡消息
        event.setDeathMessage(null);
        
        // 取消物品掉落
        event.getDrops().clear();
        event.setKeepInventory(false);
        event.setKeepLevel(false);
        
        // 处理玩家死亡逻辑
        gameManager.playerDeath(player);
        
        // 立即重生玩家
        event.setReviveHealth(20.0);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        PlayerRole role = playerManager.getPlayerRole(player.getUniqueId());
        
        // 只处理游戏中的玩家
        if (!playerManager.isPlayerInGame(player.getUniqueId())) {
            return;
        }
        
        // 禁止自然回血相关伤害
        if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION) {
            event.setCancelled(true);
            return;
        }
        
        // 游戏进行中才处理伤害
        if (gameManager.getGameState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }
        
        // 观战者无敌
        if (role == PlayerRole.SPECTATOR) {
            event.setCancelled(true);
            return;
        }
        
        // 大厅中的玩家无敌
        if (role == PlayerRole.LOBBY) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        
        PlayerRole victimRole = playerManager.getPlayerRole(victim.getUniqueId());
        PlayerRole attackerRole = playerManager.getPlayerRole(attacker.getUniqueId());
        
        // 只处理游戏中的玩家
        if (!playerManager.isPlayerInGame(victim.getUniqueId()) || 
            !playerManager.isPlayerInGame(attacker.getUniqueId())) {
            return;
        }
        
        // 游戏进行中才处理PVP
        if (gameManager.getGameState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }
        
        // 观战者不能攻击也不能被攻击
        if (victimRole == PlayerRole.SPECTATOR || attackerRole == PlayerRole.SPECTATOR) {
            event.setCancelled(true);
            return;
        }
        
        // 大厅中的玩家不能攻击也不能被攻击
        if (victimRole == PlayerRole.LOBBY || attackerRole == PlayerRole.LOBBY) {
            event.setCancelled(true);
            return;
        }
        
        // 记录伤害数据
        PlayerData attackerData = playerManager.getPlayerData(attacker.getUniqueId());
        if (attackerData != null) {
            attackerData.addDamageDealt((int) event.getDamage());
        }
        
        // 发送伤害提示
        attacker.sendMessage(ChatColor.YELLOW + "你对 " + victim.getName() + " 造成了 " + 
                           (int) event.getDamage() + " 点伤害");
        victim.sendMessage(ChatColor.RED + attacker.getName() + " 对你造成了 " + 
                          (int) event.getDamage() + " 点伤害");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // 禁止游戏中的玩家饥饿
        if (playerManager.isPlayerInGame(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // 禁止游戏中的玩家破坏方块
        if (playerManager.isPlayerInGame(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏中禁止破坏方块！");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // 禁止游戏中的玩家放置方块
        if (playerManager.isPlayerInGame(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏中禁止放置方块！");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // 检查玩家是否在游戏中
        if (!playerManager.isPlayerInGame(player.getUniqueId())) {
            return;
        }
        
        // 游戏进行中时禁止所有指令
        if (gameManager.getGameState() == GameState.IN_PROGRESS) {
            // 允许一些必要的指令
            if (command.startsWith("/msg") || 
                command.startsWith("/tell") || 
                command.startsWith("/whisper") ||
                command.startsWith("/w") ||
                command.startsWith("/r") ||
                command.startsWith("/reply")) {
                return; // 允许私聊
            }
            
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏中禁止使用指令！");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 玩家退出时离开游戏
        if (playerManager.isPlayerInGame(player.getUniqueId())) {
            gameManager.leaveGame(player);
        }
        
        // 移除玩家数据
        playerManager.removePlayer(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 隐藏加入消息
        event.setJoinMessage(null);
        
        // 如果游戏正在进行中，新加入的玩家设为观战者
        if (gameManager.getGameState() == GameState.IN_PROGRESS) {
            playerManager.setPlayerRole(player.getUniqueId(), PlayerRole.SPECTATOR);
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.sendMessage(ChatColor.GRAY + "游戏正在进行中，你已进入观战模式");
            
            // 跟随随机存活的求生者
            gameManager.playerDeath(player); // 复用死亡逻辑进入观战
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // 游戏中的玩家重生到观战位置
        if (playerManager.isPlayerInGame(player.getUniqueId())) {
            PlayerRole role = playerManager.getPlayerRole(player.getUniqueId());
            
            if (role == PlayerRole.SPECTATOR) {
                // 观战者重生到随机求生者位置
                event.setRespawnLocation(player.getLocation()); // 保持当前位置
            } else if (role == PlayerRole.LOBBY) {
                // 大厅玩家重生到大厅
                // 这里需要地图管理器提供大厅位置
                event.setRespawnLocation(player.getLocation());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // 禁止游戏中的玩家丢弃物品
        if (playerManager.isPlayerInGame(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏中禁止丢弃物品！");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // 禁止游戏中的玩家使用末影箱等
        if (playerManager.isPlayerInGame(player.getUniqueId())) {
            if (event.getClickedBlock() != null) {
                switch (event.getClickedBlock().getType()) {
                    case CHEST:
                    case TRAPPED_CHEST:
                    case FURNACE:
                    case BLAST_FURNACE:
                    case SMOKER:
                    case BARREL:
                    case SHULKER_BOX:
                    case BLACK_SHULKER_BOX:
                    case BLUE_SHULKER_BOX:
                    case BROWN_SHULKER_BOX:
                    case CYAN_SHULKER_BOX:
                    case GRAY_SHULKER_BOX:
                    case GREEN_SHULKER_BOX:
                    case LIGHT_BLUE_SHULKER_BOX:
                    case LIGHT_GRAY_SHULKER_BOX:
                    case LIME_SHULKER_BOX:
                    case MAGENTA_SHULKER_BOX:
                    case ORANGE_SHULKER_BOX:
                    case PINK_SHULKER_BOX:
                    case PURPLE_SHULKER_BOX:
                    case RED_SHULKER_BOX:
                    case WHITE_SHULKER_BOX:
                    case YELLOW_SHULKER_BOX:
                    case ENDER_CHEST:
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "游戏中禁止使用容器！");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}