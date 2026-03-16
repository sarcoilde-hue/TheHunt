# StupidCheck 插件

一个有趣的 Minecraft Bukkit/Spigot 插件，要求玩家在进入游戏后必须在限定时间内发送特定消息，否则会被杀死。

## 功能特点

- 玩家加入游戏后开始倒计时
- 必须在限定时间内发送指定消息才能存活
- 支持广播死亡信息
- 支持权限豁免
- 完全可配置的消息和时间限制
- 管理员命令查看状态

## 安装方法

1. 将编译后的 `.jar` 文件放入服务器的 `plugins` 文件夹
2. 重启服务器或使用 `/reload` 命令
3. 插件会自动生成配置文件

## 配置说明

配置文件位于 `plugins/StupidCheck/config.yml`：

```yaml
# 玩家必须发送的消息内容
required-message: "我是傻逼"

# 时间限制（秒）
time-limit-seconds: 10

# 是否广播玩家被杀死的信息
broadcast-death: true

# 广播消息
broadcast-message: "&c玩家 &6{player} &c因为10秒内没有发送'我是傻逼'而被处决了！"

# 玩家死亡时看到的个人消息
death-message: "&c你因为10秒内没有发送'我是傻逼'而被处决了！"

# 玩家成功发送消息后看到的提示
success-message: "&a验证通过！你可以继续游戏了。"

# 倒计时警告消息
warning-message: "&e你还有 &6{time} &e秒时间发送'我是傻逼'！"

# 玩家已经通过验证后再次发送消息的提示
already-passed-message: "&a你已经通过验证了，无需重复发送。"

# 豁免权限节点
exempt-permission: "stupidcheck.bypass"
```

## 权限节点

- `stupidcheck.bypass` - 拥有此权限的玩家不受检查
- `stupidcheck.reload` - 允许重载配置文件
- `stupidcheck.admin` - 管理员权限（包含所有功能）

## 命令列表

- `/stupidcheck` 或 `/sc` - 显示帮助信息
- `/stupidcheck reload` - 重载配置文件（需要 `stupidcheck.reload` 权限）
- `/stupidcheck status` - 查看插件状态（需要 `stupidcheck.admin` 权限）
- `/stupidcheck check <玩家名>` - 检查指定玩家状态（需要 `stupidcheck.admin` 权限）

## 使用示例

1. 玩家加入服务器后，会收到提示："你还有 10 秒时间发送'我是傻逼'！"
2. 玩家必须在 10 秒内发送聊天消息："我是傻逼"
3. 如果发送正确，玩家会收到："验证通过！你可以继续游戏了。"
4. 如果超时未发送，玩家会被杀死，并广播死亡信息

## 注意事项

- 插件使用异步聊天事件，不会影响服务器性能
- 检查任务每秒运行一次，性能开销很小
- 拥有 `stupidcheck.bypass` 权限的玩家（如管理员）不受影响
- 插件重载时会为当前在线玩家重新初始化检查

## 编译说明

这是一个 ScriptIrc 插件项目，需要使用 ScriptIrc 编译器进行编译：

1. 导出项目为 `.sirc` 文件
2. 放入 `plugins/ScriptIrc/scripts/src/` 目录
3. 执行 `/scriptirc compiler StupidCheck`

## 版本信息

- 当前版本：1.0.0
- 支持 Minecraft：1.21.x
- 依赖：Bukkit/Spigot API

## 问题反馈

如果遇到任何问题，请检查服务器日志中的错误信息，或联系插件开发者。