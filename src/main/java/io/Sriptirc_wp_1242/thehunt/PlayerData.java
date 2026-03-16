package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * 玩家数据管理
 */
public class PlayerData {
    
    private static final Map<UUID, Location> savedLocations = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private static final Map<UUID, Collection<PotionEffect>> savedEffects = new HashMap<>();
    private static final Map<UUID, Integer> savedFoodLevel = new HashMap<>();
    private static final Map<UUID, Double> savedHealth = new HashMap<>();
    private static final Map<UUID, Integer> savedExp = new HashMap<>();
    private static final Map<UUID, Float> savedExpToLevel = new HashMap<>();
    
    /**
     * 保存玩家状态
     */
    public static void saveState(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 保存位置
        savedLocations.put(uuid, player.getLocation());
        
        // 保存物品栏
        PlayerInventory inventory = player.getInventory();
        savedInventories.put(uuid, inventory.getContents().clone());
        savedArmor.put(uuid, inventory.getArmorContents().clone());
        
        // 保存药水效果
        savedEffects.put(uuid, new ArrayList<>(player.getActivePotionEffects()));
        
        // 保存其他状态
        savedFoodLevel.put(uuid, player.getFoodLevel());
        savedHealth.put(uuid, player.getHealth());
        savedExp.put(uuid, player.getTotalExperience());
        savedExpToLevel.put(uuid, player.getExp());
        
        // 清除玩家状态
        clearPlayerState(player);
    }
    
    /**
     * 恢复玩家状态
     */
    public static void restoreState(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 恢复位置
        if (savedLocations.containsKey(uuid)) {
            player.teleport(savedLocations.get(uuid));
            savedLocations.remove(uuid);
        }
        
        // 恢复物品栏
        if (savedInventories.containsKey(uuid)) {
            player.getInventory().setContents(savedInventories.get(uuid));
            savedInventories.remove(uuid);
        }
        
        if (savedArmor.containsKey(uuid)) {
            player.getInventory().setArmorContents(savedArmor.get(uuid));
            savedArmor.remove(uuid);
        }
        
        // 清除当前效果
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType()));
        
        // 恢复药水效果
        if (savedEffects.containsKey(uuid)) {
            for (PotionEffect effect : savedEffects.get(uuid)) {
                player.addPotionEffect(effect);
            }
            savedEffects.remove(uuid);
        }
        
        // 恢复其他状态
        if (savedFoodLevel.containsKey(uuid)) {
            player.setFoodLevel(savedFoodLevel.get(uuid));
            savedFoodLevel.remove(uuid);
        }
        
        if (savedHealth.containsKey(uuid)) {
            player.setHealth(savedHealth.get(uuid));
            savedHealth.remove(uuid);
        }
        
        if (savedExp.containsKey(uuid)) {
            player.setTotalExperience(savedExp.get(uuid));
            savedExp.remove(uuid);
        }
        
        if (savedExpToLevel.containsKey(uuid)) {
            player.setExp(savedExpToLevel.get(uuid));
            savedExpToLevel.remove(uuid);
        }
        
        // 恢复游戏模式
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
    }
    
    /**
     * 仅保存位置（用于加入游戏时）
     */
    public static void saveLocation(Player player) {
        savedLocations.put(player.getUniqueId(), player.getLocation());
    }
    
    /**
     * 仅恢复位置（用于离开游戏时）
     */
    public static void restoreLocation(Player player) {
        UUID uuid = player.getUniqueId();
        if (savedLocations.containsKey(uuid)) {
            player.teleport(savedLocations.get(uuid));
            savedLocations.remove(uuid);
        }
    }
    
    /**
     * 清除玩家状态（用于进入游戏时）
     */
    private static void clearPlayerState(Player player) {
        // 清空物品栏
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        
        // 清除所有药水效果
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType()));
        
        // 重置状态
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setFireTicks(0);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
        
        // 设置游戏模式
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
    }
    
    /**
     * 保存玩家物品栏到配置（用于保存职业）
     */
    public static Map<String, Object> saveInventoryToConfig(Player player) {
        Map<String, Object> config = new HashMap<>();
        PlayerInventory inventory = player.getInventory();
        
        // 保存主手物品栏
        ItemStack[] contents = inventory.getContents();
        List<Map<String, Object>> items = new ArrayList<>();
        
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("slot", i);
                itemData.put("item", item.serialize());
                items.add(itemData);
            }
        }
        
        config.put("inventory", items);
        
        // 保存盔甲
        ItemStack[] armor = inventory.getArmorContents();
        List<Map<String, Object>> armorItems = new ArrayList<>();
        
        String[] armorSlots = {"helmet", "chestplate", "leggings", "boots"};
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("slot", armorSlots[i]);
                itemData.put("item", item.serialize());
                armorItems.add(itemData);
            }
        }
        
        config.put("armor", armorItems);
        
        // 保存药水效果
        List<Map<String, Object>> effects = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            Map<String, Object> effectData = new HashMap<>();
            effectData.put("type", effect.getType().getName());
            effectData.put("duration", effect.getDuration());
            effectData.put("amplifier", effect.getAmplifier());
            effectData.put("ambient", effect.isAmbient());
            effectData.put("particles", effect.hasParticles());
            effectData.put("icon", effect.hasIcon());
            effects.add(effectData);
        }
        
        config.put("effects", effects);
        
        // 保存其他属性
        config.put("max-health", player.getMaxHealth());
        config.put("health", player.getHealth());
        config.put("food-level", player.getFoodLevel());
        
        return config;
    }
    
    /**
     * 从配置加载物品栏到玩家（用于应用职业）
     */
    public static void loadInventoryFromConfig(Player player, Map<String, Object> config) {
        if (config == null) return;
        
        // 清空当前物品栏
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        
        // 加载主手物品栏
        if (config.containsKey("inventory")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) config.get("inventory");
            for (Map<String, Object> itemData : items) {
                int slot = (int) itemData.get("slot");
                Map<String, Object> itemMap = (Map<String, Object>) itemData.get("item");
                ItemStack item = ItemStack.deserialize(itemMap);
                player.getInventory().setItem(slot, item);
            }
        }
        
        // 加载盔甲
        if (config.containsKey("armor")) {
            List<Map<String, Object>> armorItems = (List<Map<String, Object>>) config.get("armor");
            ItemStack[] armor = new ItemStack[4];
            
            for (Map<String, Object> itemData : armorItems) {
                String slot = (String) itemData.get("slot");
                Map<String, Object> itemMap = (Map<String, Object>) itemData.get("item");
                ItemStack item = ItemStack.deserialize(itemMap);
                
                switch (slot) {
                    case "helmet":
                        armor[3] = item;
                        break;
                    case "chestplate":
                        armor[2] = item;
                        break;
                    case "leggings":
                        armor[1] = item;
                        break;
                    case "boots":
                        armor[0] = item;
                        break;
                }
            }
            
            player.getInventory().setArmorContents(armor);
        }
        
        // 清除当前效果
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType()));
        
        // 加载药水效果
        if (config.containsKey("effects")) {
            List<Map<String, Object>> effects = (List<Map<String, Object>>) config.get("effects");
            for (Map<String, Object> effectData : effects) {
                // TODO: 需要将字符串转换为PotionEffectType
                // 这里简化处理，实际需要完整实现
            }
        }
        
        // 加载其他属性
        if (config.containsKey("max-health")) {
            player.setMaxHealth(((Number) config.get("max-health")).doubleValue());
        }
        
        if (config.containsKey("health")) {
            player.setHealth(((Number) config.get("health")).doubleValue());
        }
        
        if (config.containsKey("food-level")) {
            player.setFoodLevel((int) config.get("food-level"));
        }
    }
    
    /**
     * 检查玩家是否有保存的状态
     */
    public static boolean hasSavedState(UUID uuid) {
        return savedLocations.containsKey(uuid) || 
               savedInventories.containsKey(uuid) ||
               savedArmor.containsKey(uuid);
    }
    
    /**
     * 清理玩家数据（插件禁用时）
     */
    public static void cleanup() {
        savedLocations.clear();
        savedInventories.clear();
        savedArmor.clear();
        savedEffects.clear();
        savedFoodLevel.clear();
        savedHealth.clear();
        savedExp.clear();
        savedExpToLevel.clear();
    }
}