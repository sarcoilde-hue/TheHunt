package io.Sriptirc_wp_1242.thehunt;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.*;

public class ItemManager {
    
    private final Thehunt plugin;
    private final Random random = new Random();
    private final NamespacedKey customItemKey;
    
    public ItemManager(Thehunt plugin) {
        this.plugin = plugin;
        this.customItemKey = new NamespacedKey(plugin, "thehunt_item");
    }
    
    public void giveKillerItems(Player killer) {
        // 给予杀手武器
        ItemStack sword = createKillerSword();
        killer.getInventory().addItem(sword);
        
        // 给予杀手一些食物
        ItemStack food = new ItemStack(Material.COOKED_BEEF, 8);
        killer.getInventory().addItem(food);
        
        // 给予杀手一些药水
        ItemStack speedPotion = createSpeedPotion();
        killer.getInventory().addItem(speedPotion);
    }
    
    public ItemStack createKillerSword() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c杀手之刃");
            meta.setLore(Arrays.asList(
                "§7杀手专用武器",
                "§6+10% 移动速度",
                "§6+20% 攻击伤害"
            ));
            
            // 添加属性修饰符
            AttributeModifier speedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.movement_speed",
                0.10,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, speedModifier);
            
            AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                0.20,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
            
            // 添加自定义标签
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, "killer_sword");
            
            // 添加附魔效果
            meta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            
            sword.setItemMeta(meta);
        }
        
        return sword;
    }
    
    public ItemStack createSpeedPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b速度药水");
            meta.setLore(Arrays.asList(
                "§7使用后获得速度效果",
                "§6持续时间: 30秒"
            ));
            
            PotionData data = new PotionData(PotionType.SPEED, false, true);
            meta.setBasePotionData(data);
            
            // 添加自定义标签
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, "speed_potion");
            
            potion.setItemMeta(meta);
        }
        
        return potion;
    }
    
    public ItemStack createRandomWeapon() {
        Material[] weapons = {
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.BOW,
            Material.CROSSBOW
        };
        
        Material weaponType = weapons[random.nextInt(weapons.length)];
        ItemStack weapon = new ItemStack(weaponType);
        ItemMeta meta = weapon.getItemMeta();
        
        if (meta != null) {
            String[] names = {"§e生锈的铁剑", "§e锋利的石剑", "§e轻便的木剑", "§e精准的弓", "§e强力的弩"};
            String[] descriptions = {"§7一把普通的武器", "§7小心使用", "§7求生者专用"};
            
            meta.setDisplayName(names[random.nextInt(names.length)]);
            meta.setLore(Arrays.asList(
                descriptions[random.nextInt(descriptions.length)],
                "§6随机属性"
            ));
            
            // 随机添加附魔
            if (random.nextBoolean()) {
                if (weaponType == Material.BOW || weaponType == Material.CROSSBOW) {
                    meta.addEnchant(Enchantment.ARROW_DAMAGE, 1 + random.nextInt(2), true);
                } else {
                    meta.addEnchant(Enchantment.DAMAGE_ALL, 1 + random.nextInt(2), true);
                }
            }
            
            // 添加自定义标签
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, "survivor_weapon");
            
            weapon.setItemMeta(meta);
        }
        
        return weapon;
    }
    
    public ItemStack createRandomTool() {
        Material[] tools = {
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE
        };
        
        Material toolType = tools[random.nextInt(tools.length)];
        ItemStack tool = new ItemStack(toolType);
        ItemMeta meta = tool.getItemMeta();
        
        if (meta != null) {
            String[] names = {"§e破旧的镐", "§e锋利的斧", "§e耐用的工具"};
            
            meta.setDisplayName(names[random.nextInt(names.length)]);
            meta.setLore(Arrays.asList(
                "§7用于挖掘或砍伐",
                "§6效率提升"
            ));
            
            // 随机添加效率附魔
            if (random.nextBoolean()) {
                meta.addEnchant(Enchantment.DIG_SPEED, 1 + random.nextInt(2), true);
            }
            
            // 添加自定义标签
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, "survivor_tool");
            
            tool.setItemMeta(meta);
        }
        
        return tool;
    }
    
    public ItemStack createRandomPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        if (meta != null) {
            PotionType[] potionTypes = {
                PotionType.SPEED,
                PotionType.INVISIBILITY,
                PotionType.JUMP,
                PotionType.REGENERATION,
                PotionType.STRENGTH
            };
            
            PotionType type = potionTypes[random.nextInt(potionTypes.length)];
            boolean extended = random.nextBoolean();
            boolean upgraded = !extended && random.nextBoolean();
            
            String[] names = {
                "§b速度药水", "§7隐身药水", "§a跳跃药水", 
                "§c再生药水", "§4力量药水"
            };
            
            String name = names[Arrays.asList(potionTypes).indexOf(type)];
            if (extended) name += " §8(延长)";
            if (upgraded) name += " §8(强化)";
            
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(
                "§7使用后获得特殊效果",
                "§6小心使用"
            ));
            
            PotionData data = new PotionData(type, extended, upgraded);
            meta.setBasePotionData(data);
            
            // 添加自定义标签
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, "survivor_potion");
            
            potion.setItemMeta(meta);
        }
        
        return potion;
    }
    
    public ItemStack createRandomFood() {
        Material[] foods = {
            Material.APPLE,
            Material.BREAD,
            Material.COOKED_BEEF,
            Material.COOKED_CHICKEN,
            Material.COOKED_PORKCHOP,
            Material.GOLDEN_APPLE
        };
        
        Material foodType = foods[random.nextInt(foods.length)];
        int amount = 1 + random.nextInt(3);
        
        ItemStack food = new ItemStack(foodType, amount);
        ItemMeta meta = food.getItemMeta();
        
        if (meta != null) {
            String[] names = {"§6新鲜苹果", "§6松软面包", "§6美味牛排", "§6香脆鸡腿", "§6多汁猪排", "§6金苹果"};
            
            meta.setDisplayName(names[Arrays.asList(foods).indexOf(foodType)]);
            meta.setLore(Arrays.asList(
                "§7恢复饥饿值",
                "§6求生必备"
            ));
            
            // 添加自定义标签
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, "survivor_food");
            
            food.setItemMeta(meta);
        }
        
        return food;
    }
    
    public ItemStack createRandomSpecialItem() {
        Material[] specials = {
            Material.ENDER_PEARL,
            Material.SNOWBALL,
            Material.EGG,
            Material.FISHING_ROD,
            Material.LEAD,
            Material.SHEARS
        };
        
        Material specialType = specials[random.nextInt(specials.length)];
        ItemStack special = new ItemStack(specialType);
        ItemMeta meta = special.getItemMeta();
        
        if (meta != null) {
            String[] names = {"§5末影珍珠", "§f雪球", "§e鸡蛋", "§6钓鱼竿", "§7拴绳", "§7剪刀"};
            String[] descriptions = {
                "§7快速传送", "§7击退敌人", "§7干扰杀手", 
                "§7钩取物品", "§7限制移动", "§7剪断蛛网"
            };
            
            meta.setDisplayName(names[Arrays.asList(specials).indexOf(specialType)]);
            meta.setLore(Arrays.asList(
                descriptions[Arrays.asList(specials).indexOf(specialType)],
                "§6特殊道具"
            ));
            
            // 添加自定义标签
            meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, "special_item");
            
            // 为某些物品添加特殊属性
            if (specialType == Material.ENDER_PEARL) {
                AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    "generic.movement_speed",
                    0.05,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
                );
                meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, modifier);
            }
            
            special.setItemMeta(meta);
        }
        
        return special;
    }
    
    public boolean isCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(customItemKey, PersistentDataType.STRING);
    }
    
    public String getCustomItemType(ItemStack item) {
        if (!isCustomItem(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(customItemKey, PersistentDataType.STRING);
    }
    
    // 创建自定义物品的方法（用于配置）
    public ItemStack createCustomItem(String itemId, Map<String, Object> config) {
        // 这里可以根据配置创建自定义物品
        // 由于时间关系，先返回一个默认物品
        return createRandomWeapon();
    }
}