package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class BackpackListener implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    @EventHandler
    public void onRightClickShulker(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.getType().toString().endsWith("SHULKER_BOX")) return;

        event.setCancelled(true); // Prevent placement

        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (!(meta.getBlockState() instanceof org.bukkit.block.ShulkerBox shulkerBox)) return;

        Inventory shulkerInventory = Bukkit.createInventory(event.getPlayer(), InventoryType.SHULKER_BOX, item.getItemMeta().getDisplayName());
        shulkerInventory.setContents(shulkerBox.getInventory().getContents());

        player.openInventory(shulkerInventory);
        player.playSound(player, Sound.ENTITY_HORSE_SADDLE, 1, 1);
        player.setMetadata("shulker_box", new FixedMetadataValue(plugin, item));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().hasMetadata("shulker_box")) return;

        ItemStack item = (ItemStack) event.getPlayer().getMetadata("shulker_box").get(0).value();
        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (!(meta.getBlockState() instanceof org.bukkit.block.ShulkerBox shulkerBox)) return;

        shulkerBox.getInventory().setContents(event.getInventory().getContents());
        meta.setBlockState(shulkerBox);
        item.setItemMeta(meta);
        event.getPlayer().removeMetadata("shulker_box", plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType().toString().endsWith("SHULKER_BOX")) {
            event.setCancelled(true); // Prevent placement of shulker boxes
        }
    }

}
