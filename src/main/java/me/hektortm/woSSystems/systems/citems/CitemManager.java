package me.hektortm.woSSystems.systems.citems;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.Rarities;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CitemManager {

    private final DAOHub hub;
    private final ConditionHandler conditionHandler;

    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)),WoSCore.getPlugin(WoSCore.class));


    public CitemManager(DAOHub hub) {
        this.hub = hub;
        this.conditionHandler = new ConditionHandler(hub);
    }


    public void giveCitem(CommandSender s, Player t, String id, Integer amount) {
        ItemStack itemToGive = hub.getCitemDAO().getCitem(id);

        if (itemToGive == null) {
            s.sendMessage("[Database] Item is §onull"); // TODO: lang message
            return;
        }

        itemToGive = applyDynamicLore(t, itemToGive);
        itemToGive.setAmount(amount);
        t.getInventory().addItem(itemToGive);
        t.playSound(t.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1 ,1);
        Utils.success(s, "citems", "given", "%amount%", String.valueOf(amount), "%id%", id, "%player%", t.getName());
    }

    /**
     * Resolves the custom rarity line and all dynamic lore entries (permission-based and
     * condition-based) into the item's visible lore.
     *
     * Layout after application:
     *   [0]   §<color><rarity_icon> <rarity_name>   ← rarity line (if custom rarity set)
     *   [1..] static lore lines from DB item
     *   [end] visible dynamic lines (perm / condition gated)
     *
     * Must be called with a fresh DB copy of the item so rarity/dynamic lines aren't doubled.
     */
    public ItemStack applyDynamicLore(Player p, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // 0 — Apply dynamic name (first matching condition wins; falls back to static display name)
        String dynNameJson = data.get(Keys.DYNAMIC_NAME.get(), PersistentDataType.STRING);
        if (dynNameJson != null) {
            try {
                for (JsonElement el : JsonParser.parseString(dynNameJson).getAsJsonArray()) {
                    JsonObject entry = el.getAsJsonObject();
                    if (!"condition".equals(entry.has("type") ? entry.get("type").getAsString() : "")) continue;
                    String condKey   = entry.get("condition").getAsString();
                    String value     = entry.has("value")     ? entry.get("value").getAsString()     : "";
                    String parameter = entry.has("parameter") ? entry.get("parameter").getAsString() : null;
                    if ("-".equals(parameter)) parameter = null;
                    if (conditionHandler.evaluate(p, new Condition(condKey, value, parameter), null)) {
                        meta.setDisplayName(entry.get("name").getAsString());
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 1 — Prepend custom rarity line
        String rarityName = data.get(Keys.CUSTOM_RARITY.get(), PersistentDataType.STRING);
        if (rarityName != null) {
            Rarities rarity = Rarities.valueOf(rarityName);
            lore.add(0, rarity.getLoreLine());
        }

        // 2 — Append dynamic (perm / condition) lore lines
        String dynJson = data.get(Keys.DYNAMIC_LORE.get(), PersistentDataType.STRING);
        if (dynJson != null) {
            try {
                JsonArray entries = JsonParser.parseString(dynJson).getAsJsonArray();
                for (JsonElement el : entries) {
                    JsonObject entry = el.getAsJsonObject();
                    if (!"condition".equals(entry.has("type") ? entry.get("type").getAsString() : "")) continue;
                    String text = entry.get("text").getAsString();
                    String condKey   = entry.get("condition").getAsString();
                    String value     = entry.has("value")     ? entry.get("value").getAsString()     : "";
                    String parameter = entry.has("parameter") ? entry.get("parameter").getAsString() : null;
                    if ("-".equals(parameter)) parameter = null;
                    boolean visible = conditionHandler.evaluate(p, new Condition(condKey, value, parameter), null);

                    if (visible) lore.add(text);
                }
            } catch (Exception ignored) {}
        }

        meta.setLore(lore);

        // 3 — Apply dynamic model (first matching entry wins; falls back to static model)
        String dynModelJson = data.get(Keys.DYNAMIC_MODEL.get(), PersistentDataType.STRING);
        if (dynModelJson != null) {
            try {
                JsonArray modelEntries = JsonParser.parseString(dynModelJson).getAsJsonArray();
                for (JsonElement el : modelEntries) {
                    JsonObject entry = el.getAsJsonObject();
                    if (!"condition".equals(entry.has("type") ? entry.get("type").getAsString() : "")) continue;
                    String modelId = entry.get("model").getAsString();
                    String condKey   = entry.get("condition").getAsString();
                    String value     = entry.has("value")     ? entry.get("value").getAsString()     : "";
                    String parameter = entry.has("parameter") ? entry.get("parameter").getAsString() : null;
                    if ("-".equals(parameter)) parameter = null;
                    boolean matches = conditionHandler.evaluate(p, new Condition(condKey, value, parameter), null);

                    if (matches) {
                        meta.setItemModel(new NamespacedKey("wos", modelId));
                        break; // first match wins
                    }
                }
            } catch (Exception ignored) {}
        }

        item.setItemMeta(meta);
        return item;
    }

    public boolean isCitem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(Keys.ID.get(), PersistentDataType.STRING); // Retrieve the ID

        return hub.getCitemDAO().getCitem(itemId) != null;
    }

    public void updateItem(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        int amount = item.getAmount();
        if (item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();

        if (meta == null) return;


        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(Keys.ID.get(), PersistentDataType.STRING);
        if (itemId == null) return;

        ItemStack dbItem = hub.getCitemDAO().getCitem(itemId);

            if (dbItem == null) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                Utils.success(p, "citems", "update.removed", "%item%", meta.getDisplayName());
                p.getInventory().remove(item);
                return;
            }



        if (!dbItem.isSimilar(item)) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            Utils.success(p, "citems", "update.updated", "%item%", meta.getDisplayName());

            // Update the item in hand with the new data
            dbItem.setAmount(amount);
            p.getInventory().setItemInMainHand(dbItem);
        }


    }

    public boolean hasCitemAmount(Player p, String id, int amount) {
        ItemStack citem = hub.getCitemDAO().getCitem(id);

        if (citem == null) return false;

        int found = 0;

        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) continue;

            if (item.isSimilar(citem)) {
                found += item.getAmount();
                if (found >= amount) {
                    return true;
                }
            }
        }

        return false;
    }


    private String parseTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return now.format(formatter);
    }



}