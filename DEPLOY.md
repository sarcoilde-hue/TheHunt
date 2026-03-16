# The Hunt 插件部署说明

## ScriptIrc 编译步骤

### 1. 导出项目
- 在ScriptIrc界面中点击「导出项目」
- 保存为 `TheHunt.sirc` 文件

### 2. 上传到服务器
```bash
# 将 TheHunt.sirc 文件上传到服务器
# 放置到以下目录：
plugins/ScriptIrc/scripts/src/TheHunt.sirc
```

### 3. 编译插件
```bash
# 在游戏内或控制台执行
/scriptirc compiler TheHunt
```

### 4. 启用插件
```bash
# 编译成功后，插件会自动加载
# 或者手动重载
/reload
```

## 项目文件结构

```
TheHunt/
├── project.sirc          # ScriptIrc项目配置
├── README.md            # 使用说明文档
├── DEPLOY.md           # 部署说明（本文件）
└── src/
    └── main/
        ├── java/io/Sriptirc_wp_1242/thehunt/
        │   ├── Thehunt.java        # 主类
        │   ├── GameManager.java    # 游戏管理器
        │   ├── Game.java          # 游戏实例
        │   ├── CommandHandler.java # 命令处理器
        │   ├── EventListener.java  # 事件监听器
        │   └── PlayerData.java    # 玩家数据管理
        └── resources/
            ├── plugin.yml         # 插件描述文件
            └── config.yml        # 主配置文件
```

## 快速测试

### 1. 基础设置
```bash
# 设置大厅
/hunt editlobby
# 使用烈焰棒左键右键划定区域
/hunt setlobby

# 创建游戏
/hunt create test_game 1 4 8

# 设置游戏场地
/hunt editarena test_game
# 使用木棒左键右键划定区域
/hunt setarena test_game

# 设置出生点
/hunt sethumanspawn test_game
/hunt setkillerspawn test_game
/hunt setkillerwaiting test_game
```

### 2. 玩家测试
```bash
# 玩家加入
/hunt join test_game

# 强制开始（测试用）
/hunt start test_game
```

## 注意事项

1. **版本兼容**：插件主要支持 1.21.x，向下兼容至 1.16.5
2. **依赖插件**：需要 Vault 插件支持经济奖励功能
3. **数据存储**：游戏数据保存在 `plugins/TheHunt/games/` 目录
4. **配置重载**：使用 `/hunt reload` 重载配置，不会中断进行中的游戏

## 故障排除

### 编译错误
- 检查Java版本是否为17或更高
- 确保ScriptIrc插件版本支持Java 17
- 查看服务器日志获取详细错误信息

### 运行错误
- 检查Vault插件是否已安装（如需经济功能）
- 确认游戏配置完整（区域、出生点等）
- 查看 `plugins/TheHunt/` 目录权限

### 功能异常
- 使用 `/hunt reload` 重载配置
- 检查 `config.yml` 文件格式
- 查看游戏配置文件是否完整