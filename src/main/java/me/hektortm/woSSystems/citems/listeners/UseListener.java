package me.hektortm.woSSystems.citems.listeners;

import me.hektortm.woSSystems.citems.core.DataManager;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class UseListener implements Listener {

    private final NamespacedKey unusableKey;
    private final DataManager data;

    public UseListener(DataManager data) {
        unusableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "unusable");
        this.data = data;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        Action action = e.getAction();
        Player p = e.getPlayer();

        switch (action) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                data.rightClickAction(p);
                break;
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                data.leftClickAction(p);
                break;
        }


        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.has(unusableKey, PersistentDataType.BOOLEAN)) {
                e.setCancelled(true); // Prevent the item from being dropped
                e.getPlayer().sendMessage("§bDEBUG: §cYou cannot Use this item.");
            }
        }
    }

}
