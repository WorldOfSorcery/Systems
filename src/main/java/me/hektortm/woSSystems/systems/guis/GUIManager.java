package me.hektortm.woSSystems.systems.guis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class GUIManager {

    public final Map<String, JsonObject> guiConfigs = new HashMap<>();
    public final Map<UUID, String> openGUIs = new HashMap<>();
    private final File guiFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "guis");


    // Load all GUIs from a directory
    public void loadGUIs() {
        if (!guiFolder.exists() || !guiFolder.isDirectory()) {
            System.err.println("Invalid directory: " + guiFolder.getAbsolutePath());
            return;
        }

        File[] files = guiFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            String guiID = file.getName().replace(".json", "");
            try {
                JsonObject json = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
                guiConfigs.put(guiID, json);
                System.out.println("Loaded GUI: " + guiID);
            } catch (Exception e) {
                System.err.println("Failed to load GUI from " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Open a GUI for a player
    public void openGUI(Player player, String guiID) {
        JsonObject config = guiConfigs.get(guiID);
        if (config == null) {
            player.sendMessage(ChatColor.RED + "GUI with ID '" + guiID + "' not found.");
            return;
        }

        Inventory inventory = createInventory(config);
        player.openInventory(inventory);
        openGUIs.put(player.getUniqueId(), guiID);
    }

    // Create an inventory from the configuration
    private Inventory createInventory(JsonObject config) {
        String title = ChatColor.translateAlternateColorCodes('&', config.get("title").getAsString());
        int rows = config.get("rows").getAsInt();
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);

        JsonObject slots = config.getAsJsonObject("slots");
        for (Map.Entry<String, JsonElement> entry : slots.entrySet()) {
            int slot = Integer.parseInt(entry.getKey());
            JsonObject slotConfig = entry.getValue().getAsJsonObject();

            // Check visibility
            JsonObject attributes = slotConfig.getAsJsonObject("attributes");
            boolean visible = attributes != null && attributes.has("visible") && attributes.get("visible").getAsBoolean();
            if (!visible) continue;

            // Add item to inventory
            JsonObject itemConfig = slotConfig.getAsJsonObject("item");
            ItemStack item = createItem(itemConfig);
            if (item != null) {
                inventory.setItem(slot, item);
            }
        }

        return inventory;
    }

    // Item creation (with material, name, lore, enchanted, and amount)
    private ItemStack createItem(JsonObject itemConfig) {
        if (itemConfig == null) return null;

        Material material = Material.STONE;
        if (itemConfig.has("material")) {
            String materialName = itemConfig.get("material").getAsString().toUpperCase();
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid material: " + materialName);
                return null;
            }
        }

        int amount = itemConfig.has("amount") ? itemConfig.get("amount").getAsInt() : 1;

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        if (itemConfig.has("name")) {
            String name = ChatColor.translateAlternateColorCodes('&', itemConfig.get("name").getAsString());
            meta.setDisplayName(name);
        }

        if (itemConfig.has("lore")) {
            List<String> lore = new ArrayList<>();
            for (JsonElement line : itemConfig.getAsJsonArray("lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line.getAsString()));
            }
            meta.setLore(lore);
        }

        if (itemConfig.has("enchanted") && itemConfig.get("enchanted").getAsBoolean()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }

        item.setItemMeta(meta);
        return item;
    }

    // Handle inventory clicks
    public void handleInventoryClick(InventoryClickEvent event, String guiID) {
        JsonObject config = guiConfigs.get(guiID);
        if (config == null) return;

        JsonObject slots = config.getAsJsonObject("slots");
        JsonObject slotConfig = slots.getAsJsonObject(String.valueOf(event.getRawSlot()));

        if (slotConfig != null) {
            JsonObject attributes = slotConfig.getAsJsonObject("attributes");
            boolean clickable = attributes != null && attributes.has("clickable") && attributes.get("clickable").getAsBoolean();
            if (!clickable) {
                event.setCancelled(true);
            }
        }
    }
}
