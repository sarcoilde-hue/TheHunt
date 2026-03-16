package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

/**
 * 事件监听器
 */
public class EventListener implements Listener {
    
    private final Thehunt plugin;
    private final GameManager gameManager;
    private final CommandHandler commandHandler;
    
    public EventListener(Thehunt plugin, GameManager gameManager, CommandHandler commandHandler) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.commandHandler = commandHandler;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        String displayName = meta.getDisplayName();
        
        // 检查是否是区域编辑工具
        if (displayName.contains("大厅区域编辑器")) {
            event.setCancelled(true);
            
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location location = event.getClickedBlock().getLocation();
                commandHandler.setLobbyPoint(player, location, true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location location = event.getClickedBlock().getLocation();
                commandHandler.setLobbyPoint(player, location, false);
            }
            
        } else if (displayName.contains("游戏场地编辑器")) {
            event.setCancelled(true);
            
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location location = event.getClickedBlock().getLocation();
                commandHandler.setArenaPoint(player, location, true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location location = event.getClickedBlock().getLocation();
                commandHandler.setArenaPoint(player, location, false);
            }
        }
        
        // 检查告示牌交互
        if (event.getClickedBlock() != null && 
            event.getClickedBlock().getType().name().contains("SIGN")) {
            // TODO: 处理告示牌加入/离开游戏
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // 检查是否在大厅中走出范围
        GameManager.Region lobbyRegion = gameManager.getLobbyRegion();
        if (lobbyRegion != null) {
            boolean wasInLobby = gameManager.isInLobby(from);
            boolean isInLobby = gameManager.isInLobby(to);
            
            // 如果玩家在大厅中但走出了范围
            if (wasInLobby && !isInLobby) {
                // 检查玩家是否在游戏中
                Game game = gameManager.getPlayerGame(player);
                if (game != null && game.getState() == Game.GameState.WAITING) {
                    // 传送回大厅出生点
                    Location lobbySpawn = gameManager.getLobbySpawn();
                    if (lobbySpawn != null) {
                        player.teleport(lobbySpawn);
                        player.sendMessage(ChatColor.RED + "请不要离开大厅区域！");
                    }
                }
            }
        }
        
        // 检查是否在游戏场地中走出范围
        Game game = gameManager.getPlayerGame(player);
        if (game != null && game.isRunning()) {
            GameManager.Region arena = game.getArena();
            if (arena != null) {
                boolean wasInArena = arena.contains(from);
                boolean isInArena = arena.contains(to);
                
                // 如果玩家在游戏场地中但走出了范围
                if (wasInArena && !isInArena) {
                    // 玩家死亡
                    player.setHealth(0);
                    player.sendMessage(ChatColor.RED + "你离开了游戏场地！");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Game game = gameManager.getPlayerGame(player);
        
        if (game != null && game.isRunning()) {
            // 处理游戏内死亡
            game.onPlayerDeath(player);
            
            // 取消死亡消息
            event.setDeathMessage(null);
            
            // 不清除物品
            event.getDrops().clear();
            event.setKeepInventory(true);
            event.setKeepLevel(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        Game game = gameManager.getPlayerGame(player);
        
        if (game != null) {
            // 检查游戏规则
            if (plugin.getConfig().getBoolean("game.disable-natural-regeneration", true)) {
                // 禁止自然伤害（摔落、溺水等）
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL ||
                    event.getCause() == EntityDamageEvent.DamageCause.DROWNING ||
                    event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION ||
                    event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR ||
                    event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING ||
                    event.getCause() == EntityDamageEvent.DamageCause.POISON ||
                    event.getCause() == EntityDamageEvent.DamageCause.WITHER ||
                    event.getCause() == EntityDamageEvent.DamageCause.STARVATION ||
                    event.getCause() == EntityDamageEvent.DamageCause.MAGIC ||
                    event.getCause() == EntityDamageEvent.DamageCause.DRYOUT) {
                    
                    // 在准备阶段和猎杀阶段允许伤害
                    if (game.getState() == Game.GameState.PREPARATION || 
                        game.getState() == Game.GameState.HUNTING) {
                        // 允许伤害
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
            
            // 检查生命数
            int lives = plugin.getConfig().getInt("game.lives", 1);
            if (lives == 1 && player.getHealth() - event.getFinalDamage() <= 0) {
                // 单生命模式，允许死亡
            } else if (player.getHealth() - event.getFinalDamage() <= 0) {
                // 多生命模式，恢复生命
                event.setCancelled(true);
                player.setHealth(player.getMaxHealth());
                player.sendMessage(ChatColor.YELLOW + "你失去了一条生命！剩余生命: " + (lives - 1));
                // TODO: 减少生命计数
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        
        Game game = gameManager.getPlayerGame(victim);
        if (game == null) return;
        
        // 检查同阵营PVP
        if (plugin.getConfig().getBoolean("game.disable-friendly-fire", true)) {
            boolean victimIsKiller = game.getKillers().contains(victim.getUniqueId());
            boolean attackerIsKiller = game.getKillers().contains(attacker.getUniqueId());
            
            // 同阵营伤害
            if (victimIsKiller == attackerIsKiller) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "你不能攻击同阵营的玩家！");
            }
        }
        
        // 检查大厅PVP
        if (game.getState() == Game.GameState.WAITING) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + "大厅区域内禁止PVP！");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        Game game = gameManager.getPlayerGame(player);
        
        if (game != null && plugin.getConfig().getBoolean("game.disable-hunger", true)) {
            // 禁止饥饿
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Game game = gameManager.getPlayerGame(player);
        
        if (game != null && plugin.getConfig().getBoolean("game.disable-block-break", true)) {
            // 禁止破坏方块
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏内禁止破坏方块！");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Game game = gameManager.getPlayerGame(player);
        
        if (game != null && plugin.getConfig().getBoolean("game.disable-block-place", true)) {
            // 禁止放置方块
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏内禁止放置方块！");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Game game = gameManager.getPlayerGame(player);
        
        if (game != null && game.isRunning()) {
            String command = event.getMessage().toLowerCase();
            
            // 管理员豁免
            if (player.hasPermission("thehunt.admin")) {
                return;
            }
            
            // 检查白名单
            boolean allowed = false;
            for (String whitelisted : plugin.getConfig().getStringList("command-whitelist")) {
                if (command.startsWith(whitelisted.toLowerCase())) {
                    allowed = true;
                    break;
                }
            }
            
            // 插件命令豁免
            if (command.startsWith("/hunt")) {
                allowed = true;
            }
            
            if (!allowed) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "游戏内禁止使用此命令！");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 玩家退出时离开游戏
        Game game = gameManager.getPlayerGame(player);
        if (game != null) {
            gameManager.leaveGame(player);
        }
        
        // 玩家退出时停止观战
        Game spectatorGame = gameManager.getSpectatorGame(player);
        if (spectatorGame != null) {
            gameManager.stopSpectating(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // TODO: 恢复观战状态等
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Game game = gameManager.getPlayerGame(player);
        
        if (game != null) {
            // 设置重生点为大厅
            Location lobbySpawn = gameManager.getLobbySpawn();
            if (lobbySpawn != null) {
                event.setRespawnLocation(lobbySpawn);
            }
        }
    }
}