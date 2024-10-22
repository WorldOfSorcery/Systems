package me.hektortm.woSSystems.systems.interactions.gui;


import me.hektortm.woSSystems.systems.citems.DataManager;
import me.hektortm.woSSystems.systems.interactions.core.ActionHandler;
import me.hektortm.woSSystems.systems.interactions.core.InteractionConfig;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIManager implements Listener {



    private final Plugin plugin;
    private final ActionHandler actionHandler;
    private final PlaceholderResolver resolver;
    private final DataManager citemManager;
    private Map<String, InteractionConfig> guiInteractions = new HashMap<>();
    public Map<Player, InteractionConfig> openGUIs = new HashMap<>();

    public GUIManager(Plugin plugin, ActionHandler actionHandler, PlaceholderResolver resolver, DataManager citemManager) {
        this.plugin = plugin;
        this.citemManager = citemManager;
        this.actionHandler = actionHandler;
        this.resolver = resolver;
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

        // Update the inventory items based on the current player stats
        updateInventory(inventory, interactionConfig, player);

        // Track that this player has this custom GUI open
        openGUIs.put(player, interactionConfig);

        // Open the inventory for the player
        player.openInventory(inventory);
    }

    private void updateInventory(Inventory inventory, InteractionConfig interactionConfig, Player player) {
        // Clear the inventory to prepare for new items
        inventory.clear();

        // Setup inventory slots based on the configuration
        Map<Integer, Map<String, Object>> slots = interactionConfig.getSlots();
        for (Map.Entry<Integer, Map<String, Object>> entry : slots.entrySet()) {
            int slot = entry.getKey();
            // Create items based on the current stats instead of a static config
            inventory.setItem(slot, createItemFromConfig2(entry.getValue(), player));
        }

        // The inventory is now populated with the latest items
    }

    private ItemStack createItemFromConfig2(Map<String, Object> slotConfig, Player player) {
        ItemStack item = null;
        ItemMeta meta = null;

        // Check if it's a citem
        if (slotConfig.containsKey("citem")) {
            String citemId = (String) slotConfig.get("citem");

            // Load the citem from the custom item system (replace with your actual loading mechanism)
            File citemFile = new File(plugin.getDataFolder()+ "citems/", citemId + ".json");  // Adjust the path as needed
            item = citemManager.loadItemFromFile(citemFile);  // Assuming this method returns an ItemStack

            if (item != null) {
                meta = item.getItemMeta();  // Use the metadata from the custom item directly
            }

            List<String> lore = (List<String>) slotConfig.get("citem.lore");
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, lore.get(i).replace("&", "§"));
                }
                meta.setLore(lore);
            }

        }

        // If no citem or it failed to load, fallback to a regular item
        if (item == null && slotConfig.containsKey("item")) {
            Material material = Material.getMaterial((String) slotConfig.get("item"));
            if (material == null) material = Material.STONE;  // Fallback to stone if item is invalid
            item = new ItemStack(material, (int) slotConfig.getOrDefault("amount", 1));
            meta = item.getItemMeta();  // Use the regular item meta
        }

        // If no valid item was found, fallback to STONE
        if (item == null) {
            item = new ItemStack(Material.STONE);  // Default to stone
            meta = item.getItemMeta();
        }

        // Apply metadata from slotConfig only if it's a regular item (not citem)
        if (meta != null && !slotConfig.containsKey("citem")) {
            // Set custom name from slotConfig
            String name = (String) slotConfig.get("meta.name");
            if (name != null) {
                meta.setDisplayName(name.replace("&", "§"));
            }

            // Set lore dynamically based on the config
            List<String> lore = (List<String>) slotConfig.get("meta.lore");
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, lore.get(i).replace("&", "§"));
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

    // Create an item based on YAML configuration
    private ItemStack createItemFromConfig(Map<String, Object> slotConfig, Player player) {
        Material material = Material.getMaterial((String) slotConfig.get("item"));
        if (material == null) material = Material.STONE;  // Fallback to stone if item is invalid
        ItemStack item = new ItemStack(material, (int) slotConfig.getOrDefault("amount", 1));

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Set custom name
            String name = (String) slotConfig.get("meta.name");
            if (name != null) {
                name = resolver.resolvePlaceholders(name, player);  // Ensure this accesses the current player's state
                meta.setDisplayName(name.replace("&", "§"));
            }

            // Set lore dynamically based on current player stats
            List<String> lore = (List<String>) slotConfig.get("meta.lore");
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    // Resolve lore with current player stats
                    lore.set(i, resolver.resolvePlaceholders(lore.get(i), player).replace("&", "§"));
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
