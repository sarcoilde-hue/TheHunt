# The Hunt - 非对称对抗游戏插件

一个基于 Bukkit/Spigot 的非对称对抗游戏插件，玩家分为杀手和求生者两个阵营进行猎杀游戏。

## 功能特性

### 核心玩法
- **非对称对抗**：杀手 vs 求生者
- **多游戏支持**：可创建多个不同配置的游戏
- **大厅系统**：统一的等待区域
- **游戏场地**：每个游戏独立的活动范围
- **职业系统**：可自定义的杀手和求生者职业
- **资源系统**：定时刷新的游戏资源

### 游戏阶段
1. **等待阶段**：玩家在大厅等待游戏开始
2. **准备阶段**（60秒）：求生者躲藏，杀手在等待区域
3. **猎杀阶段**：杀手猎杀求生者，求生者生存或反击
4. **结束阶段**：宣布获胜者并发放奖励

### 获胜条件
- **求生者获胜**：
  - 成功生存300秒（可配置）
  - 击杀所有杀手
- **杀手获胜**：
  - 在限定时间内杀光所有求生者

## 安装要求

- Minecraft 服务器：1.16.5 - 1.21.x（主要支持 1.21.x）
- 服务端核心：Bukkit / Spigot / Paper
- 可选依赖：Vault（用于经济奖励）

## 快速开始

### 1. 基础设置
```bash
# 创建大厅
/hunt editlobby          # 获得烈焰棒划定大厅区域
/hunt setlobby           # 确认大厅设置（当前位置为出生点）

# 创建游戏
/hunt create <游戏名> <杀手数> <求生者数> <最大玩家数>
# 示例：/hunt create forest_hunt 2 8 16

# 设置游戏场地
/hunt editarena <游戏名>  # 获得木棒划定游戏场地
/hunt setarena <游戏名>   # 确认游戏场地
```

### 2. 设置出生点
```bash
# 设置求生者出生点（可设置多个）
/hunt sethumanspawn <游戏名>

# 设置杀手出生点（可设置多个）
/hunt setkillerspawn <游戏名>

# 设置杀手等待点（只能设置1个）
/hunt setkillerwaiting <游戏名>
```

### 3. 设置资源点（可选）
```bash
# 设置资源刷新点
/hunt setsupply <游戏名>
```

### 4. 玩家加入
```bash
# 玩家加入游戏
/hunt join <游戏名>

# 玩家退出游戏
/hunt leave <游戏名>

# 观战游戏
/hunt spectate <游戏名>
```

## 命令参考

### 玩家命令
| 命令 | 权限 | 描述 |
|------|------|------|
| `/hunt join <游戏名>` | `thehunt.join` | 加入游戏 |
| `/hunt leave <游戏名>` | `thehunt.leave` | 退出游戏 |
| `/hunt spectate <游戏名>` | `thehunt.spectate` | 观战游戏 |
| `/hunt class` | `thehunt.class` | 选择职业 |

### 管理员命令
| 命令 | 权限 | 描述 |
|------|------|------|
| `/hunt reload` | `thehunt.reload` | 重载插件配置 |
| `/hunt editlobby` | `thehunt.edit` | 编辑大厅区域 |
| `/hunt setlobby` | `thehunt.edit` | 设置大厅 |
| `/hunt create <名> <杀> <求> <最大>` | `thehunt.create` | 创建新游戏 |
| `/hunt editarena <游戏名>` | `thehunt.edit` | 编辑游戏场地 |
| `/hunt setarena <游戏名>` | `thehunt.edit` | 设置游戏场地 |
| `/hunt sethumanspawn <游戏名>` | `thehunt.edit` | 设置求生者出生点 |
| `/hunt setkillerspawn <游戏名>` | `thehunt.edit` | 设置杀手出生点 |
| `/hunt setkillerwaiting <游戏名>` | `thehunt.edit` | 设置杀手等待点 |
| `/hunt setsupply <游戏名>` | `thehunt.edit` | 设置资源点 |
| `/hunt start <游戏名>` | `thehunt.force` | 强制开始游戏 |
| `/hunt stop <游戏名>` | `thehunt.force` | 强制结束游戏 |

## 权限节点

| 权限节点 | 默认 | 描述 |
|----------|------|------|
| `thehunt.join` | true | 加入游戏 |
| `thehunt.leave` | true | 退出游戏 |
| `thehunt.spectate` | true | 观战游戏 |
| `thehunt.class` | true | 选择职业 |
| `thehunt.admin` | op | 管理员权限 |
| `thehunt.reload` | op | 重载插件 |
| `thehunt.create` | op | 创建游戏 |
| `thehunt.edit` | op | 编辑游戏配置 |
| `thehunt.force` | op | 强制操作 |

## 配置文件

### 主配置文件 (`config.yml`)
```yaml
# 游戏大厅配置
lobby:
  wait-time: 60          # 大厅等待时间（秒）
  min-players: 2         # 最小玩家数
  max-players: 16        # 最大玩家数

# 游戏全局配置
game:
  survivor-win-time: 300 # 求生者获胜时间（秒）
  preparation-time: 60   # 准备阶段时间（秒）
  lives: 1               # 玩家生命数
  
  hunting:
    supply-spawn-interval: 60    # 资源刷新间隔
    outline-interval: 60         # 轮廓显示间隔
    outline-duration: 10         # 轮廓显示时间
    outline-survivors: true      # 显示求生者轮廓
    outline-killers: true        # 显示杀手轮廓
    outline-enabled: true        # 轮廓显示开关

# 计分系统
scoring:
  survivor-per-minute: 1 # 求生者每生存60秒得分
  killer-per-kill: 5     # 杀手每击杀得分

# 奖励系统
rewards:
  money-multiplier: 100  # 金钱奖励倍数
  item-rewards:          # 道具奖励
    tier1: {score: 50, items: ["DIAMOND:10"]}
    tier2: {score: 30, items: ["GOLD_INGOT:10"]}
    # ... 更多等级
```

### 游戏配置文件
每个游戏在 `plugins/TheHunt/games/<游戏名>/` 目录下有3个配置文件：
1. `config.yml` - 游戏基本设置（区域、出生点等）
2. `classes.yml` - 职业配置
3. `supplies.yml` - 资源生成配置

## 游戏机制

### 区域限制
- **大厅区域**：玩家走出范围会被传送回出生点
- **游戏场地**：玩家走出范围会直接死亡

### 游戏规则
- 禁止饥饿和自然回血
- 禁止破坏和放置方块
- 禁止同阵营PVP
- 游戏内指令限制（可配置白名单）

### 轮廓显示
每隔60秒显示10秒的玩家轮廓，帮助找到对手（可配置开关和目标）

### 资源系统
- 每隔60秒在资源点生成道具
- 仅求生者可拾取资源
- 支持自定义资源物品

### 职业系统
- 每个游戏可自定义多个职业
- 杀手和求生者职业分开配置
- 支持完整的NBT物品保存
- 支持模组物品

## 开发状态

### 已完成功能
- [x] 基础插件框架
- [x] 游戏管理器
- [x] 命令系统（基础）
- [x] 事件监听器
- [x] 区域编辑工具
- [x] 玩家数据管理
- [x] 配置文件系统

### 待完成功能
- [ ] 职业选择GUI
- [ ] 告示牌系统
- [ ] 计分板显示
- [ ] 轮廓显示效果
- [ ] 资源生成系统
- [ ] 奖励发放系统
- [ ] 观战系统完善
- [ ] 职业管理命令

## 编译和部署

### 使用 ScriptIrc
1. 导出项目为 `.sirc` 文件
2. 放入 `plugins/ScriptIrc/scripts/src/`
3. 执行 `/scriptirc compiler TheHunt`

### 手动编译
```bash
# 需要 Maven 和 Java 开发环境
mvn clean package
# 生成的 jar 文件在 target/ 目录
```

## 问题反馈

如果遇到问题，请检查：
1. 服务器版本是否兼容（1.16.5 - 1.21.x）
2. 是否安装了必要的依赖（Vault）
3. 游戏配置是否完整（区域、出生点等）
4. 查看服务器日志获取错误信息

## 更新日志

### v1.0.0
- 初始版本发布
- 基础游戏框架
- 区域编辑系统
- 基本命令系统
- 事件监听和游戏规则

## 许可证

本项目基于 MIT 许可证开源。