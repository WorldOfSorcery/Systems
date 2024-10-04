package me.hektortm.woSSystems.interactions.gui;


import me.hektortm.woSSystems.interactions.core.ActionHandler;
import me.hektortm.woSSystems.interactions.core.InteractionConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIManager implements Listener {

    private Plugin plugin;
    private ActionHandler actionHandler;
    private Map<String, InteractionConfig> guiInteractions = new HashMap<>();
    private Map<Player, InteractionConfig> openGUIs = new HashMap<>();

    public GUIManager(Plugin plugin, ActionHandler actionHandler) {
        this.plugin = plugin;
        this.actionHandler = actionHandler;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Load and store GUI interactions for later use
    public void loadGUIInteraction(String id, InteractionConfig interactionConfig) {
        guiInteractions.put(id, interactionConfig);
    }

    // Open a GUI for the player
    public void openGUI(Player player, InteractionConfig interactionConfig) {
        String title = interactionConfig.getInventoryTitle();
        int rows = interactionConfig.getInventoryRows();
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);

        // Setup inventory slots based on the configuration (this part remains unchanged)
        Map<Integer, Map<String, Object>> slots = interactionConfig.getSlots();
        for (Map.Entry<Integer, Map<String, Object>> entry : slots.entrySet()) {
            int slot = entry.getKey();
            inventory.setItem(slot, createItemFromConfig(entry.getValue()));
        }

        // Track that this player has this custom GUI open
        openGUIs.put(player, interactionConfig);

        // Open the inventory for the player
        player.openInventory(inventory);
    }

    // Create an item based on YAML configuration
    private ItemStack createItemFromConfig(Map<String, Object> slotConfig) {
        Material material = Material.getMaterial((String) slotConfig.get("item"));
        if (material == null) material = Material.STONE;  // Fallback to stone if item is invalid
        ItemStack item = new ItemStack(material, (int) slotConfig.getOrDefault("amount", 1));

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Set custom name
            String name = (String) slotConfig.get("meta.name");
            if (name != null) {
                meta.setDisplayName(name.replace("&", "§"));
            }

            // Set lore
            List<String> lore = (List<String>) slotConfig.get("meta.lore");
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, lore.get(i).replace("&", "§")); // Replace & with § in lore
                }
                meta.setLore(lore);
            }

            // Set unbreakable
            Boolean unbreakable = (Boolean) slotConfig.get("meta.unbreakable");
            if (unbreakable != null) meta.setUnbreakable(unbreakable);

            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Handle any clean-up or additional logic when the inventory is closed
    }

    public boolean hasCustomGUIOpen(Player player) {
        return openGUIs.containsKey(player);
    }

    // Get the InteractionConfig for the player’s open GUI
    public InteractionConfig getOpenGUIConfig(Player player) {
        return openGUIs.get(player);
    }

    // Remove the player from the open GUI map when they close their GUI
    public void closeGUI(Player player) {
        openGUIs.remove(player);
    }

}
