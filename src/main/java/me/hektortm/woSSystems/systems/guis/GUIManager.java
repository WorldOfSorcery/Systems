package me.hektortm.woSSystems.systems.guis;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GUIManager implements Listener {

    private final Gson gson = new Gson();
    private final Map<String, Inventory> guis = new HashMap<>();
    private final File guiFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "guis");

    public GUIManager() {
    }

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
                Inventory gui = createGUIFromJson(json);
                guis.put(guiID, gui);
                System.out.println("Loaded GUI: " + guiID);
            } catch (Exception e) {
                System.err.println("Failed to load GUI from " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Create GUI from JSON
    private Inventory createGUIFromJson(JsonObject config) {
        String title = ChatColor.translateAlternateColorCodes('&', config.get("title").getAsString());
        int rows = config.get("rows").getAsInt();
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);

        JsonObject slots = config.getAsJsonObject("slots");
        for (Map.Entry<String, JsonElement> entry : slots.entrySet()) {
            int slot = Integer.parseInt(entry.getKey());
            JsonObject itemConfig = entry.getValue().getAsJsonObject().getAsJsonObject("item");
            ItemStack item = createItem(itemConfig);
            if (item != null) {
                inventory.setItem(slot, item);
            }
        }

        return inventory;
    }

    // Open a GUI for a player
    public void openGUI(Player player, String guiID) {
        Inventory gui = guis.get(guiID);
        if (gui != null) {
            player.openInventory(gui);
        } else {
            player.sendMessage(ChatColor.RED + "GUI with ID '" + guiID + "' not found.");
        }
    }


    private ItemStack createItem(JsonObject itemConfig) {
        if (itemConfig == null) return null;

        Material material = Material.BARRIER;
        if (itemConfig.has("material")) material = Material.getMaterial(itemConfig.get("material").getAsString());

        ItemStack item = new ItemStack(material);
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

        if (itemConfig.has("amount")) {
            int amount = itemConfig.getAsJsonPrimitive("amount").getAsInt();
            item.setAmount(amount);
        }

        if(itemConfig.has("enchanted") && itemConfig.get("enchanted").getAsBoolean()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }
        item.setItemMeta(meta);
        return item;

    }
}
