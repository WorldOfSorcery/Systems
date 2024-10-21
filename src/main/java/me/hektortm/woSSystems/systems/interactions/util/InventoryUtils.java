package me.hektortm.woSSystems.systems.interactions.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InventoryUtils {

    // Create an item with metadata
    public static ItemStack createItem(Material material, String name, List<String> lore, boolean enchanted, boolean unbreakable, int durability, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }
            if (lore != null) {
                meta.setLore(lore);
            }
            meta.setUnbreakable(unbreakable);
            if (enchanted) {
                meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);  // Example enchantment
            }
            item.setItemMeta(meta);
            item.setDurability((short) durability);
        }

        return item;
    }
}