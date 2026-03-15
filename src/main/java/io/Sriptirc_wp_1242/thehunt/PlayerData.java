package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.UUID;

public class PlayerData {
    
    private final UUID playerId;
    private PlayerRole role = PlayerRole.NONE;
    private boolean isAlive = true;
    private int kills = 0;
    private int assists = 0;
    private int damageDealt = 0;
    private int survivalTime = 0;
    
    // 保存玩家原始状态
    private ItemStack[] originalInventory;
    private ItemStack[] originalArmor;
    private Collection<PotionEffect> originalEffects;
    private double originalHealth;
    private int originalFoodLevel;
    private float originalSaturation;
    private String originalDisplayName;
    private boolean originalAllowFlight;
    private boolean originalFlying;
    private GameMode originalGameMode;
    
    public PlayerData(UUID playerId) {
        this.playerId = playerId;
    }
    
    public void saveOriginalState(Player player) {
        this.originalInventory = player.getInventory().getContents();
        this.originalArmor = player.getInventory().getArmorContents();
        this.originalEffects = player.getActivePotionEffects();
        this.originalHealth = player.getHealth();
        this.originalFoodLevel = player.getFoodLevel();
        this.originalSaturation = player.getSaturation();
        this.originalDisplayName = player.getDisplayName();
        this.originalAllowFlight = player.getAllowFlight();
        this.originalFlying = player.isFlying();
        this.originalGameMode = player.getGameMode();
    }
    
    public void restoreOriginalState(Player player) {
        if (originalInventory != null) {
            player.getInventory().setContents(originalInventory);
        }
        if (originalArmor != null) {
            player.getInventory().setArmorContents(originalArmor);
        }
        
        // 清除所有药水效果
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // 恢复原始药水效果
        if (originalEffects != null) {
            for (PotionEffect effect : originalEffects) {
                player.addPotionEffect(effect);
            }
        }
        
        player.setHealth(Math.min(originalHealth, player.getMaxHealth()));
        player.setFoodLevel(originalFoodLevel);
        player.setSaturation(originalSaturation);
        player.setDisplayName(originalDisplayName);
        player.setAllowFlight(originalAllowFlight);
        player.setFlying(originalFlying);
        player.setGameMode(originalGameMode);
        
        // 重置玩家状态
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setGlowing(false);
    }
    
    public void applyGameSettings(Player player, PlayerRole role) {
        // 清空背包
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        // 清除所有药水效果
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // 根据角色应用设置
        if (role == PlayerRole.KILLER) {
            // 杀手设置
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            player.setHealth(40.0); // 杀手有更多生命值
        } else if (role == PlayerRole.SURVIVOR) {
            // 求生者设置
            player.setHealth(20.0);
        }
        
        // 通用设置
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        
        // 禁止饥饿和自然回血
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
    }
    
    // Getter 和 Setter 方法
    public UUID getPlayerId() { return playerId; }
    public PlayerRole getRole() { return role; }
    public void setRole(PlayerRole role) { this.role = role; }
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }
    public int getKills() { return kills; }
    public void addKill() { kills++; }
    public int getAssists() { return assists; }
    public void addAssist() { assists++; }
    public int getDamageDealt() { return damageDealt; }
    public void addDamageDealt(int damage) { damageDealt += damage; }
    public int getSurvivalTime() { return survivalTime; }
    public void addSurvivalTime(int seconds) { survivalTime += seconds; }
    
    // 枚举 GameMode 的兼容性处理
    public enum GameMode {
        SURVIVAL,
        CREATIVE,
        ADVENTURE,
        SPECTATOR
    }
}