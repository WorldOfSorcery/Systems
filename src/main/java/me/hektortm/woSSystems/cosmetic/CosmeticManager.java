package me.hektortm.woSSystems.cosmetic;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.CosmeticType;
import me.hektortm.wosCore.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class CosmeticManager {
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    public Inventory mainPage = Bukkit.createInventory(null, 9, "§6Cosmetics");
    public static Inventory titlesPage;
    public static Inventory prefixPage;
    public static Inventory badgesPage;

    public CosmeticManager(DAOHub hub) {
        this.hub = hub;
    }


    public void openMainPage(Player p) {
        mainPage.setItem(2, createItem("§6Titles", Material.NAME_TAG));
        mainPage.setItem(4, createItem("§6Prefixes", Material.PAPER));
        mainPage.setItem(6, createItem("§6Badges", Material.FLOWER_BANNER_PATTERN));
        p.openInventory(mainPage);
    }

    public void openTitlesPage(Player p) {
        titlesPage = Bukkit.createInventory(null, 9*5, "§6Titles");
        List<String> titles = hub.getCosmeticsDAO().getPlayerCosmetics(p, CosmeticType.TITLE);
        Map<String, String> permissionTitles = hub.getCosmeticsDAO().getPermissionCosmetics(CosmeticType.TITLE);
        for (Map.Entry<String, String> entry : permissionTitles.entrySet()) {
            String id = entry.getKey();
            String permNode = entry.getValue();

            if (permNode == null || id == null) {
                plugin.writeLog("CosmeticManager", Level.WARNING, "Null permission node or id for prefix: " + entry);
                continue;
            }

            if (p.hasPermission(permNode) && !titles.contains(id)) {
                titles.add(id);
                plugin.writeLog("CosmeticManager", Level.INFO, "Added prefix " + id + " for player " + p.getName());
            }
        }

        for (int i = 0; i < titles.size(); i++) {
            List<String> lore = new ArrayList<>();
            plugin.writeLog("CosmeticManager", Level.INFO, hub.getCosmeticsDAO().getCurrentCosmeticId(p, CosmeticType.TITLE) + " | " + titles.get(i));
            lore.add(Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDescription(CosmeticType.TITLE, titles.get(i))));
            lore.add("§6"+hub.getCosmeticsDAO().getPlayerObtainedTime(p, titles.get(i)));
            lore.add("§7");
            if (Objects.equals(hub.getCosmeticsDAO().getCurrentCosmeticId(p, CosmeticType.TITLE), titles.get(i))) {
                lore.add("§cCurrently selected");
            } else {
                lore.add("§bClick to select");
            }

            ItemStack item = createItem(Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.TITLE, titles.get(i))), Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "titleID"), PersistentDataType.STRING, titles.get(i));
            meta.setLore(lore);
            item.setItemMeta(meta);
            titlesPage.setItem(i,item);
        }
        p.openInventory(titlesPage);
    }

    public void openPrefixPage(Player p) {
        prefixPage  = Bukkit.createInventory(null, 9*5, "§6Prefixes");
        List<String> prefixes = hub.getCosmeticsDAO().getPlayerCosmetics(p, CosmeticType.PREFIX);
        Map<String, String> permissionPrefix = hub.getCosmeticsDAO().getPermissionCosmetics(CosmeticType.PREFIX);
        for (Map.Entry<String, String> entry : permissionPrefix.entrySet()) {
            String id = entry.getKey();
            String permNode = entry.getValue();

            if (permNode == null || id == null) {
                plugin.writeLog("CosmeticManager", Level.WARNING, "Null permission node or id for prefix: " + entry);
                continue;
            }

            if (p.hasPermission(permNode) && !prefixes.contains(id)) {
                prefixes.add(id);
                plugin.writeLog("CosmeticManager", Level.INFO, "Added prefix " + id + " for player " + p.getName());
            }
        }


        for (int i = 0; i < prefixes.size(); i++) {
            List<String> lore = new ArrayList<>();
            lore.add(Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDescription(CosmeticType.PREFIX, prefixes.get(i))));
            lore.add("§6"+hub.getCosmeticsDAO().getPlayerObtainedTime(p, prefixes.get(i)));
            lore.add("§7");
            if (Objects.equals(hub.getCosmeticsDAO().getCurrentCosmeticId(p, CosmeticType.PREFIX), prefixes.get(i))) {
                lore.add("§cCurrently selected");
            } else {
                lore.add("§bClick to select");
            }

            ItemStack item = createItem(Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.PREFIX, prefixes.get(i))), Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "prefixID"), PersistentDataType.STRING, prefixes.get(i));
            meta.setLore(lore);
            item.setItemMeta(meta);
            prefixPage.setItem(i,item);
        }
        p.openInventory(prefixPage);
    }

    public void openBadgePage(Player p) {
        badgesPage  = Bukkit.createInventory(null, 9*5, "§6Badges");
        List<String> badges = hub.getCosmeticsDAO().getPlayerCosmetics(p, CosmeticType.BADGE);
        Map<String, String> permissionBadge = hub.getCosmeticsDAO().getPermissionCosmetics(CosmeticType.BADGE);
        for (Map.Entry<String, String> entry : permissionBadge.entrySet()) {
            String id = entry.getKey();
            String permNode = entry.getValue();

            if (permNode == null || id == null) {
                plugin.writeLog("CosmeticManager", Level.WARNING, "Null permission node or id for prefix: " + entry);
                continue;
            }

            if (p.hasPermission(permNode) && !badges.contains(id)) {
                badges.add(id);
                plugin.writeLog("CosmeticManager", Level.INFO, "Added prefix " + id + " for player " + p.getName());
            }
        }


        for (int i = 0; i < badges.size(); i++) {
            List<String> lore = new ArrayList<>();
            lore.add(Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDescription(CosmeticType.BADGE, badges.get(i))));
            lore.add("§6"+hub.getCosmeticsDAO().getPlayerObtainedTime(p, badges.get(i)));
            lore.add("§7");
            if (Objects.equals(hub.getCosmeticsDAO().getCurrentCosmeticId(p, CosmeticType.BADGE), badges.get(i))) {
                lore.add("§cCurrently selected");
            } else {
                lore.add("§bClick to select");
            }

            ItemStack item = createItem(Utils.parseColorCodeString(hub.getCosmeticsDAO().getCosmeticDisplay(CosmeticType.BADGE, badges.get(i))), Material.FLOWER_BANNER_PATTERN);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "badgeID"), PersistentDataType.STRING, badges.get(i));
            meta.setLore(lore);
            item.setItemMeta(meta);
            badgesPage.setItem(i,item);
        }
        p.openInventory(badgesPage);
    }

    private ItemStack createItem(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
