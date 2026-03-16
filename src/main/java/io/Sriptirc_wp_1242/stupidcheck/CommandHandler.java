package io.Sriptirc_wp_1242.stupidcheck;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final Stupidcheck plugin;
    private final ConfigManager configManager;
    private final PlayerManager playerManager;
    
    public CommandHandler(Stupidcheck plugin, ConfigManager configManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerManager = playerManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "status":
                return handleStatus(sender);
            case "check":
                return handleCheck(sender, args);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("stupidcheck.reload") && !sender.hasPermission("stupidcheck.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }
        
        configManager.reload();
        sender.sendMessage(ChatColor.GREEN + "配置文件已重载！");
        plugin.getLogger().info("配置文件已由 " + sender.getName() + " 重载");
        return true;
    }
    
    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("stupidcheck.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== StupidCheck 插件状态 ===");
        sender.sendMessage(ChatColor.YELLOW + "版本: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "要求消息: " + ChatColor.WHITE + configManager.getRequiredMessage());
        sender.sendMessage(ChatColor.YELLOW + "时间限制: " + ChatColor.WHITE + configManager.getTimeLimit() + " 秒");
        sender.sendMessage(ChatColor.YELLOW + "广播死亡: " + ChatColor.WHITE + (configManager.isBroadcastDeath() ? "是" : "否"));
        sender.sendMessage(ChatColor.YELLOW + "豁免权限: " + ChatColor.WHITE + configManager.getExemptPermission());
        
        // 显示在线玩家状态
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (playerManager.isPlayerTiming(player)) {
                int remaining = configManager.getTimeLimit() - playerManager.getRemainingTime(player);
                sender.sendMessage(ChatColor.YELLOW + "你的状态: " + ChatColor.RED + "未通过 (" + remaining + "秒剩余)");
            } else if (playerManager.hasPlayerPassed(player)) {
                sender.sendMessage(ChatColor.YELLOW + "你的状态: " + ChatColor.GREEN + "已通过");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "你的状态: " + ChatColor.GRAY + "未检查");
            }
        }
        
        return true;
    }
    
    private boolean handleCheck(CommandSender sender, String[] args) {
        if (!sender.hasPermission("stupidcheck.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /stupidcheck check <玩家名>");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + args[1] + " 不在线！");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== 玩家 " + target.getName() + " 状态 ===");
        sender.sendMessage(ChatColor.YELLOW + "豁免权限: " + ChatColor.WHITE + 
                (target.hasPermission(configManager.getExemptPermission()) ? "是" : "否"));
        
        if (playerManager.isPlayerTiming(target)) {
            int elapsed = playerManager.getRemainingTime(target);
            int remaining = configManager.getTimeLimit() - elapsed;
            sender.sendMessage(ChatColor.YELLOW + "验证状态: " + ChatColor.RED + "未通过");
            sender.sendMessage(ChatColor.YELLOW + "已过时间: " + ChatColor.WHITE + elapsed + " 秒");
            sender.sendMessage(ChatColor.YELLOW + "剩余时间: " + ChatColor.WHITE + remaining + " 秒");
        } else if (playerManager.hasPlayerPassed(target)) {
            sender.sendMessage(ChatColor.YELLOW + "验证状态: " + ChatColor.GREEN + "已通过");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "验证状态: " + ChatColor.GRAY + "未检查");
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== StupidCheck 插件帮助 ===");
        sender.sendMessage(ChatColor.YELLOW + "插件功能: 玩家必须在 " + configManager.getTimeLimit() + 
                " 秒内发送 '" + configManager.getRequiredMessage() + "' 才能存活");
        sender.sendMessage(ChatColor.GRAY + "命令列表:");
        sender.sendMessage(ChatColor.WHITE + "/stupidcheck help" + ChatColor.GRAY + " - 显示此帮助");
        
        if (sender.hasPermission("stupidcheck.reload") || sender.hasPermission("stupidcheck.admin")) {
            sender.sendMessage(ChatColor.WHITE + "/stupidcheck reload" + ChatColor.GRAY + " - 重载配置文件");
        }
        
        if (sender.hasPermission("stupidcheck.admin")) {
            sender.sendMessage(ChatColor.WHITE + "/stupidcheck status" + ChatColor.GRAY + " - 查看插件状态");
            sender.sendMessage(ChatColor.WHITE + "/stupidcheck check <玩家>" + ChatColor.GRAY + " - 检查玩家状态");
        }
        
        sender.sendMessage(ChatColor.GRAY + "权限节点:");
        sender.sendMessage(ChatColor.WHITE + "stupidcheck.bypass" + ChatColor.GRAY + " - 豁免检查");
        sender.sendMessage(ChatColor.WHITE + "stupidcheck.reload" + ChatColor.GRAY + " - 重载权限");
        sender.sendMessage(ChatColor.WHITE + "stupidcheck.admin" + ChatColor.GRAY + " - 管理员权限");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("help", "status"));
            
            if (sender.hasPermission("stupidcheck.reload") || sender.hasPermission("stupidcheck.admin")) {
                completions.add("reload");
            }
            
            if (sender.hasPermission("stupidcheck.admin")) {
                completions.add("check");
            }
            
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("check") && sender.hasPermission("stupidcheck.admin")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
        }
        
        return Collections.emptyList();
    }
}