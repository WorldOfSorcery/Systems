package me.hektortm.woSSystems.systems.citems;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.chat.ChatManager;
import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.Icons;
import me.hektortm.woSSystems.utils.Letters;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static me.hektortm.woSSystems.utils.Icons.SIGNED_BY;
import static me.hektortm.woSSystems.utils.Icons.TIME;
import static me.hektortm.woSSystems.utils.Letters.*;


public class CitemManager {

    private final NamespacedKey undroppableKey;
    private final NamespacedKey unusableKey;
    private final NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "id");
    private final NamespacedKey leftActionKey;
    private final NamespacedKey rightActionKey;
    private final NamespacedKey timeKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey quoteKey;
    public final NamespacedKey ownerKey;
    public final File citemFolder;

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private InteractionManager interactionManager;
    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)),WoSCore.getPlugin(WoSCore.class));
    private final LangManager lang = new LangManager(WoSCore.getPlugin(WoSCore.class));
    private final NicknameManager nickManager = new NicknameManager(new ChatManager(plugin));
    private final CitemCommand cmd;


    public CitemManager() {
        citemFolder = new File(plugin.getDataFolder(), "citems");
        cmd = new CitemCommand(interactionManager);
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
        unusableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "unusable");
        leftActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-left");
        rightActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-right");
        timeKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "stamp-time");
        nameKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "stamp-name");
        quoteKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "stamp-quote");
        ownerKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "owner");

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
            if (meta instanceof Damageable) {
                Damageable damageableMeta = (Damageable) meta;
                itemData.put("damage", damageableMeta.getDamage()); // Use getDamage() here
            }
            if (meta.hasCustomModelData()) {
                itemData.put("custom_model_data", meta.getCustomModelData());
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
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(ownerKey, PersistentDataType.STRING, t.getUniqueId().toString());

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

        // Get the item material and add it to the JSON
        Material material = item.getType();
        itemData.put("material", material.toString());

        // Get the item meta and check if it's not null
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Preserve the ID (if present) in the PersistentDataContainer
            PersistentDataContainer data = meta.getPersistentDataContainer();
            String time = data.get(timeKey, PersistentDataType.STRING);
            String name = data.get(nameKey, PersistentDataType.STRING);
            if (data.has(idKey, PersistentDataType.STRING)) {
                String id = data.get(idKey, PersistentDataType.STRING);
                itemData.put("id", id);
            }

            // Add the display name, if present
            if (meta.hasDisplayName()) {
                itemData.put("name", meta.getDisplayName());
            }

            // Add the lore, if present
            if (meta.hasLore()) {
                itemData.put("lore", meta.getLore());
            }

            // Add attributes (e.g., unbreakable)
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("unbreakable", meta.isUnbreakable());
            itemData.put("attributes", attributes);

            // Add custom flags (undroppable, unusable)
            Map<String, Boolean> customFlags = new HashMap<>();
            customFlags.put("undroppable", data.has(undroppableKey, PersistentDataType.BYTE) && data.get(undroppableKey, PersistentDataType.BYTE) == 1);
            customFlags.put("unusable", data.has(unusableKey, PersistentDataType.BYTE) && data.get(unusableKey, PersistentDataType.BYTE) == 1);
            itemData.put("custom_flags", customFlags);

            // Add left and right action, if present
            if (data.has(leftActionKey, PersistentDataType.STRING)) {
                itemData.put("action-left", data.get(leftActionKey, PersistentDataType.STRING));
            }

            if (data.has(rightActionKey, PersistentDataType.STRING)) {
                itemData.put("action-right", data.get(rightActionKey, PersistentDataType.STRING));
            }

            // Add enchantments, if present
            if (meta.hasEnchants()) {
                Map<String, Integer> enchantments = new HashMap<>();
                meta.getEnchants().forEach((enchantment, level) -> {
                    enchantments.put(enchantment.getKey().getKey(), level);
                });
                itemData.put("enchantments", enchantments);
            }

            // Add damage value, if item is Damageable
            if (meta instanceof Damageable) {
                Damageable damageableMeta = (Damageable) meta;
                itemData.put("damage", damageableMeta.getDamage());
            }
            if (meta.hasCustomModelData()) {
                itemData.put("custom_model_data", meta.getCustomModelData());
            }
        }

        // Write the updated item data to the file, overwriting the old data
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
                if (jsonObject.has("damage")) {
                    if (meta instanceof Damageable) {
                        int damage = jsonObject.get("damage").getAsInt();
                        Damageable damageableMeta = (Damageable) meta;
                        damageableMeta.setDamage(damage);  // Set the damage value
                    }
                }
                if(jsonObject.has("custom_model_data")) {
                    int customModelData = jsonObject.get("custom_model_data").getAsInt();
                    meta.setCustomModelData(customModelData);
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
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasLore()) return;

        List<String> copyLore = meta.getLore();
        int length = copyLore.size();

        String nameKey2 = "null";
        String time = "null";
        String quote = "null";

        // Process lore to preserve "Time" and "Obtained by" and remove others
        if (length > 1) {
            String indLast = copyLore.get(length - 1);
            String indSecLast = copyLore.get(length - 2);

            // If "Signed By" is found, capture its value and process the rest
            if (indSecLast.contains(SIGNED_BY.getIcon())) {
                nameKey2 = indSecLast.split(SIGNED_BY.getIcon() + " ")[1];

                // Check the last line for "Time" or "Quote" and capture appropriately
                if (indLast.contains(QUOTE.getLetter())) {
                    quote = indLast;
                } else if (indLast.contains(TIME.getIcon())) {
                    time = indLast.split(TIME.getIcon() + " ")[1];
                }

                // Safely remove the last 3 lore entries containing "Time", "Obtained by", and "Quote"
                int itemsToRemove = Math.min(3, copyLore.size());
                for (int i = 0; i < itemsToRemove; i++) {
                    copyLore.remove(copyLore.size() - 1);
                }
            }
        }

        // Remove "Time" and "Obtained by" entries from the filtered lore list (do not touch these in the update)
        List<String> filteredLore = new ArrayList<>();
        for (String loreLine : copyLore) {
            if (!loreLine.contains(SIGNED_BY.getIcon()) && (!loreLine.contains(TIME.getIcon()) || !loreLine.contains(QUOTE.getLetter()))) {
                filteredLore.add(loreLine);
            }
        }

        // Now check if the filtered lore is different from the original lore
        boolean loreChanged = !filteredLore.equals(copyLore);

        // If the lore was changed, update it on the item
        if (loreChanged) {
            meta.setLore(filteredLore);
            item.setItemMeta(meta);
        }

        if (item == null || !item.hasItemMeta()) return;
        if (meta == null) return;

        // Attempt to retrieve the item's ID from persistent data
        NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "id");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(idKey, PersistentDataType.STRING);

        if (itemId != null) {
            // Construct the file path based on the ID
            File file = new File(cmd.citemsFolder, itemId + ".json");

            if (!file.exists()) {
                // Remove the item from the player's inventory if the file doesn't exist
                p.getInventory().remove(item);
                Utils.successMsg1Value(p, "citems", "update.removed", "%item%", item.getItemMeta().getDisplayName());
                return;
            }

            // Load the item data from the file
            ItemStack savedItem = loadItemFromFile(file);

            // Compare the item meta and update if necessary
            if (savedItem != null && !item.isSimilar(savedItem)) {
                ItemMeta newMeta = savedItem.getItemMeta();
                if (newMeta != null) {
                    // Keep the "Time" and "Obtained by" intact, but update other lore entries
                    List<String> newLore = newMeta.getLore();
                    // Re-attach "Time" and "Obtained by" to the new lore
                    if (nameKey2 != null && !nameKey2.equals("null")) {
                        newLore.add("§7");
                        newLore.add("§f" + SIGNED_BY.getIcon() + " §e" + nameKey2);

                        // Add "Quote" if present, otherwise add "Time"
                        if (quote != null && !quote.equals("null")) {
                            newLore.add("§e" + quote);
                        } else if (time != null && !time.equals("null")) {
                            newLore.add("§f" + TIME.getIcon() + " §e" + time);
                        }
                    }

                    newMeta.setLore(newLore);
                    savedItem.setItemMeta(newMeta);

                    // Update the player's item to match the saved data
                    if (!newMeta.equals(meta)) {  // Only update if the new meta is different
                        item.setItemMeta(newMeta);
                        Utils.successMsg1Value(p, "citems", "update.updated", "%item%", item.getItemMeta().getDisplayName());
                    }
                }
            }
        } else {
            log.sendWarning(p.getName() + ": Item \"" + item.getItemMeta().getDisplayName() + "%red_300%\" -> no valid ID");
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

    private void setStamp(ItemStack item) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String time = data.get(timeKey, PersistentDataType.STRING);
        String name = data.get(nameKey, PersistentDataType.STRING);
        String quote = data.get(quoteKey, PersistentDataType.STRING);

        System.out.println("Setting stamp: time=" + time + ", name=" + name);

        if (time == null || name == null) {
            System.out.println("Time or name is null, skipping stamp.");
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        name = parseUni(name);
        time = parseUni(time);
        lore.add("§7");
        lore.add("§f"+ SIGNED_BY.getIcon() +" §e" + name);
        if (quote.equals("null") || quote == null) {
            lore.add("§f" + TIME.getIcon() + " §e" + time);
        } else {
            lore.add("§e"+parseUni(quote));
        }

        meta.setLore(lore);
        item.setItemMeta(meta); // Ensure changes are applied
    }



    public void createStamp(Player player, ItemStack item, @Nullable String message) {
        if (item == null || player == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        String parsedTime = parseTime();
        String playerName = player.getName();
        String quote = "null";
        if (message != null) {
            quote = "\""+ message+ "\"";
        }
        if (nickManager.getNickname(player) != null) {
            playerName = nickManager.getNickname(player).replace("_", " ");
        }

        data.set(quoteKey, PersistentDataType.STRING, quote);
        data.set(timeKey, PersistentDataType.STRING, parsedTime);
        data.set(nameKey, PersistentDataType.STRING, playerName);



        item.setItemMeta(meta); // Ensure changes are applied
        setStamp(item); // Ensure stamping happens after setting data
    }

    private String parseUni(String s) {
        StringBuilder result = new StringBuilder();

        for (char c : s.toCharArray()) {
            Letters letterEnum = null;

            // Map each character to the corresponding enum value
            if (Character.isLetter(c)) {
                letterEnum = Letters.valueOf(String.valueOf(c).toUpperCase());
            } else if (Character.isDigit(c)) {
                switch (c) {
                    case '0': letterEnum = Letters.ZERO; break;
                    case '1': letterEnum = Letters.ONE; break;
                    case '2': letterEnum = Letters.TWO; break;
                    case '3': letterEnum = Letters.THREE; break;
                    case '4': letterEnum = Letters.FOUR; break;
                    case '5': letterEnum = Letters.FIVE; break;
                    case '6': letterEnum = Letters.SIX; break;
                    case '7': letterEnum = Letters.SEVEN; break;
                    case '8': letterEnum = Letters.EIGHT; break;
                    case '9': letterEnum = Letters.NINE; break;
                }
            } else if (c == '_') {
                letterEnum = Letters.UNDERSCORE;
            } else if (c == '-') {
                letterEnum = Letters.DASH;
            } else if (c == '"') {
                letterEnum = QUOTE;
            } else if (c == '&') {
                letterEnum = AMPERSAND;
            } else if (c == '(') {
                letterEnum = BRACKET_OPEN;
            } else if (c == ')') {
                letterEnum = BRACKET_CLOSED;
            } else if (c == ':') {
                letterEnum = COLON;
            } else if (c == '=') {
                letterEnum = EQUALS;
            } else if (c == '!') {
                letterEnum = EXCLAMATION;
            } else if (c == '#') {
                letterEnum = HASHTAG;
            } else if (c == '+') {
                letterEnum = PLUS;
            } else if (c == '?') {
                letterEnum = QUESTION;
            } else if (c == '/') {
                letterEnum = SLASH;
            } else if (c == ';') {
                letterEnum = SEMICOLON;
            } else if (c == '%') {
                letterEnum = PERCENTAGE;
            } else if (c == '.') {
                letterEnum = DOT;
            } else if (c == ',') {
                letterEnum = COMMA;
            } else if (c == '*') {
                letterEnum = STAR;
            }

            // Append the Unicode value or the original character if no mapping exists
            if (letterEnum != null) {
                result.append(letterEnum.getLetter());
            } else {
                result.append(c); // Keep non-mapped characters as is
            }
        }

        return result.toString();
    }


    private String parseTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return now.format(formatter);
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