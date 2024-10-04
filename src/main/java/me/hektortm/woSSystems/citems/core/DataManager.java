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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private final NamespacedKey undroppableKey;
    private final CitemCommand cmd;
    private final NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "id");

    public DataManager(CitemCommand cmd) {
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
        this.cmd = cmd;
    }

    public void saveItemToFile(ItemStack item, File file, String id) {
        ItemMeta meta = item.getItemMeta();
        JSONObject itemData = new JSONObject();

        // Save the item material and other properties
        itemData.put("material", item.getType().toString());

        // Check if item has meta data
        if (meta != null) {
            // Set the ID in the PersistentDataContainer

            PersistentDataContainer data = meta.getPersistentDataContainer();

            // Save the ID
            data.set(idKey, PersistentDataType.STRING, id);

            // Debug: Confirm the ID is being set
            String testId = data.get(idKey, PersistentDataType.STRING);
            System.out.println("Saved ID in PersistentDataContainer: " + testId); // Debug output

            // Setting additional meta data
            meta.getDisplayName(); // Example name, replace with actual logic
            item.setItemMeta(meta); // Don't forget to update the item with new meta

            // Save the ID to JSON
            itemData.put("id", id);
            if (meta.hasDisplayName()) {
                itemData.put("name", meta.getDisplayName());
            }
            if (meta.hasLore()) {
                itemData.put("lore", meta.getLore());
            }
            itemData.put("attributes", Collections.singletonMap("unbreakable", meta.isUnbreakable()));

            Map<String, Boolean> customFlags = new HashMap<>();
            customFlags.put("undroppable", meta.getPersistentDataContainer().has(undroppableKey, PersistentDataType.BYTE) &&
                    meta.getPersistentDataContainer().get(undroppableKey, PersistentDataType.BYTE) == 1);
            itemData.put("custom_flags", customFlags);

            if (meta.hasEnchants()) {
                Map<String, Integer> enchantments = new HashMap<>();
                meta.getEnchants().forEach((enchantment, level) -> {
                    enchantments.put(enchantment.getKey().getKey(), level);
                });
                itemData.put("enchantments", enchantments);
            }
        }

        // Write to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(itemData.toJSONString());
            writer.flush();
            System.out.println("Saved item to file: " + file.getAbsolutePath()); // Debug output
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public void updateItemInFile(ItemStack item, File file) {
        // Create a new JSON object to hold the item data
        JSONObject itemData = new JSONObject();

        // Load existing item data from the file to preserve the ID
        try (FileReader reader = new FileReader(file)) {
            JsonObject existingData = JsonParser.parseReader(reader).getAsJsonObject();

            // Preserve existing ID and material
            if (existingData.has("id")) {
                itemData.put("id", existingData.get("id").getAsString());
            }

            if (existingData.has("material")) {
                itemData.put("material", existingData.get("material").getAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now add or update the new item properties
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
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

            if (meta.hasEnchants()) {
                Map<String, Integer> enchantments = new HashMap<>();
                meta.getEnchants().forEach((enchantment, level) -> {
                    enchantments.put(enchantment.getKey().getKey(), level);
                });
                itemData.put("enchantments", enchantments);
            }
        }

        // Write the updated item data to the file
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

                if (jsonObject.has("id")) {
                    String loadedId = jsonObject.get("id").getAsString();
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(idKey, PersistentDataType.STRING, loadedId);
                    System.out.println("Loaded ID into PersistentDataContainer: " + loadedId); // Debugging ID
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
        if (meta == null) return;

        // Attempt to retrieve the item's ID from persistent data
        NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "id");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(idKey, PersistentDataType.STRING); // Retrieve the actual item ID

        // Check if item has a valid ID
        if (itemId != null) {
            // Construct the file path based on the ID
            File file = new File(cmd.citemsFolder, itemId + ".json");
            System.out.println("Looking for file: " + file.getAbsolutePath()); // Debug output

            // Check if the file exists
            if (!file.exists()) {
                // Remove the item from the player's inventory
                p.getInventory().remove(item);
                p.sendMessage("Your item has been removed because its data file is missing.");
                System.out.println("Removed item with ID " + itemId + " from inventory."); // Debug output
                return; // Exit the method early since the item was removed
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
        } else {
            p.sendMessage("This item doesn't have a valid ID.");
        }
    }







}