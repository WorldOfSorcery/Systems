package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DropListener implements Listener {
    private final NamespacedKey undroppableKey;

    public DropListener() {
        undroppableKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "undroppable");
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (Boolean.TRUE.equals(data.get(undroppableKey, PersistentDataType.BOOLEAN))) {
                event.setCancelled(true); // Prevent the item from being dropped
            } else {
                return;
            }
        } else {
            return;
        }
    }

}
