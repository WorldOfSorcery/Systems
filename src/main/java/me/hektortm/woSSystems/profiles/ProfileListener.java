package me.hektortm.woSSystems.profiles;

import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ProfileListener implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ProfileManager manager = plugin.getProfileManager();

    @EventHandler
    public void onProfileClose(InventoryCloseEvent e) {
        // Ensure the event is triggered by a player
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();

        // Check if the closed inventory is the profile editor
        if (inv.equals(manager.getEditProfile())) {
            // Initialize variables for background and picture data
            String backgroundUni = null;
            String pictureUni = null;
            String pictureID = null;

            // Check and process the background item (slot 7)
            ItemStack backgroundItem = inv.getItem(7);
            if (backgroundItem != null && backgroundItem.hasItemMeta()) {
                ItemMeta backgroundMeta = backgroundItem.getItemMeta();
                PersistentDataContainer backgroundData = backgroundMeta.getPersistentDataContainer();

                // Retrieve the background unique identifier
                if (backgroundData.has(plugin.getCitemManager().getProfileBgKey(), PersistentDataType.STRING)) {
                    backgroundUni = backgroundData.get(plugin.getCitemManager().getProfileBgKey(), PersistentDataType.STRING);
                } else {
                    p.sendMessage("§cInvalid Background Item!");
                }
            }

            // Check and process the picture item (slot 6)
            ItemStack pictureItem = inv.getItem(6);
            if (pictureItem != null && pictureItem.hasItemMeta()) {
                ItemMeta pictureMeta = pictureItem.getItemMeta();
                PersistentDataContainer pictureData = pictureMeta.getPersistentDataContainer();

                // Retrieve the picture unique identifier
                if (pictureData.has(plugin.getCitemManager().getProfilePicKey(), PersistentDataType.STRING)) {
                    pictureUni = pictureData.get(plugin.getCitemManager().getProfilePicKey(), PersistentDataType.STRING);
                } else {
                    p.sendMessage("§cInvalid Picture Item!");
                }

                // Retrieve the picture ID
                if (pictureData.has(plugin.getCitemManager().getIdKey(), PersistentDataType.STRING)) {
                    pictureID = pictureData.get(plugin.getCitemManager().getIdKey(), PersistentDataType.STRING);
                }
            }

            // Update the profile in the database
            manager.updateProfile(p, pictureUni, pictureID, backgroundUni, "e");
        }
    }

    @EventHandler
    public void onProfileClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        Player p = (Player) e.getWhoClicked();
        if (inv.equals(manager.getViewProfile())) {
            if (manager.friendButton.contains(e.getSlot())) {
                ItemStack item = e.getCurrentItem();
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer data = meta.getPersistentDataContainer();
                String cmd = data.get(manager.getCmdKey(), PersistentDataType.STRING);
                Bukkit.dispatchCommand(p, cmd);
            }
            e.setCancelled(true);
        }
    }
}