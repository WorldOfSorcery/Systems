package me.hektortm.woSSystems.citems.core;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.citems.commands.CitemCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private final NamespacedKey undroppableKey;
    private final CitemCommand cmd;

    public DataManager(CitemCommand cmd) {
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
        this.cmd = cmd;
    }

    public void saveItemToFile(ItemStack item, File file, String id) {
        NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), id); // Use dynamic ID as key
        ItemMeta meta = item.getItemMeta();
        JSONObject itemData = new JSONObject();

        // Save the item id in persistent data
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(idKey, PersistentDataType.STRING, id); // Store the ID as a string in the persistent data

        // Save existing item metadata (name, lore, attributes, etc.)
        if (meta.hasDisplayName()) {
            itemData.put("name", meta.getDisplayName());
        }
        if (meta.hasLore()) {
            itemData.put("lore", meta.getLore());
        }
        itemData.put("id", id); // Store the actual ID in the file

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("unbreakable", meta.isUnbreakable());
        itemData.put("attributes", attributes);

        Map<String, Boolean> customFlags = new HashMap<>();
        customFlags.put("undroppable", data.has(undroppableKey, PersistentDataType.BYTE) && data.get(undroppableKey, PersistentDataType.BYTE) == 1);
        itemData.put("custom_flags", customFlags);

        itemData.put("material", item.getType().toString());

        if (meta.hasEnchants()) {
            Map<String, Integer> enchantments = new HashMap<>();
            meta.getEnchants().forEach((enchantment, level) -> {
                enchantments.put(enchantment.getKey().getKey(), level);
            });
            itemData.put("enchantments", enchantments);
        }

        // Save changes to item meta
        item.setItemMeta(meta);

        // Write item data to the JSON file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(itemData.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateItemInFile(ItemStack item, File file) {
        ItemMeta meta = item.getItemMeta();
        JSONObject itemData = new JSONObject();

        if (meta.hasDisplayName()) {
            itemData.put("name", meta.getDisplayName());
        }

        if (meta.hasLore()) {
            itemData.put("lore", meta.getLore());
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("unbreakable", meta.isUnbreakable());
        itemData.put("attributes", attributes);

        Map<String, Boolean> customFlags = new HashMap<>();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        customFlags.put("undroppable", data.has(undroppableKey, PersistentDataType.BYTE) && data.get(undroppableKey, PersistentDataType.BYTE) == 1);
        itemData.put("custom_flags", customFlags);

        itemData.put("material", item.getType().toString());

        if (meta.hasEnchants()) {
            Map<String, Integer> enchantments = new HashMap<>();
            meta.getEnchants().forEach((enchantment, level) -> {
                enchantments.put(enchantment.getKey().getKey(), level);
            });
            itemData.put("enchantments", enchantments);
        }

        // Write updated item data to the file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(itemData.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack loadItemFromFile(File file) {
        Gson gson = new Gson();
        ItemStack itemStack = null;

        try (FileReader reader = new FileReader(file)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            String materialString = jsonObject.get("material").getAsString();
            Material material = Material.getMaterial(materialString);
            if (material == null) {
                return null;
            }

            itemStack = new ItemStack(material);
            ItemMeta meta = itemStack.getItemMeta();

            if (meta != null) {

                if (jsonObject.has("name")) {
                    String name = jsonObject.get("name").getAsString();
                    meta.setDisplayName(name);
                }

                if (jsonObject.has("lore")) {
                    List<String> lore = gson.fromJson(jsonObject.get("lore").getAsJsonArray(), List.class);
                    meta.setLore(lore);
                }

                if (jsonObject.has("attributes")) {
                    JsonObject attributes = jsonObject.get("attributes").getAsJsonObject();
                    if (attributes.get("unbreakable").getAsBoolean()) {
                        meta.setUnbreakable(true);
                    }
                }

                if (jsonObject.has("custom_flags")) {
                    JsonObject customFlags = jsonObject.get("custom_flags").getAsJsonObject();
                    if (customFlags.has("undroppable") && customFlags.get("undroppable").getAsBoolean()) {
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        data.set(undroppableKey, PersistentDataType.BYTE, (byte) 1);
                    }
                }

                if (jsonObject.has("enchantments")) {
                    JsonObject enchantments = jsonObject.get("enchantments").getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : enchantments.entrySet()) {
                        String enchantmentName = entry.getKey();
                        int level = entry.getValue().getAsInt();
                        Enchantment enchantment = Enchantment.getByName(enchantmentName);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        }
                    }
                }

                itemStack.setItemMeta(meta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemStack;
    }

    public void updateItem(Player p) {
        ItemStack item = p.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "id"); // Use "id" as key for retrieval
        String itemId = data.get(idKey, PersistentDataType.STRING);  // Retrieve the actual item ID

        Bukkit.getLogger().info(itemId);

        if (itemId == null) {
            p.sendMessage("This item doesn't have a valid ID.");
            return;
        }

        // Construct the file path based on the ID
        File file = new File(cmd.citemsFolder, itemId + ".json");

        if (!file.exists()) {
            p.sendMessage("No data file found for this item.");
            return;
        }

        // Load the item data from the file
        ItemStack savedItem = loadItemFromFile(file);

        if (savedItem != null && !item.isSimilar(savedItem)) {
            // Update the player's item to match the saved data if there are differences
            item.setItemMeta(savedItem.getItemMeta());
            p.sendMessage("Your item has been updated to match the saved data.");
        } else {
            p.sendMessage("Your item is already up to date.");
        }
    }
}