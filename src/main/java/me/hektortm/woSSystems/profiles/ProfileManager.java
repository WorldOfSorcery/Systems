package me.hektortm.woSSystems.profiles;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileManager {

    private NamespacedKey cmdKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "gui-cmd");
    private static Inventory editProfile;
    private static Inventory viewProfile;
    public List<Integer> friendButton = new ArrayList<>();

    private final DAOHub hub;

    public ProfileManager(DAOHub hub) {
        this.hub = hub;
        friendButton.add(14);
        friendButton.add(15);
        friendButton.add(16);
        friendButton.add(17);
    }

    public void openEditProfile(Player forPlayer) {

        String background = negativeSpace(8)+"\uE700";

        String picture = "";

        if (hub.getProfileDAO().getProfilePicture(forPlayer.getUniqueId()) != null) {
            String uni = StringEscapeUtils.unescapeJava(hub.getProfileDAO().getProfilePicture(forPlayer.getUniqueId()));
            picture = negativeSpace(169)+uni;
        }


        ItemStack backgroundItem = hub.getCitemDAO().getCitem(hub.getProfileDAO().getBackgroundID(forPlayer.getUniqueId()));
        ItemStack pictureItem = hub.getCitemDAO().getCitem(hub.getProfileDAO().getProfilePictureID(forPlayer.getUniqueId()));


        editProfile = Bukkit.createInventory(null, 9*6, "§f"+background+picture);
        if (pictureItem != null) {
            editProfile.setItem(6, pictureItem);
        }
        if (backgroundItem != null) {
            editProfile.setItem(7, backgroundItem);
        }
        editProfile.setItem(8, createItem("§cClose"));
        editProfile.setItem(14, createItem("§aAdd Friend"));
        editProfile.setItem(15, createItem("§aAdd Friend"));
        editProfile.setItem(16, createItem("§aAdd Friend"));
        editProfile.setItem(17, createItem("§aAdd Friend"));
        editProfile.setItem(20, createPlayerHead(forPlayer));
        editProfile.setItem(23, createItem("§eTrade"));
        editProfile.setItem(24, createItem("§eTrade"));
        editProfile.setItem(25, createItem("§eTrade"));
        editProfile.setItem(26, createItem("§eTrade"));

        forPlayer.openInventory(editProfile);

    }

    public void openViewProfile(Player forPlayer, OfflinePlayer target) {
        UUID targetUUID = target.getUniqueId();

        String background = negativeSpace(8)+"\uE700";

        String picture = "";

        if (hub.getProfileDAO().getProfilePicture(target.getUniqueId()) != null) {
            String uni = StringEscapeUtils.unescapeJava(hub.getProfileDAO().getProfilePicture(target.getUniqueId()));
            picture = negativeSpace(169)+uni;
        }


        ItemStack backgroundItem = hub.getCitemDAO().getCitem(hub.getProfileDAO().getBackgroundID(target.getUniqueId()));
        ItemStack pictureItem = hub.getCitemDAO().getCitem(hub.getProfileDAO().getProfilePictureID(target.getUniqueId()));


        viewProfile = Bukkit.createInventory(null, 9*6, "§f"+background+picture);
        if (pictureItem != null) {
            viewProfile.setItem(6, pictureItem);
        }
        if (backgroundItem != null) {
            viewProfile.setItem(7, backgroundItem);
        }
        viewProfile.setItem(8, createItem("§cClose"));
        viewProfile.setItem(14, createItemWithCommand("§aAdd Friend", "friend add "+target.getName()));
        viewProfile.setItem(15, createItemWithCommand("§aAdd Friend", "friend add "+target.getName()));
        viewProfile.setItem(16, createItemWithCommand("§aAdd Friend", "friend add "+target.getName()));
        viewProfile.setItem(17, createItemWithCommand("§aAdd Friend", "friend add "+target.getName()));
        viewProfile.setItem(20, createPlayerHead(target));
        viewProfile.setItem(23, createItem("§eTrade"));
        viewProfile.setItem(24, createItem("§eTrade"));
        viewProfile.setItem(25, createItem("§eTrade"));
        viewProfile.setItem(26, createItem("§eTrade"));

        forPlayer.openInventory(viewProfile);
    }

    private ItemStack createPlayerHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName("§f"+player.getName());
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(String name) {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(9);
        meta.setItemName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItemWithCommand(String name, String command) {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(9);
        meta.setItemName(name);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(cmdKey, PersistentDataType.STRING, command);
        item.setItemMeta(meta);
        return item;
    }

    public void updateProfile(Player p, String picture, String pictureID, String background, String backgroundID) {
        hub.getProfileDAO().updateBackground(p.getUniqueId(), background);
        hub.getProfileDAO().updateProfilePicture(p.getUniqueId(), picture, pictureID);
        p.sendMessage("§aProfile updated!");
    }

    public void updatePicture(Player p, String picture, String pictureID) {
        hub.getProfileDAO().updateProfilePicture(p.getUniqueId(), picture, pictureID);
        p.sendMessage("§aProfile picture updated!");
    }

    private String negativeSpace(int amount) {
        String space = "";
        for (int i = 0; i < amount; i++) {
            space += "\uF002";
        }
        return space;
    }

    public Inventory getEditProfile() {
        return editProfile;
    }

    public Inventory getViewProfile() {
        return viewProfile;
    }
    public NamespacedKey getCmdKey() {
        return cmdKey;
    }
}
