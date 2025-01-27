package me.hektortm.woSSystems.utils;

import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ErrorHandler {

    public boolean handleCitemErrors(ItemStack item, Player p) {
        ItemMeta meta = item.getItemMeta();

        if (item == null || item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return false;
        }

        if (meta == null) {
            Utils.error(p, "citems", "error.no-meta");
            return false;
        }
        return true;
    }



}
