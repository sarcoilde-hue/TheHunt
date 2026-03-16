package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 命令处理器
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final Thehunt plugin;
    private final GameManager gameManager;
    
    // 区域编辑工具
    private final Map<UUID, Location> lobbyPos1 = new HashMap<>();
    private final Map<UUID, Location> lobbyPos2 = new HashMap<>();
    private final Map<UUID, String> editingArena = new HashMap<>();
    private final Map<UUID, Location> arenaPos1 = new HashMap<>();
    private final Map<UUID, Location> arenaPos2 = new HashMap<>();
    
    public CommandHandler(Thehunt plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        try {
            switch (subCommand) {
                case "join":
                    return handleJoin(sender, args);
                case "leave":
                    return handleLeave(sender, args);
                case "spectate":
                    return handleSpectate(sender, args);
                case "class":
                    return handleClass(sender, args);
                case "reload":
                    return handleReload(sender, args);
                case "editlobby":
                    return handleEditLobby(sender, args);
                case "setlobby":
                    return handleSetLobby(sender, args);
                case "create":
                    return handleCreate(sender, args);
                case "editarena":
                    return handleEditArena(sender, args);
                case "setarena":
                    return handleSetArena(sender, args);
                case "sethumanspawn":
                    return handleSetHumanSpawn(sender, args);
                case "setkillerspawn":
                    return handleSetKillerSpawn(sender, args);
                case "setkillerwaiting":
                    return handleSetKillerWaiting(sender, args);
                case "setsupply":
                    return handleSetSupply(sender, args);
                case "humanclass":
                    return handleHumanClass(sender, args);
                case "killerclass":
                    return handleKillerClass(sender, args);
                case "supply":
                    return handleSupply(sender, args);
                case "start":
                    return handleStart(sender, args);
                case "stop":
                    return handleStop(sender, args);
                case "setsign":
                    return handleSetSign(sender, args);
                case "setleavesign":
                    return handleSetLeaveSign(sender, args);
                default:
                    sender.sendMessage(ChatColor.RED + "未知命令。使用 /hunt help 查看帮助。");
                    return true;
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "执行命令时发生错误: " + e.getMessage());
            plugin.getLogger().warning("命令执行错误: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 主命令补全
            String[] commands = {"join", "leave", "spectate", "class", "reload", "editlobby", "setlobby", 
                               "create", "editarena", "setarena", "sethumanspawn", "setkillerspawn", 
                               "setkillerwaiting", "setsupply", "humanclass", "killerclass", "supply", 
                               "start", "stop", "setsign", "setleavesign"};
            
            for (String cmd : commands) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            // 游戏名补全
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("join") || subCommand.equals("leave") || subCommand.equals("spectate") ||
                subCommand.equals("editarena") || subCommand.equals("setarena") || subCommand.equals("sethumanspawn") ||
                subCommand.equals("setkillerspawn") || subCommand.equals("setkillerwaiting") || 
                subCommand.equals("setsupply") || subCommand.equals("start") || subCommand.equals("stop") ||
                subCommand.equals("setsign")) {
                
                for (String gameName : gameManager.getGameNames()) {
                    if (gameName.startsWith(args[1].toLowerCase())) {
                        completions.add(gameName);
                    }
                }
            } else if (subCommand.equals("create")) {
                // 创建游戏时不需要补全
            } else if (subCommand.equals("humanclass") || subCommand.equals("killerclass")) {
                // 职业命令补全
                String[] subCommands = {"save", "list", "delete", "setdefault"};
                for (String sub : subCommands) {
                    if (sub.startsWith(args[1].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            } else if (subCommand.equals("supply")) {
                // 资源命令补全
                String[] subCommands = {"add", "list", "remove", "clear"};
                for (String sub : subCommands) {
                    if (sub.startsWith(args[1].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            }
        } else if (args.length == 3) {
            // 职业名或游戏名补全
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("humanclass") || subCommand.equals("killerclass")) {
                if (args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("delete")) {
                    // 职业名不需要补全
                } else if (args[1].equalsIgnoreCase("setdefault")) {
                    // 游戏名补全
                    for (String gameName : gameManager.getGameNames()) {
                        if (gameName.startsWith(args[2].toLowerCase())) {
                            completions.add(gameName);
                        }
                    }
                }
            } else if (subCommand.equals("supply")) {
                // 游戏名补全
                for (String gameName : gameManager.getGameNames()) {
                    if (gameName.startsWith(args[2].toLowerCase())) {
                        completions.add(gameName);
                    }
                }
            } else if (subCommand.equals("create")) {
                // 创建游戏时参数补全
                if (args.length == 3) {
                    // 杀手数量建议
                    completions.add("1");
                    completions.add("2");
                    completions.add("3");
                }
            }
        } else if (args.length == 4) {
            // 创建游戏参数补全
            if (args[0].equalsIgnoreCase("create")) {
                // 求生者数量建议
                completions.add("4");
                completions.add("8");
                completions.add("12");
                completions.add("16");
            }
        } else if (args.length == 5) {
            // 创建游戏参数补全
            if (args[0].equalsIgnoreCase("create")) {
                // 最大玩家数建议
                completions.add("8");
                completions.add("12");
                completions.add("16");
                completions.add("20");
            }
        }
        
        return completions;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== The Hunt 插件帮助 ===");
        sender.sendMessage(ChatColor.YELLOW + "玩家命令:");
        sender.sendMessage(ChatColor.GREEN + "/hunt join <游戏名>" + ChatColor.WHITE + " - 加入游戏");
        sender.sendMessage(ChatColor.GREEN + "/hunt leave <游戏名>" + ChatColor.WHITE + " - 退出游戏");
        sender.sendMessage(ChatColor.GREEN + "/hunt spectate <游戏名>" + ChatColor.WHITE + " - 观战游戏");
        sender.sendMessage(ChatColor.GREEN + "/hunt class" + ChatColor.WHITE + " - 选择职业");
        
        if (sender.hasPermission("thehunt.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "\n管理员命令:");
            sender.sendMessage(ChatColor.GREEN + "/hunt reload" + ChatColor.WHITE + " - 重载插件");
            sender.sendMessage(ChatColor.GREEN + "/hunt editlobby" + ChatColor.WHITE + " - 编辑大厅区域");
            sender.sendMessage(ChatColor.GREEN + "/hunt setlobby" + ChatColor.WHITE + " - 设置大厅");
            sender.sendMessage(ChatColor.GREEN + "/hunt create <游戏名> <杀手数> <求生者数> <最大玩家数>" + ChatColor.WHITE + " - 创建游戏");
            sender.sendMessage(ChatColor.GREEN + "/hunt editarena <游戏名>" + ChatColor.WHITE + " - 编辑游戏场地");
            sender.sendMessage(ChatColor.GREEN + "/hunt setarena <游戏名>" + ChatColor.WHITE + " - 设置游戏场地");
            sender.sendMessage(ChatColor.GREEN + "/hunt start <游戏名>" + ChatColor.WHITE + " - 强制开始游戏");
            sender.sendMessage(ChatColor.GREEN + "/hunt stop <游戏名>" + ChatColor.WHITE + " - 强制结束游戏");
        }
    }
    
    private boolean handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.join")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt join <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        String gameName = args[1];
        
        if (gameManager.joinGame(player, gameName)) {
            player.sendMessage(ChatColor.GREEN + "成功加入游戏: " + gameName);
        } else {
            player.sendMessage(ChatColor.RED + "无法加入游戏。可能原因：游戏不存在、游戏已满、游戏已开始、或你已在其他游戏中。");
        }
        
        return true;
    }
    
    private boolean handleLeave(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.leave")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (gameManager.leaveGame(player)) {
            player.sendMessage(ChatColor.GREEN + "已退出游戏。");
        } else {
            player.sendMessage(ChatColor.RED + "你不在任何游戏中。");
        }
        
        return true;
    }
    
    private boolean handleSpectate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.spectate")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt spectate <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        String gameName = args[1];
        
        if (gameManager.spectateGame(player, gameName)) {
            player.sendMessage(ChatColor.GREEN + "正在观战游戏: " + gameName);
        } else {
            player.sendMessage(ChatColor.RED + "无法观战游戏。可能原因：游戏不存在、游戏未开始。");
        }
        
        return true;
    }
    
    private boolean handleClass(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.class")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        Player player = (Player) sender;
        
        // TODO: 打开职业选择GUI
        player.sendMessage(ChatColor.YELLOW + "职业选择功能开发中...");
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("thehunt.reload")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        plugin.reloadConfig();
        gameManager.loadGames();
        gameManager.loadLobbyConfig();
        
        sender.sendMessage(ChatColor.GREEN + "插件配置已重载。");
        return true;
    }
    
    private boolean handleEditLobby(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 给予烈焰棒
        ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = blazeRod.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "大厅区域编辑器");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "左键点击设置第一个点");
        lore.add(ChatColor.GRAY + "右键点击设置第二个点");
        lore.add(ChatColor.GRAY + "然后使用 /hunt setlobby 确认");
        meta.setLore(lore);
        blazeRod.setItemMeta(meta);
        
        player.getInventory().addItem(blazeRod);
        player.sendMessage(ChatColor.GREEN + "已获得大厅区域编辑器。");
        player.sendMessage(ChatColor.YELLOW + "使用烈焰棒左键和右键点击来划定大厅区域。");
        
        // 清空之前的点
        lobbyPos1.remove(player.getUniqueId());
        lobbyPos2.remove(player.getUniqueId());
        
        return true;
    }
    
    private boolean handleSetLobby(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        if (!lobbyPos1.containsKey(uuid) || !lobbyPos2.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "请先使用烈焰棒设置两个点。");
            return true;
        }
        
        Location pos1 = lobbyPos1.get(uuid);
        Location pos2 = lobbyPos2.get(uuid);
        
        // 设置大厅区域
        gameManager.setLobbyRegion(pos1, pos2);
        
        // 设置大厅出生点为玩家当前位置
        gameManager.setLobbySpawn(player.getLocation());
        
        // 保存配置
        gameManager.saveLobbyConfig();
        
        player.sendMessage(ChatColor.GREEN + "大厅区域已设置。");
        player.sendMessage(ChatColor.GREEN + "出生点: " + formatLocation(player.getLocation()));
        
        // 清理
        lobbyPos1.remove(uuid);
        lobbyPos2.remove(uuid);
        
        return true;
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("thehunt.create")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt create <游戏名> <杀手数> <求生者数> <最大玩家数>");
            sender.sendMessage(ChatColor.RED + "注意: 杀手数必须小于求生者数，最大玩家数至少为2");
            return true;
        }
        
        String gameName = args[1];
        
        try {
            int killerCount = Integer.parseInt(args[2]);
            int survivorCount = Integer.parseInt(args[3]);
            int maxPlayers = Integer.parseInt(args[4]);
            
            if (maxPlayers < 2) {
                sender.sendMessage(ChatColor.RED + "错误: 最大玩家数不能少于2。");
                return true;
            }
            
            if (killerCount >= survivorCount) {
                sender.sendMessage(ChatColor.RED + "错误: 杀手数量必须小于求生者数量。");
                return true;
            }
            
            if (killerCount < 1) {
                sender.sendMessage(ChatColor.RED + "错误: 至少需要1名杀手。");
                return true;
            }
            
            if (gameManager.createGame(gameName, killerCount, survivorCount, maxPlayers)) {
                sender.sendMessage(ChatColor.GREEN + "游戏 '" + gameName + "' 创建成功！");
                sender.sendMessage(ChatColor.YELLOW + "接下来需要:");
                sender.sendMessage(ChatColor.YELLOW + "1. 使用 /hunt editarena " + gameName + " 设置游戏场地");
                sender.sendMessage(ChatColor.YELLOW + "2. 使用 /hunt setarena " + gameName + " 确认场地");
                sender.sendMessage(ChatColor.YELLOW + "3. 设置出生点和资源点");
            } else {
                sender.sendMessage(ChatColor.RED + "创建游戏失败。可能原因：游戏名已存在或参数无效。");
            }
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "错误: 参数必须是数字。");
        }
        
        return true;
    }
    
    private boolean handleEditArena(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt editarena <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        String gameName = args[1];
        
        Game game = gameManager.getGame(gameName);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        // 给予木棒
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "游戏场地编辑器 - " + gameName);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "左键点击设置第一个点");
        lore.add(ChatColor.GRAY + "右键点击设置第二个点");
        lore.add(ChatColor.GRAY + "然后使用 /hunt setarena " + gameName + " 确认");
        meta.setLore(lore);
        stick.setItemMeta(meta);
        
        player.getInventory().addItem(stick);
        player.sendMessage(ChatColor.GREEN + "已获得游戏场地编辑器。");
        player.sendMessage(ChatColor.YELLOW + "使用木棒左键和右键点击来划定游戏场地。");
        
        // 记录正在编辑的游戏
        editingArena.put(player.getUniqueId(), gameName);
        arenaPos1.remove(player.getUniqueId());
        arenaPos2.remove(player.getUniqueId());
        
        return true;
    }
    
    private boolean handleSetArena(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt setarena <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String gameName = args[1];
        
        if (!editingArena.containsKey(uuid) || !editingArena.get(uuid).equals(gameName)) {
            player.sendMessage(ChatColor.RED + "请先使用 /hunt editarena " + gameName + " 开始编辑。");
            return true;
        }
        
        if (!arenaPos1.containsKey(uuid) || !arenaPos2.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "请先使用木棒设置两个点。");
            return true;
        }
        
        Game game = gameManager.getGame(gameName);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        Location pos1 = arenaPos1.get(uuid);
        Location pos2 = arenaPos2.get(uuid);
        
        // 设置游戏场地
        game.setArena(new GameManager.Region(pos1, pos2));
        
        player.sendMessage(ChatColor.GREEN + "游戏场地已设置: " + gameName);
        player.sendMessage(ChatColor.YELLOW + "接下来需要设置出生点和资源点。");
        
        // 清理
        editingArena.remove(uuid);
        arenaPos1.remove(uuid);
        arenaPos2.remove(uuid);
        
        return true;
    }
    
    private boolean handleSetHumanSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt sethumanspawn <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        String gameName = args[1];
        
        Game game = gameManager.getGame(gameName);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        game.addSurvivorSpawn(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "已添加求生者出生点: " + formatLocation(player.getLocation()));
        player.sendMessage(ChatColor.YELLOW + "当前求生者出生点数量: " + game.getSurvivorSpawns().size());
        
        return true;
    }
    
    private boolean handleSetKillerSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt setkillerspawn <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        String gameName = args[1];
        
        Game game = gameManager.getGame(gameName);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        game.addKillerSpawn(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "已添加杀手出生点: " + formatLocation(player.getLocation()));
        player.sendMessage(ChatColor.YELLOW + "当前杀手出生点数量: " + game.getKillerSpawns().size());
        
        return true;
    }
    
    private boolean handleSetKillerWaiting(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt setkillerwaiting <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        String gameName = args[1];
        
        Game game = gameManager.getGame(gameName);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        game.setKillerWaiting(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "已设置杀手等待点: " + formatLocation(player.getLocation()));
        
        return true;
    }
    
    private boolean handleSetSupply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        
        if (!sender.hasPermission("thehunt.edit")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt setsupply <游戏名>");
            return true;
        }
        
        Player player = (Player) sender;
        String gameName = args[1];
        
        Game game = gameManager.getGame(gameName);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        game.addSupplyPoint(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "已添加资源点: " + formatLocation(player.getLocation()));
        player.sendMessage(ChatColor.YELLOW + "当前资源点数量: " + game.getSupplyPoints().size());
        
        return true;
    }
    
    private boolean handleHumanClass(CommandSender sender, String[] args) {
        // TODO: 实现求生者职业管理
        sender.sendMessage(ChatColor.YELLOW + "求生者职业管理功能开发中...");
        return true;
    }
    
    private boolean handleKillerClass(CommandSender sender, String[] args) {
        // TODO: 实现杀手职业管理
        sender.sendMessage(ChatColor.YELLOW + "杀手职业管理功能开发中...");
        return true;
    }
    
    private boolean handleSupply(CommandSender sender, String[] args) {
        // TODO: 实现资源管理
        sender.sendMessage(ChatColor.YELLOW + "资源管理功能开发中...");
        return true;
    }
    
    private boolean handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("thehunt.force")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt start <游戏名>");
            return true;
        }
        
        String gameName = args[1];
        Game game = gameManager.getGame(gameName);
        
        if (game == null) {
            sender.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        if (game.start()) {
            sender.sendMessage(ChatColor.GREEN + "游戏 '" + gameName + "' 已强制开始。");
        } else {
            sender.sendMessage(ChatColor.RED + "无法开始游戏。请检查游戏配置是否完整。");
        }
        
        return true;
    }
    
    private boolean handleStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("thehunt.force")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hunt stop <游戏名>");
            return true;
        }
        
        String gameName = args[1];
        Game game = gameManager.getGame(gameName);
        
        if (game == null) {
            sender.sendMessage(ChatColor.RED + "游戏不存在: " + gameName);
            return true;
        }
        
        // TODO: 实现强制结束游戏
        sender.sendMessage(ChatColor.YELLOW + "强制结束游戏功能开发中...");
        
        return true;
    }
    
    private boolean handleSetSign(CommandSender sender, String[] args) {
        // TODO: 实现告示牌设置
        sender.sendMessage(ChatColor.YELLOW + "告示牌设置功能开发中...");
        return true;
    }
    
    private boolean handleSetLeaveSign(CommandSender sender, String[] args) {
        // TODO: 实现离开告示牌设置
        sender.sendMessage(ChatColor.YELLOW + "离开告示牌设置功能开发中...");
        return true;
    }
    
    /**
     * 记录大厅编辑点
     */
    public void setLobbyPoint(Player player, Location location, boolean isFirst) {
        UUID uuid = player.getUniqueId();
        
        if (isFirst) {
            lobbyPos1.put(uuid, location);
            player.sendMessage(ChatColor.GREEN + "第一个点已设置: " + formatLocation(location));
        } else {
            lobbyPos2.put(uuid, location);
            player.sendMessage(ChatColor.GREEN + "第二个点已设置: " + formatLocation(location));
            player.sendMessage(ChatColor.YELLOW + "现在可以使用 /hunt setlobby 确认大厅区域。");
        }
    }
    
    /**
     * 记录游戏场地编辑点
     */
    public void setArenaPoint(Player player, Location location, boolean isFirst) {
        UUID uuid = player.getUniqueId();
        
        if (!editingArena.containsKey(uuid)) {
            return;
        }
        
        String gameName = editingArena.get(uuid);
        
        if (isFirst) {
            arenaPos1.put(uuid, location);
            player.sendMessage(ChatColor.GREEN + "第一个点已设置: " + formatLocation(location));
            player.sendMessage(ChatColor.YELLOW + "游戏: " + gameName);
        } else {
            arenaPos2.put(uuid, location);
            player.sendMessage(ChatColor.GREEN + "第二个点已设置: " + formatLocation(location));
            player.sendMessage(ChatColor.YELLOW + "现在可以使用 /hunt setarena " + gameName + " 确认游戏场地。");
        }
    }
    
    /**
     * 格式化位置信息
     */
    private String formatLocation(Location location) {
        return String.format("(%d, %d, %d)", 
            (int) location.getX(), 
            (int) location.getY(), 
            (int) location.getZ());
    }
}