package me.hektortm.woSSystems.systems.guis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
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
    private final PlaceholderResolver placeholderResolver = plugin.getPlaceholderResolver();
    private final ConditionHandler conditions = plugin.getConditionHandler();
    private final ActionHandler actionHandler = new ActionHandler();
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
                gui.getSize()*9,
                gui.getTitle()
        );
        List<GUISlot> slots = gui.getSlots();


        for (GUISlot slot : slots) {
            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.GUISLOT,
                    guiId + ":"+slot.getSlot()+":"+slot.getSlotId()
            );
            if (!conditionList.isEmpty()) {
                boolean shouldRun;
                if ("one".equalsIgnoreCase(slot.getMatchType())) {
                    shouldRun = conditionList.isEmpty() || conditionList.stream().anyMatch(cond -> conditions.evaluate(player, cond));
                } else {
                    shouldRun = conditions.checkConditions(player, conditionList);
                }

                if (!shouldRun) continue;
            }

            if (!slot.isVisible()) continue;

            List<String> preLore = slot.getLore();
            for (int i = 0; i < preLore.size(); i++) {
                preLore.set(i, placeholderResolver.resolvePlaceholders(preLore.get(i), player).replace("&", "§"));
            }


            ItemStack item = new ItemStack(slot.getMaterial());
            ItemMeta meta = item.getItemMeta();

            if (slot.getDisplayName() != null) {
                meta.setDisplayName(placeholderResolver.resolvePlaceholders(slot.getDisplayName(), player).replace("&", "§"));
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
        actionHandler.executeActions(player, gui.getOpenActions(), ActionHandler.SourceType.GUI, guiId);
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
        plugin.writeLog("GUIManager", Level.INFO, gui.getSlots().toString());
        for (GUISlot guiSlot : gui.getSlots()) {
            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.GUISLOT,
                    holder.getGuiId() + ":"+guiSlot.getSlot()+":"+guiSlot.getSlotId()
            );
            plugin.writeLog("GUIManager", Level.INFO, holder.getGuiId()+":"+guiSlot.getSlot()+":"+guiSlot.getSlotId() + " - Conditions: " + conditionList.toString());
            if (!conditionList.isEmpty()) {
                boolean shouldRun;
                if ("one".equalsIgnoreCase(guiSlot.getMatchType())) {
                    shouldRun = conditionList.isEmpty() || conditionList.stream().anyMatch(cond -> conditions.evaluate(player, cond));
                } else {
                    shouldRun = conditions.checkConditions(player, conditionList);
                }

                if (!shouldRun) continue;
            }
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
            actionHandler.executeActions(player, actions, ActionHandler.SourceType.GUI, null);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;

        GUI gui = hub.getGuiDAO().getGUIbyId(holder.getGuiId());
        if (gui != null) {
            actionHandler.executeActions(player, gui.getCloseActions(), ActionHandler.SourceType.GUI, gui.getGuiId());
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
