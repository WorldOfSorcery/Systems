package me.hektortm.woSSystems.systems.guis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.GUI;
import me.hektortm.woSSystems.utils.dataclasses.GUISlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Level;

public class GUIManager implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    public GUIManager(DAOHub hub) {
        this.hub = hub;
    }

    public void openGUI(Player player, String guiId) {
        GUI gui = hub.getGuiDAO().getGUIbyId(guiId);
        if (gui == null) {
            plugin.writeLog("GUIManager", Level.WARNING, "GUI not found: " + guiId);
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new GUIHolder(guiId),
                gui.getSize(),
                gui.getTitle()
        );

        for (GUISlot slot : gui.getSlots()) {
            if (!slot.isVisible()) continue;

            ItemStack item = new ItemStack(slot.getMaterial());
            ItemMeta meta = item.getItemMeta();

            if (slot.getDisplayName() != null) {
                meta.setDisplayName(slot.getDisplayName());
            }

            if (slot.getLore() != null && !slot.getLore().isEmpty()) {
                meta.setLore(slot.getLore());
            }

            if (slot.getCustomModelData() != 0) {
                meta.setCustomModelData(slot.getCustomModelData());
            }

            if (slot.isEnchanted()) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
            inventory.setItem(slot.getSlot(), item);
        }

        player.openInventory(inventory);
        executeActions(player, gui.getOpenActions());
    }

    private void executeActions(Player player, List<String> actions) {
        for (String action : actions) {
            String processed = action.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;

        event.setCancelled(true);

        GUI gui = hub.getGuiDAO().getGUIbyId(holder.getGuiId());
        if (gui == null) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        for (GUISlot guiSlot : gui.getSlots()) {
            if (guiSlot.getSlot() == slot && guiSlot.isVisible()) {
                handleClickActions(player, event.getClick(), guiSlot);
                break;
            }
        }
    }

    private void handleClickActions(Player player, ClickType clickType, GUISlot slot) {
        List<String> actions = switch (clickType) {
            case LEFT -> slot.getLeft_actions();
            case RIGHT -> slot.getRight_actions();
            default -> null;
        };

        if (actions != null && !actions.isEmpty()) {
            executeActions(player, actions);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;

        GUI gui = hub.getGuiDAO().getGUIbyId(holder.getGuiId());
        if (gui != null) {
            executeActions(player, gui.getCloseActions());
        }
    }

    private static class GUIHolder implements InventoryHolder {
        private final String guiId;

        public GUIHolder(String guiId) {
            this.guiId = guiId;
        }

        public String getGuiId() {
            return guiId;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
