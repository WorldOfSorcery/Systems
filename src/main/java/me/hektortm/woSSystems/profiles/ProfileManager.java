package me.hektortm.woSSystems.profiles;

import me.hektortm.woSSystems.database.DAOHub;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ProfileManager {

    private static Inventory editProfile;
    private static Inventory viewProfile;

    private final DAOHub hub;

    public ProfileManager(DAOHub hub) {
        this.hub = hub;
    }

    public void openProfile(Player forPlayer) {

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

}
