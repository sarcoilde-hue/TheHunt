package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final Thehunt plugin;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final MapManager mapManager;
    private final ConfigManager configManager;
    
    public CommandHandler(Thehunt plugin, GameManager gameManager, PlayerManager playerManager,
                         MapManager mapManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.playerManager = playerManager;
        this.mapManager = mapManager;
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("hunt")) {
            return handleHuntCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("huntadmin")) {
            return handleHuntAdminCommand(sender, args);
        }
        return false;
    }
    
    private boolean handleHuntCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家才能执行此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                if (!player.hasPermission("thehunt.join")) {
                    player.sendMessage(ChatColor.RED + "你没有权限加入游戏！");
                    return true;
                }
                gameManager.joinLobby(player);
                return true;
                
            case "leave":
                if (!player.hasPermission("thehunt.leave")) {
                    player.sendMessage(ChatColor.RED + "你没有权限离开游戏！");
                    return true;
                }
                gameManager.leaveGame(player);
                return true;
                
            case "info":
                sendGameInfo(player);
                return true;
                
            case "help":
                sendHelp(player);
                return true;
                
            default:
                player.sendMessage(ChatColor.RED + "未知命令！使用 /hunt help 查看帮助");
                return true;
        }
    }
    
    private boolean handleHuntAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("thehunt.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有管理员权限！");
            return true;
        }
        
        if (args.length == 0) {
            sendAdminHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "start":
                if (!sender.hasPermission("thehunt.start")) {
                    sender.sendMessage(ChatColor.RED + "你没有权限开始游戏！");
                    return true;
                }
                if (gameManager.getGameState() == GameState.DISABLED) {
                    gameManager.startLobby();
                    sender.sendMessage(ChatColor.GREEN + "游戏大厅已开启！");
                } else {
                    sender.sendMessage(ChatColor.RED + "游戏已经在进行中！");
                }
                return true;
                
            case "stop":
                if (!sender.hasPermission("thehunt.stop")) {
                    sender.sendMessage(ChatColor.RED + "你没有权限停止游戏！");
                    return true;
                }
                // 这里需要实现强制停止游戏的逻辑
                sender.sendMessage(ChatColor.YELLOW + "强制停止游戏功能待实现");
                return true;
                
            case "reload":
                configManager.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "配置文件已重载！");
                return true;
                
            case "setspawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "只有玩家才能设置出生点！");
                    return true;
                }
                if (!sender.hasPermission("thehunt.setup")) {
                    sender.sendMessage(ChatColor.RED + "你没有权限设置出生点！");
                    return true;
                }
                return handleSetSpawn((Player) sender, args);
                
            case "status":
                sendGameStatus(sender);
                return true;
                
            case "help":
                sendAdminHelp(sender);
                return true;
                
            default:
                sender.sendMessage(ChatColor.RED + "未知命令！使用 /huntadmin help 查看帮助");
                return true;
        }
    }
    
    private boolean handleSetSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /huntadmin setspawn <killer|survivor|resource>");
            return true;
        }
        
        Location location = player.getLocation();
        String type = args[1].toLowerCase();
        
        switch (type) {
            case "killer":
                mapManager.addKillerSpawn(location);
                player.sendMessage(ChatColor.GREEN + "杀手出生点已设置！");
                return true;
                
            case "survivor":
                mapManager.addSurvivorSpawn(location);
                player.sendMessage(ChatColor.GREEN + "求生者出生点已设置！");
                return true;
                
            case "resource":
                mapManager.addResourcePoint(location);
                player.sendMessage(ChatColor.GREEN + "资源点已设置！");
                return true;
                
            default:
                player.sendMessage(ChatColor.RED + "类型必须是: killer, survivor 或 resource");
                return true;
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "=== The Hunt 游戏帮助 ===");
        player.sendMessage(ChatColor.YELLOW + "/hunt join - 加入游戏");
        player.sendMessage(ChatColor.YELLOW + "/hunt leave - 离开游戏");
        player.sendMessage(ChatColor.YELLOW + "/hunt info - 查看游戏信息");
        player.sendMessage(ChatColor.YELLOW + "/hunt help - 显示此帮助");
    }
    
    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "=== The Hunt 管理员帮助 ===");
        sender.sendMessage(ChatColor.YELLOW + "/huntadmin start - 开启游戏大厅");
        sender.sendMessage(ChatColor.YELLOW + "/huntadmin stop - 强制停止游戏");
        sender.sendMessage(ChatColor.YELLOW + "/huntadmin reload - 重载配置文件");
        sender.sendMessage(ChatColor.YELLOW + "/huntadmin setspawn <类型> - 设置出生点");
        sender.sendMessage(ChatColor.YELLOW + "/huntadmin status - 查看游戏状态");
        sender.sendMessage(ChatColor.YELLOW + "/huntadmin help - 显示此帮助");
        sender.sendMessage(ChatColor.GRAY + "出生点类型: killer, survivor, resource");
    }
    
    private void sendGameInfo(Player player) {
        GameState state = gameManager.getGameState();
        
        player.sendMessage(ChatColor.GREEN + "=== The Hunt 游戏信息 ===");
        player.sendMessage(ChatColor.YELLOW + "状态: " + getStateText(state));
        
        if (state == GameState.LOBBY_WAITING || state == GameState.LOBBY_COUNTDOWN) {
            player.sendMessage(ChatColor.YELLOW + "玩家: " + gameManager.getPlayersInGame().size() + 
                             "/" + configManager.getMaxPlayers());
            player.sendMessage(ChatColor.YELLOW + "最少需要: " + configManager.getMinPlayers() + " 名玩家");
        } else if (state == GameState.IN_PROGRESS) {
            player.sendMessage(ChatColor.YELLOW + "剩余时间: " + formatTime(gameManager.getGameTimeLeft()));
            player.sendMessage(ChatColor.RED + "存活杀手: " + gameManager.getKillers().size());
            player.sendMessage(ChatColor.GREEN + "存活求生者: " + gameManager.getSurvivors().size());
            player.sendMessage(ChatColor.GRAY + "观战者: " + gameManager.getSpectators().size());
            
            if (gameManager.isOutlineActive()) {
                player.sendMessage(ChatColor.AQUA + "轮廓显示: " + ChatColor.GREEN + "开启");
            } else {
                player.sendMessage(ChatColor.AQUA + "轮廓显示: " + ChatColor.RED + "关闭");
            }
        }
    }
    
    private void sendGameStatus(CommandSender sender) {
        GameState state = gameManager.getGameState();
        
        sender.sendMessage(ChatColor.GREEN + "=== The Hunt 游戏状态 ===");
        sender.sendMessage(ChatColor.YELLOW + "游戏状态: " + getStateText(state));
        sender.sendMessage(ChatColor.YELLOW + "配置版本: " + configManager.getConfig().getInt("ScriptIrc-config-version", 1));
        sender.sendMessage(ChatColor.YELLOW + "游戏时间: " + configManager.getGameTime() + " 秒");
        sender.sendMessage(ChatColor.YELLOW + "轮廓显示: 每 " + configManager.getOutlineInterval() + 
                         " 秒显示 " + configManager.getOutlineDuration() + " 秒");
        sender.sendMessage(ChatColor.YELLOW + "玩家范围: " + configManager.getMinPlayers() + 
                         " - " + configManager.getMaxPlayers());
        sender.sendMessage(ChatColor.YELLOW + "双杀手阈值: " + configManager.getKillerThreshold() + " 人");
        
        if (state != GameState.DISABLED) {
            sender.sendMessage(ChatColor.YELLOW + "当前玩家: " + gameManager.getPlayersInGame().size());
            sender.sendMessage(ChatColor.YELLOW + "出生点数量:");
            sender.sendMessage(ChatColor.RED + "  杀手: " + mapManager.getKillerSpawns().size());
            sender.sendMessage(ChatColor.GREEN + "  求生者: " + mapManager.getSurvivorSpawns().size());
            sender.sendMessage(ChatColor.AQUA + "  资源点: " + mapManager.getResourcePoints().size());
        }
    }
    
    private String getStateText(GameState state) {
        switch (state) {
            case DISABLED: return ChatColor.RED + "未启用";
            case LOBBY_WAITING: return ChatColor.YELLOW + "大厅等待中";
            case LOBBY_COUNTDOWN: return ChatColor.GOLD + "大厅倒计时";
            case IN_PROGRESS: return ChatColor.GREEN + "游戏进行中";
            case ENDING: return ChatColor.AQUA + "游戏结束中";
            default: return ChatColor.GRAY + "未知";
        }
    }
    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (command.getName().equalsIgnoreCase("hunt")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("join", "leave", "info", "help"));
            }
        } else if (command.getName().equalsIgnoreCase("huntadmin")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("start", "stop", "reload", "setspawn", "status", "help"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("setspawn")) {
                completions.addAll(Arrays.asList("killer", "survivor", "resource"));
            }
        }
        
        // 过滤匹配的补全
        if (args.length > 0) {
            String lastArg = args[args.length - 1].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(lastArg));
        }
        
        return completions;
    }
}