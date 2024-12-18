package me.hektortm.woSSystems.systems.citems;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

public class CitemManager {

    private final NamespacedKey undroppableKey;
    private final NamespacedKey unusableKey;
    private final NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "id");
    private final NamespacedKey leftActionKey;
    private final NamespacedKey rightActionKey;
    public final File citemFolder;

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private InteractionManager interactionManager;
    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)),WoSCore.getPlugin(WoSCore.class));
    private final LangManager lang = new LangManager(WoSCore.getPlugin(WoSCore.class));
    private final CitemCommand cmd;


    public CitemManager() {
        citemFolder = new File(plugin.getDataFolder(), "citems");
        cmd = new CitemCommand(interactionManager);
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
        unusableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "unusable");
        leftActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-left");
        rightActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-right");
    }

    public void setInteractionManager(InteractionManager interactionManager) {
        if (interactionManager == null) {
            throw new IllegalArgumentException("ConditionHandler cannot be null.");
        }
        this.interactionManager = interactionManager;
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


            meta.getDisplayName();
            item.setItemMeta(meta);

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
                    meta.getPersistentDataContainer().get(undroppableKey, PersistentDataType.BOOLEAN) == true);
            customFlags.put("unusable", meta.getPersistentDataContainer().has(unusableKey, PersistentDataType.BYTE) &&
                    meta.getPersistentDataContainer().get(unusableKey,PersistentDataType.BOOLEAN) == true);

            itemData.put("custom_flags", customFlags);

            if (data.has(leftActionKey, PersistentDataType.STRING)) {
                itemData.put("action-left", data.get(leftActionKey, PersistentDataType.STRING));
            }

            if (data.has(rightActionKey, PersistentDataType.STRING)) {
                itemData.put("action-right", data.get(rightActionKey, PersistentDataType.STRING));
            }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void giveCitem(CommandSender s, Player t, String id, Integer amount) {
        if (!citemFolder.exists()) {
            Utils.error(s, "citems", "error.no-items");
            return;
        }

        File itemFile = new File(citemFolder, id + ".json");
        if (!itemFile.exists()) {
            Utils.error(s, "citems", "error.no-items");
            return;
        }
        ItemStack savedItem = loadItemFromFile(itemFile);
        ItemStack item = savedItem.clone();

        item.setAmount(amount);
        if (savedItem == null) {
            Utils.error(s, "citems", "error.not-found");
            return;
        }
        t.getInventory().addItem(item);
        t.playSound(t.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1 ,1);
        String message = lang.getMessage("citems", "given").replace("%amount%", String.valueOf(amount)).replace("%id%", id).replace("%player%", t.getName());
        s.sendMessage(message);
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
            customFlags.put("unusable", data.has(unusableKey, PersistentDataType.BYTE) && data.get(unusableKey, PersistentDataType.BYTE) == 1);
            itemData.put("custom_flags", customFlags);

            if (data.has(leftActionKey, PersistentDataType.STRING)) {
                itemData.put("action-left", data.get(leftActionKey, PersistentDataType.STRING));
            }

            if (data.has(rightActionKey, PersistentDataType.STRING)) {
                itemData.put("action-right", data.get(rightActionKey, PersistentDataType.STRING));
            }

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

    public boolean isCitem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;  // Return false if the item is null or has no metadata
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;  // Return false if item meta is null
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        // Check if the item has a custom "id" key in its persistent data
        String itemId = data.get(idKey, PersistentDataType.STRING); // Retrieve the ID
        return itemId != null && !itemId.isEmpty(); // Return true if ID exists and is not empty
    }


    public int citemAmount(Player p, String id) {
        if (p == null || id == null) {
            return 0;
        }

        PlayerInventory inv = p.getInventory();
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item == null || !item.hasItemMeta()) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            PersistentDataContainer data = meta.getPersistentDataContainer();
            String itemId = data.get(idKey, PersistentDataType.STRING);
            if (id.equals(itemId)) {
                count += item.getAmount();
            }
        }
        return count;
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
                    if (customFlags.has("unusable") && customFlags.get("unusable").getAsBoolean()) {
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        data.set(unusableKey, PersistentDataType.BYTE, (byte) 1);
                    }
                }

                if (jsonObject.has("action-left")) {
                    String leftActionId = jsonObject.get("action-left").getAsString();
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(leftActionKey, PersistentDataType.STRING, leftActionId);
                }

                if (jsonObject.has("action-right")) {
                    String rightActionId = jsonObject.get("action-right").getAsString();
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(rightActionKey, PersistentDataType.STRING, rightActionId);
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

            // Check if the file exists
            if (!file.exists()) {
                // Remove the item from the player's inventory
                p.getInventory().remove(item);
                Utils.successMsg1Value(p, "citems", "update.removed", "%item%", item.getItemMeta().getDisplayName());
                return; // Exit the method early since the item was removed
            }

            // Load the item data from the file
            ItemStack savedItem = loadItemFromFile(file);

            if (savedItem != null && !item.isSimilar(savedItem)) {
                // Update the player's item to match the saved data if there are differences
                item.setItemMeta(savedItem.getItemMeta());
                Utils.successMsg1Value(p, "citems", "update.updated", "%item%", item.getItemMeta().getDisplayName());
            }
        } else {
            log.sendWarning(p.getName() + ": Item \""+ item.getItemMeta().getDisplayName()+"%red_300%\" -> no valid ID");
        }
    }


    public void leftClickAction(Player p) {
        ItemStack item = p.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey leftActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-left");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String actionId = data.get(leftActionKey, PersistentDataType.STRING);
        if (actionId != null) {
            interactionManager.triggerInteraction(p, actionId);
        }
    }

    public void rightClickAction(Player p) {
        ItemStack item = p.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey rightActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-right");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String actionId = data.get(rightActionKey, PersistentDataType.STRING);
        if (actionId != null) {
            interactionManager.triggerInteraction(p, actionId);
        }
    }

    public NamespacedKey getIdKey() {
        return idKey;
    }

    public NamespacedKey getUndroppableKey() {
        return undroppableKey;
    }

    public NamespacedKey getUnusableKey() {
        return unusableKey;
    }

    public NamespacedKey getLeftActionKey() {
        return leftActionKey;
    }

    public NamespacedKey getRightActionKey() {
        return rightActionKey;
    }



}