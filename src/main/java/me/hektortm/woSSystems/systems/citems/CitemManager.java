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
import java.util.UUID;


public class CitemManager {

    private final DAOHub hub;

    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)),WoSCore.getPlugin(WoSCore.class));


    public CitemManager(DAOHub hub) {
        this.hub = hub;
    }


    public void giveCitem(CommandSender s, Player t, String id, Integer amount) {
        ItemStack itemToGive = hub.getCitemDAO().getCitem(id);

        if (itemToGive == null) {
            s.sendMessage("[Database] Item is §onull"); // TODO: lang message
            return;
        }

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
        if (itemId == null) return;

        ItemStack dbItem = hub.getCitemDAO().getCitem(itemId);

            if (dbItem == null) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                Utils.success(p, "citems", "update.removed", "%item%", meta.getDisplayName());
                p.getInventory().remove(item);
                return;
            }



        if (!dbItem.isSimilar(item)) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            Utils.success(p, "citems", "update.updated", "%item%", meta.getDisplayName());

            // Update the item in hand with the new data
            dbItem.setAmount(amount);
            p.getInventory().setItemInMainHand(dbItem);
        }


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