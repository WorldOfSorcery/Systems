package me.hektortm.woSSystems.database.dao;

import com.google.gson.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.FoodProperties;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import static io.papermc.paper.datacomponent.item.DyedItemColor.dyedItemColor;

public class CitemDAO implements IDAO {

    private final me.hektortm.wosCore.database.DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CitemDAO";

    public CitemDAO(me.hektortm.wosCore.database.DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    // ─── Schema ──────────────────────────────────────────────────────────────

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            // Original tables
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS items (
                        id        VARCHAR(40) NOT NULL,
                        item_data TEXT        NOT NULL,
                        web_data  JSON        NOT NULL,
                        item_json TEXT,
                        PRIMARY KEY (id)
                    )""");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS placed_citems (
                        citem_id         VARCHAR(255) NOT NULL,
                        owner_uuid       CHAR(36)     NOT NULL,
                        block_location   VARCHAR(255) NOT NULL,
                        display_location VARCHAR(255) NOT NULL,
                        creative_placed  BOOLEAN      NOT NULL DEFAULT FALSE,
                        PRIMARY KEY (citem_id)
                    )""");

            // Migration: add item_json column if missing
            try {
                stmt.execute("ALTER TABLE items ADD COLUMN item_json TEXT");
                plugin.getLogger().info(logName + ": added item_json column.");
            } catch (SQLException ignored) {
                // Column already exists – normal on subsequent starts
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:adc901cc", "Failed to initialize CitemDAO table: ", e);
        } finally {
            plugin.getLogger().info(logName + ": tables ready.");
        }

        // Fill item_json for existing rows that were saved before this migration
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::migrateBase64ToJson);
    }

    /** One-time migration: serialise any item that has no item_json yet. */
    private void migrateBase64ToJson() {
        String selectSql = "SELECT id, item_data FROM items WHERE item_json IS NULL";
        String updateSql = "UPDATE items SET item_json = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement sel = conn.prepareStatement(selectSql);
             PreparedStatement upd = conn.prepareStatement(updateSql)) {

            ResultSet rs = sel.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    ItemStack item = itemStackFromBase64(rs.getString("item_data"));
                    String json = itemStackToFullJson(id, item).toString();
                    upd.setString(1, json);
                    upd.setString(2, id);
                    upd.executeUpdate();
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": could not migrate item '" + id + "': " + e.getMessage());
                }
            }
            if (count > 0) plugin.getLogger().info(logName + ": migrated " + count + " item(s) to JSON.");

        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:mig001", "Migration Base64→JSON failed: ", e);
        }
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public List<String> getCitemIds() {
        List<String> ids = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM items")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getString("id"));
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:7e0bb0a0", "Failed to get Citem IDs: ", e);
        }
        return ids;
    }

    public void saveCitem(String id, ItemStack item) {
        String base64 = itemStackToBase64(item);
        String json   = itemStackToFullJson(id, item).toString();
        String legacy = buildLegacyWebData(id, item).toString();

        String sql = "INSERT INTO items (id, item_data, web_data, item_json) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, base64);
            stmt.setString(3, legacy);
            stmt.setString(4, json);
            stmt.execute();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "e70ccfb9", "Failed to save Citem: ", e);
        }
    }

    public void updateCitem(String id, ItemStack item) {
        String base64 = itemStackToBase64(item);
        String json   = itemStackToFullJson(id, item).toString();
        String legacy = buildLegacyWebData(id, item).toString();

        String sql = "UPDATE items SET item_data = ?, web_data = ?, item_json = ? WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, base64);
            stmt.setString(2, legacy);
            stmt.setString(3, json);
            stmt.setString(4, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "24835abe", "Failed to update Citem: ", e);
        }
    }

    /** Saves (or replaces) a Citem directly from a JSON string. Intended for the Web GUI. */
    public void saveCitemFromJson(String id, String jsonStr) {
        try {
            ItemStack item = itemStackFromFullJson(id, jsonStr);
            String base64 = itemStackToBase64(item);
            String legacy = buildLegacyWebData(id, item).toString();

            String sql = "INSERT OR REPLACE INTO items (id, item_data, web_data, item_json) VALUES (?, ?, ?, ?)";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                stmt.setString(2, base64);
                stmt.setString(3, legacy);
                stmt.setString(4, jsonStr);
                stmt.execute();
            }
        } catch (Exception e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:wj001", "Failed to save Citem from JSON: ", e);
        }
    }

    /** Returns the comprehensive item_json string for a given ID, or null. */
    public String getCitemJson(String id) {
        String sql = "SELECT item_json FROM items WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("item_json");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:gj001", "Failed to get Citem JSON: ", e);
        }
        return null;
    }

    /** Returns a JSON array of all item_json entries — useful for the web GUI list endpoint. */
    public JsonArray getAllCitemsJson() {
        JsonArray result = new JsonArray();
        String sql = "SELECT id, item_json FROM items";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String raw = rs.getString("item_json");
                if (raw != null) {
                    result.add(JsonParser.parseString(raw));
                } else {
                    // Fallback: minimal object with just the id
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", rs.getString("id"));
                    result.add(obj);
                }
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:gaj001", "Failed to get all Citem JSONs: ", e);
        }
        return result;
    }

    /**
     * Retrieves an ItemStack. Prefers item_json (comprehensive JSON); falls back
     * to item_data (Base64) for items saved before the migration.
     */
    public ItemStack getCitem(String id) {
        String sql = "SELECT item_data, item_json FROM items WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            String json   = rs.getString("item_json");
            String base64 = rs.getString("item_data");

            if (json != null && !json.isBlank()) {
                return itemStackFromFullJson(id, json);
            }
            return itemStackFromBase64(base64);

        } catch (Exception e) {
            WoSSystems.discordLog(Level.SEVERE, "de1adeb7", "Failed to retrieve Citem: ", e);
            return null;
        }
    }

    public void deleteCitem(String id) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "a3f1c200", "Failed to delete Citem: ", e);
        }
    }

    public boolean citemExists(String id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM items WHERE id = ?")) {
            stmt.setString(1, id);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "ce7f7083", "Failed to check existing Citem: ", e);
            return false;
        }
    }

    // ─── Placed display helpers (unchanged) ──────────────────────────────────

    public void createItemDisplay(String id, UUID ownerUUID, Location blockLocation, Location displayLocation, boolean isCreative) {
        String sql = "INSERT INTO placed_citems (citem_id, owner_uuid, block_location, display_location, creative_placed) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, ownerUUID.toString());
            pstmt.setString(3, Parsers.locationToString(blockLocation));
            pstmt.setString(4, Parsers.locationToString(displayLocation));
            pstmt.setBoolean(5, isCreative);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "9e2bb566", "Failed to create Item Display", e);
        }
    }

    public void removeItemDisplay(UUID ownerUUID, Location location) {
        String sql = "DELETE FROM placed_citems WHERE owner_uuid = ? AND block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ownerUUID.toString());
            pstmt.setString(2, Parsers.locationToString(location));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "4e63206a", "Failed to remove Item Display", e);
        }
    }

    public UUID getUUID(Location location) {
        String sql = "SELECT owner_uuid FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(location));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return UUID.fromString(rs.getString("owner_uuid"));
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "b1ebfe0d", "Failed to get Owner UUID", e);
        }
        return null;
    }

    public void changeDisplay(Location oldLocation, Location newLocation) {
        String sql = "UPDATE placed_citems SET display_location = ? WHERE display_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(newLocation));
            pstmt.setString(2, Parsers.locationToString(oldLocation));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "621ebf18", "Failed to change Item Display", e);
        }
    }

    public boolean isCreativePlaced(Location location) {
        String sql = "SELECT creative_placed FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(location));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getBoolean("creative_placed");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "d3041663", "Failed to check Creative Placed", e);
        }
        return false;
    }

    public Location getDisplayLocation(Location location) {
        String sql = "SELECT display_location FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(location));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return Parsers.stringToLocation(rs.getString("display_location"));
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "cbf86e39", "Failed to get Display Location", e);
        }
        return null;
    }

    public String getItemDisplayID(Location location) {
        String sql = "SELECT citem_id FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(location));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("citem_id");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "9b378252", "Failed to get Display ID", e);
        }
        return null;
    }

    public boolean isItemDisplay(Location location) {
        String sql = "SELECT 1 FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(location));
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "593f879f", "Failed to check Item Display", e);
            return false;
        }
    }

    public boolean isItemDisplayOwner(Location location, UUID uuid) {
        String sql = "SELECT 1 FROM placed_citems WHERE block_location = ? AND owner_uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(location));
            pstmt.setString(2, uuid.toString());
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "f2863bfe", "Failed to check Display Owner", e);
        }
        return false;
    }

    // ─── Comprehensive JSON serialisation ────────────────────────────────────

    /**
     * Converts an ItemStack to the full JSON schema that the web GUI reads/writes.
     * Every property managed by the Citem system is included.
     */
    public JsonObject itemStackToFullJson(String id, ItemStack item) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("material", item.getType().name());

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return obj;

        // ── Display name & lore ──
        obj.addProperty("display_name", meta.hasDisplayName() ? meta.getDisplayName() : "");

        JsonArray loreArr = new JsonArray();
        if (meta.hasLore()) for (String l : meta.getLore()) loreArr.add(l);
        obj.add("lore", loreArr);

        // ── ItemMeta flags ──
        obj.addProperty("unbreakable", meta.isUnbreakable());
        Boolean glint = meta.getEnchantmentGlintOverride();
        if (glint != null) obj.addProperty("enchant_glint", glint);
        obj.addProperty("hide_tooltip", meta.isHideTooltip());
        obj.addProperty("hide_flags", !meta.getItemFlags().isEmpty());

        // ── Model / Tooltip style ──
        NamespacedKey model = meta.getItemModel();
        if (model != null) obj.addProperty("item_model", model.toString());
        NamespacedKey tooltip = meta.getTooltipStyle();
        if (tooltip != null) obj.addProperty("tooltip_style", tooltip.toString());

        // ── PDC ──
        PersistentDataContainer data = meta.getPersistentDataContainer();

        String rarity = data.get(Keys.CUSTOM_RARITY.get(), PersistentDataType.STRING);
        if (rarity != null) obj.addProperty("custom_rarity", rarity);

        JsonObject actions = new JsonObject();
        actions.addProperty("left",   nullable(data.get(Keys.LEFT_ACTION.get(),   PersistentDataType.STRING)));
        actions.addProperty("right",  nullable(data.get(Keys.RIGHT_ACTION.get(),  PersistentDataType.STRING)));
        actions.addProperty("placed", nullable(data.get(Keys.PLACED_ACTION.get(), PersistentDataType.STRING)));
        obj.add("actions", actions);

        JsonObject flags = new JsonObject();
        flags.addProperty("undroppable", data.has(Keys.UNDROPPABLE.get(), PersistentDataType.BOOLEAN));
        flags.addProperty("unusable",    data.has(Keys.UNUSABLE.get(),    PersistentDataType.BOOLEAN));
        Integer placeable = data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER);
        flags.addProperty("placeable", placeable != null ? placeable : 0);
        obj.add("flags", flags);

        String dynName = data.get(Keys.DYNAMIC_NAME.get(), PersistentDataType.STRING);
        obj.add("dynamic_name", dynName != null
                ? JsonParser.parseString(dynName).getAsJsonArray() : new JsonArray());

        String dynLore = data.get(Keys.DYNAMIC_LORE.get(), PersistentDataType.STRING);
        obj.add("dynamic_lore", dynLore != null
                ? JsonParser.parseString(dynLore).getAsJsonArray() : new JsonArray());

        String dynModel = data.get(Keys.DYNAMIC_MODEL.get(), PersistentDataType.STRING);
        obj.add("dynamic_model", dynModel != null
                ? JsonParser.parseString(dynModel).getAsJsonArray() : new JsonArray());

        // ── Attributes ──
        JsonArray attrArr = new JsonArray();
        Map<Attribute, Collection<AttributeModifier>> mods = meta.getAttributeModifiers().asMap();
        if (mods != null) {
            for (Map.Entry<Attribute, Collection<AttributeModifier>> e : mods.entrySet()) {
                for (AttributeModifier m : e.getValue()) {
                    JsonObject a = new JsonObject();
                    a.addProperty("attribute", e.getKey().getKey().toString());
                    a.addProperty("amount",    m.getAmount());
                    a.addProperty("operation", m.getOperation().name());
                    a.addProperty("slot",      slotGroupToString(m.getSlotGroup()));
                    attrArr.add(a);
                }
            }
        }
        obj.add("attributes", attrArr);

        // ── Equippable ──
        EquippableComponent eq = meta.getEquippable();
        if (eq != null && eq.getSlot() != null) {
            obj.addProperty("equippable_slot", eq.getSlot().name());
        }

        // ── Data components ──
        Integer maxStack = item.getData(DataComponentTypes.MAX_STACK_SIZE);
        if (maxStack != null) obj.addProperty("max_stack_size", maxStack);

        DyedItemColor dyed = item.getData(DataComponentTypes.DYED_COLOR);
        if (dyed != null) {
            Color c = dyed.color();
            obj.addProperty("color", String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
        }

        FoodProperties food = item.getData(DataComponentTypes.FOOD);
        if (food != null) {
            JsonObject fo = new JsonObject();
            fo.addProperty("nutrition",     food.nutrition());
            fo.addProperty("saturation",    food.saturation());
            fo.addProperty("can_always_eat", food.canAlwaysEat());
            obj.add("food", fo);
        }

        return obj;
    }

    /**
     * Reconstructs an ItemStack from the comprehensive JSON schema.
     * The {@code id} is written into the item's PDC so it behaves as a proper Citem.
     */
    public ItemStack itemStackFromFullJson(String id, String jsonStr) {
        JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();

        Material material = Material.valueOf(obj.get("material").getAsString().toUpperCase());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // ── Display name & lore ──
        if (obj.has("display_name") && !obj.get("display_name").isJsonNull())
            meta.setDisplayName(obj.get("display_name").getAsString());

        if (obj.has("lore")) {
            List<String> lore = new ArrayList<>();
            for (JsonElement el : obj.getAsJsonArray("lore")) lore.add(el.getAsString());
            meta.setLore(lore);
        }

        // ── ItemMeta flags ──
        if (obj.has("unbreakable"))
            meta.setUnbreakable(obj.get("unbreakable").getAsBoolean());
        if (obj.has("enchant_glint") && !obj.get("enchant_glint").isJsonNull())
            meta.setEnchantmentGlintOverride(obj.get("enchant_glint").getAsBoolean());
        if (obj.has("hide_tooltip"))
            meta.setHideTooltip(obj.get("hide_tooltip").getAsBoolean());
        if (obj.has("hide_flags") && obj.get("hide_flags").getAsBoolean())
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
                    ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ARMOR_TRIM,
                    ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_STORED_ENCHANTS, ItemFlag.HIDE_DYE);

        // ── Model / Tooltip ──
        if (obj.has("item_model") && !obj.get("item_model").isJsonNull())
            meta.setItemModel(parseNamespacedKey(obj.get("item_model").getAsString()));
        if (obj.has("tooltip_style") && !obj.get("tooltip_style").isJsonNull())
            meta.setTooltipStyle(parseNamespacedKey(obj.get("tooltip_style").getAsString()));

        // ── PDC ──
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(Keys.ID.get(), PersistentDataType.STRING, id);

        if (obj.has("custom_rarity") && !obj.get("custom_rarity").isJsonNull())
            data.set(Keys.CUSTOM_RARITY.get(), PersistentDataType.STRING,
                    obj.get("custom_rarity").getAsString());

        if (obj.has("actions")) {
            JsonObject acts = obj.getAsJsonObject("actions");
            setIfPresent(data, Keys.LEFT_ACTION,   acts, "left");
            setIfPresent(data, Keys.RIGHT_ACTION,  acts, "right");
            setIfPresent(data, Keys.PLACED_ACTION, acts, "placed");
        }

        if (obj.has("flags")) {
            JsonObject fl = obj.getAsJsonObject("flags");
            if (fl.has("undroppable") && fl.get("undroppable").getAsBoolean())
                data.set(Keys.UNDROPPABLE.get(), PersistentDataType.BOOLEAN, true);
            if (fl.has("unusable") && fl.get("unusable").getAsBoolean())
                data.set(Keys.UNUSABLE.get(), PersistentDataType.BOOLEAN, true);
            int placeable = fl.has("placeable") ? fl.get("placeable").getAsInt() : 0;
            if (placeable > 0) data.set(Keys.PLACEABLE.get(), PersistentDataType.INTEGER, placeable);
        }

        if (obj.has("dynamic_name"))
            data.set(Keys.DYNAMIC_NAME.get(), PersistentDataType.STRING,
                    obj.get("dynamic_name").toString());

        if (obj.has("dynamic_lore"))
            data.set(Keys.DYNAMIC_LORE.get(), PersistentDataType.STRING,
                    obj.get("dynamic_lore").toString());

        if (obj.has("dynamic_model"))
            data.set(Keys.DYNAMIC_MODEL.get(), PersistentDataType.STRING,
                    obj.get("dynamic_model").toString());

        // ── Attributes ──
        if (obj.has("attributes")) {
            for (JsonElement el : obj.getAsJsonArray("attributes")) {
                JsonObject a = el.getAsJsonObject();
                Attribute attribute = Registry.ATTRIBUTE.get(
                        NamespacedKey.fromString(a.get("attribute").getAsString()));
                if (attribute == null) continue;
                double amount = a.get("amount").getAsDouble();
                AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(
                        a.get("operation").getAsString().toUpperCase());
                EquipmentSlotGroup slot = stringToSlotGroup(a.has("slot")
                        ? a.get("slot").getAsString() : "any");
                meta.addAttributeModifier(attribute, new AttributeModifier(
                        new NamespacedKey("wos", "citem_" + attribute.getKey().getKey() + "_" + slotGroupToString(slot)),
                        amount, op, slot));
            }
        }

        // ── Equippable ──
        if (obj.has("equippable_slot") && !obj.get("equippable_slot").isJsonNull()) {
            EquipmentSlot slot = EquipmentSlot.valueOf(obj.get("equippable_slot").getAsString().toUpperCase());
            EquippableComponent comp = meta.getEquippable();
            comp.setSlot(slot);
            meta.setEquippable(comp);
        }

        item.setItemMeta(meta);

        // ── Data components (applied after setItemMeta) ──
        if (obj.has("max_stack_size") && !obj.get("max_stack_size").isJsonNull())
            item.setData(DataComponentTypes.MAX_STACK_SIZE, obj.get("max_stack_size").getAsInt());

        if (obj.has("fire_resistant") && obj.get("fire_resistant").getAsBoolean())
            item.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        if (obj.has("color") && !obj.get("color").isJsonNull()) {
            Color color = hexToColor(obj.get("color").getAsString());
            if (color != null) item.setData(DataComponentTypes.DYED_COLOR, dyedItemColor(color));
        }

        if (obj.has("food") && !obj.get("food").isJsonNull()) {
            JsonObject fo = obj.getAsJsonObject("food");
            item.setData(DataComponentTypes.FOOD, FoodProperties.food()
                    .nutrition(fo.get("nutrition").getAsInt())
                    .saturation(fo.get("saturation").getAsFloat())
                    .canAlwaysEat(fo.has("can_always_eat") && fo.get("can_always_eat").getAsBoolean())
                    .build());
        }

        return item;
    }

    // ─── Base64 (kept for backward compat / fallback) ─────────────────────────

    public static String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BukkitObjectOutputStream bOut = new BukkitObjectOutputStream(out);
            bOut.writeObject(item);
            bOut.close();
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            WoSSystems.discordLog(Level.SEVERE, "9592f4b1", "Failed to build Base64 String", e);
            throw new RuntimeException(e);
        }
    }

    public ItemStack itemStackFromBase64(String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            ObjectInputStream in = new BukkitObjectInputStream(new ByteArrayInputStream(data));
            ItemStack item = (ItemStack) in.readObject();
            in.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            WoSSystems.discordLog(Level.SEVERE, "d270e031", "Failed to deserialize ItemStack from Base64", e);
            throw new RuntimeException(e);
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /** Minimal legacy JSON for the web_data column (kept for backward compat). */
    private JsonObject buildLegacyWebData(String id, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        JsonObject obj = new JsonObject();
        obj.addProperty("material", item.getType().name());
        obj.addProperty("display_name", meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "");
        JsonArray lore = new JsonArray();
        if (meta != null && meta.hasLore()) for (String l : meta.getLore()) lore.add(l);
        obj.add("lore", lore);
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            obj.addProperty("right-click", data.get(Keys.RIGHT_ACTION.get(), PersistentDataType.STRING));
            obj.addProperty("left-click",  data.get(Keys.LEFT_ACTION.get(),  PersistentDataType.STRING));
        }
        return obj;
    }

    private static void setIfPresent(PersistentDataContainer data, Keys key, JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            String val = obj.get(field).getAsString();
            if (!val.isBlank()) data.set(key.get(), PersistentDataType.STRING, val);
        }
    }

    private static String nullable(String s) {
        return s != null ? s : "";
    }

    private static NamespacedKey parseNamespacedKey(String s) {
        String[] parts = s.split(":", 2);
        return parts.length == 2 ? new NamespacedKey(parts[0], parts[1]) : NamespacedKey.minecraft(s);
    }

    private static Color hexToColor(String hex) {
        if (hex == null) return null;
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6) return null;
        try {
            return Color.fromRGB(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String slotGroupToString(EquipmentSlotGroup g) {
        if (g == EquipmentSlotGroup.MAINHAND) return "mainhand";
        if (g == EquipmentSlotGroup.OFFHAND)  return "offhand";
        if (g == EquipmentSlotGroup.HEAD)     return "head";
        if (g == EquipmentSlotGroup.CHEST)    return "chest";
        if (g == EquipmentSlotGroup.LEGS)     return "legs";
        if (g == EquipmentSlotGroup.FEET)     return "feet";
        if (g == EquipmentSlotGroup.ARMOR)    return "armor";
        return "any";
    }

    private static EquipmentSlotGroup stringToSlotGroup(String s) {
        return switch (s.toLowerCase()) {
            case "mainhand" -> EquipmentSlotGroup.MAINHAND;
            case "offhand"  -> EquipmentSlotGroup.OFFHAND;
            case "head"     -> EquipmentSlotGroup.HEAD;
            case "chest"    -> EquipmentSlotGroup.CHEST;
            case "legs"     -> EquipmentSlotGroup.LEGS;
            case "feet"     -> EquipmentSlotGroup.FEET;
            case "armor"    -> EquipmentSlotGroup.ARMOR;
            default         -> EquipmentSlotGroup.ANY;
        };
    }
}
