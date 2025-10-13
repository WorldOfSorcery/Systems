package me.hektortm.woSSystems.systems.guis;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.woSSystems.utils.dataclasses.GUI;
import me.hektortm.woSSystems.utils.dataclasses.GUISlot;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

import javax.swing.text.html.HTML;
import java.util.*;
import java.util.logging.Level;

import static io.papermc.paper.datacomponent.item.DyedItemColor.dyedItemColor;
import static me.hektortm.woSSystems.utils.Parsers.hexToBukkitColor;

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
                Utils.parseColorCodeString(gui.getTitle())
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


            ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(placeholderResolver.resolvePlaceholders(slot.getMaterial().toString(), player))));
            ItemMeta meta = item.getItemMeta();

            if (slot.getDisplayName() != null) meta.setDisplayName(Utils.parseColorCodeString(placeholderResolver.resolvePlaceholders(slot.getDisplayName(), player)));


            if (slot.getLore() != null && !slot.getLore().isEmpty()) {

                List<String> slotLore = slot.getLore();
                for (int i = 0; i < slotLore.size(); i++) {
                    slotLore.set(i, Utils.parseColorCodeString(placeholderResolver.resolvePlaceholders(slotLore.get(i), player)));
                }

                meta.setLore(slotLore);
            }

            if (slot.getModel() != null) meta.setItemModel(new NamespacedKey("wos", placeholderResolver.resolvePlaceholders(slot.getModel(), player)));

            DyedItemColor dyedColor = null;
            if(slot.getColor() != null) dyedColor = dyedItemColor(hexToBukkitColor(slot.getColor()));



            if(slot.getTooltip() != null && slot.getTooltip().isEmpty()) {
                if (Objects.equals(slot.getTooltip(), "hidden")) meta.setHideTooltip(true);
                else if(slot.getTooltip() != null && !slot.getTooltip().isEmpty()) meta.setTooltipStyle(new NamespacedKey("minecraft", placeholderResolver.resolvePlaceholders(slot.getTooltip(), player)));
            }

            if (slot.isEnchanted()) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_UNBREAKABLE,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
                    ItemFlag.HIDE_DESTROYS,
                    ItemFlag.HIDE_ARMOR_TRIM,
                    ItemFlag.HIDE_PLACED_ON,
                    ItemFlag.HIDE_STORED_ENCHANTS,
                    ItemFlag.HIDE_DYE);

            item.setItemMeta(meta);
            item.setAmount(slot.getAmount());
            if (dyedColor != null) item.setData(DataComponentTypes.DYED_COLOR, dyedColor);
            inventory.setItem(slot.getSlot(), item);
        }
        if (gui.getOpenActions() != null && !gui.getOpenActions().isEmpty()) {
            actionHandler.executeActions(player, gui.getOpenActions(), ActionHandler.SourceType.GUI, guiId);
        }
        player.openInventory(inventory);
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

        if (actions == null || actions.isEmpty()) return;
        actionHandler.executeActions(player, actions, ActionHandler.SourceType.GUI, null);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;

        GUI gui = hub.getGuiDAO().getGUIbyId(holder.getGuiId());
        if (gui != null) {
            if (gui.getCloseActions() != null && gui.getCloseActions().isEmpty()) {
                actionHandler.executeActions(player, gui.getCloseActions(), ActionHandler.SourceType.GUI, gui.getGuiId());
            }
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
