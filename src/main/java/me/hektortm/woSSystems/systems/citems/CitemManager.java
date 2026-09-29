package me.hektortm.woSSystems.systems.citems;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;


public class CitemManager {

    private final DAOHub hub;

    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)),WoSCore.getPlugin(WoSCore.class));


    public CitemManager(DAOHub hub) {
        this.hub = hub;
    }


    public void giveCitem(CommandSender s, Player t, String id, Integer amount) {
        ItemStack itemToGive = hub.getCitemDAO().getCitem(id);

        if (itemToGive == null) return;


        itemToGive.setAmount(amount);
        t.getInventory().addItem(itemToGive);
        t.playSound(t.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1 ,1);
        Utils.success(s, "citems", "given", "%amount%", String.valueOf(amount), "%id%", id, "%player%", t.getName());
    }

    public boolean isCitem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(Keys.ID.get(), PersistentDataType.STRING); // Retrieve the ID

        return hub.getCitemDAO().getCitem(itemId) != null;
    }

    public void updateItem(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        int amount = item.getAmount();
        if (item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();

        if (meta == null) return;


        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(Keys.ID.get(), PersistentDataType.STRING);
        String uuuid = data.get(Keys.UUUID.get(),  PersistentDataType.STRING);
        if (itemId == null) return;

        ItemStack dbItem = hub.getCitemDAO().getCitem(itemId);

            if (dbItem == null) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                Utils.success(p, "citems", "update.removed", "%item%", meta.getDisplayName());
                p.getInventory().remove(item);
                return;
            }



        if (isUpdateUuidDifferent(item, dbItem)) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            Utils.success(p, "citems", "update.updated", "%item%", meta.getDisplayName());

            // Update the item in hand with the new data
            dbItem.setAmount(amount);
            p.getInventory().setItemInMainHand(dbItem);
        }


    }


    public boolean isUpdateUuidDifferent(ItemStack item1, ItemStack item2) {
        String updateUUID1 = item1.getItemMeta().getPersistentDataContainer().get(Keys.UUUID.get(), PersistentDataType.STRING);
        String updateUUID2 = item2.getItemMeta().getPersistentDataContainer().get(Keys.UUUID.get(), PersistentDataType.STRING);

        return !Objects.equals(updateUUID1, updateUUID2);
    }

    /** The citem id stored on an item, or {@code null} if it isn't a citem. */
    private static String citemIdOf(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer().get(Keys.ID.get(), PersistentDataType.STRING);
    }

    /** How many of the custom item {@code id} the player carries (matched by its id tag). */
    public int countCitem(Player p, String id) {
        int found = 0;
        for (ItemStack item : p.getInventory().getStorageContents()) {
            if (id.equals(citemIdOf(item))) found += item.getAmount();
        }
        return found;
    }

    /**
     * Takes {@code amount} of the custom item {@code id} (matched by its id tag).
     * Takes nothing and returns false if the player has fewer.
     */
    public boolean takeCitem(Player p, String id, int amount) {
        if (countCitem(p, id) < amount) return false;
        ItemStack[] contents = p.getInventory().getStorageContents();
        int left = amount;
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack item = contents[i];
            if (!id.equals(citemIdOf(item))) continue;
            int take = Math.min(left, item.getAmount());
            item.setAmount(item.getAmount() - take);
            if (item.getAmount() <= 0) contents[i] = null;
            left -= take;
        }
        p.getInventory().setStorageContents(contents);
        return true;
    }

    /**
     * Gives {@code amount} of the custom item {@code id} without messages; what
     * doesn't fit drops at the player's feet. False if the item doesn't exist.
     */
    public boolean addCitem(Player p, String id, int amount) {
        ItemStack item = hub.getCitemDAO().getCitem(id);
        if (item == null) return false;
        item.setAmount(amount);
        for (ItemStack rest : p.getInventory().addItem(item).values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), rest);
        }
        return true;
    }

    public boolean hasCitemAmount(Player p, String id, int amount) {
        ItemStack citem = hub.getCitemDAO().getCitem(id);

        if (citem == null) return false;

        int found = 0;

        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) continue;

            if (item.isSimilar(citem)) {
                found += item.getAmount();
                if (found >= amount) {
                    return true;
                }
            }
        }

        return false;
    }


    private String parseTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return now.format(formatter);
    }



}