package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.*;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GUIDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "GUIDAO";

    private final Map<String, GUI> cache = new ConcurrentHashMap<>();

    public GUIDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, GUI.class);
        SchemaManager.syncTable(db, GUIPage.class);
        SchemaManager.syncTable(db, GUISlot.class);
        SchemaManager.syncTable(db, GUISlotConfig.class);
        WoSSystems.getInstance().getServer().getScheduler()
                .runTaskAsynchronously(plugin, this::preloadGuis);
    }

    public void preloadGuis() {
        String sql = "SELECT id FROM guis";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    GUI gui = buildGui(conn, id);
                    if (gui != null) {
                        cache.put(id, gui);
                        count++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": failed to preload '" + id + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info(logName + ": preloaded " + count + " gui(s) into cache.");
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, logName + ":preload", "Failed to preload guis into cache: ", e));
        }
    }


    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT * FROM guis WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cache.put(id, buildGui(conn, id));
                p.sendTitle("§aUpdated Gui", "§e"+id );
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                p.sendTitle("§cDeleted Gui", "§e"+id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, logName + ":reload", "Failed to reload item from DB: ", e);
        }
    }

    public GUI getGUIbyId(String id) {
        return cache.computeIfAbsent(id, k -> {
            try (Connection conn = db.getConnection()) {
                return buildGui(conn, k);
            } catch (Exception e) {
                plugin.getLogger().warning(logName + ": failed to get gui '" + k + "': " + e.getMessage());
                return null;
            }
        });
    }

    // ── Single-connection build chain ────────────────────────────────────────

    private GUI buildGui(Connection conn, String id) throws SQLException {
        String sql = "SELECT title, size, type, open_actions, close_actions FROM guis WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                String title = rs.getString("title");
                int size = rs.getInt("size");
                String type = rs.getString("type");
                List<String> openActions = stringToList(rs.getString("open_actions"));
                List<String> closeActions = stringToList(rs.getString("close_actions"));
                List<GUIPage> pages = buildPages(conn, id);

                return new GUI(id, size, title, type, pages, openActions, closeActions);
            }
        }
    }

    private List<GUIPage> buildPages(Connection conn, String guiId) throws SQLException {
        String sql = "SELECT page_id FROM gui_pages WHERE gui_id = ?";
        List<GUIPage> pages = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, guiId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int pageId = rs.getInt("page_id");
                    List<GUISlot> slots = buildSlots(conn, guiId, pageId);
                    pages.add(new GUIPage(guiId, pageId, slots));
                }
            }
        }
        return pages;
    }

    private List<GUISlot> buildSlots(Connection conn, String guiId, int pageId) throws SQLException {
        String sql = "SELECT slot_id, active FROM gui_slots WHERE gui_id = ? AND page_id = ?";
        List<GUISlot> slots = new ArrayList<>();
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, guiId);
            s.setInt(2, pageId);
            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    int slotId = rs.getInt("slot_id");
                    boolean active = rs.getBoolean("active");
                    List<GUISlotConfig> configs = buildConfigs(conn, guiId, pageId, slotId);
                    slots.add(new GUISlot(guiId, pageId, slotId, active, configs));
                }
            }
        }
        return slots;
    }

    private List<GUISlotConfig> buildConfigs(Connection conn, String guiId, int pageId, int slotId) throws SQLException {
        String sql = "SELECT config_id, matchtype, amount, visible, material, display_name, lore, model, color, tooltip, enchanted, " +
                "global_actions, right_actions, left_actions, confirm, sound, checks " +
                "FROM gui_slot_configs WHERE gui_id = ? AND page_id = ? AND slot_id = ?";
        List<GUISlotConfig> configs = new ArrayList<>();
        try (PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, guiId);
            s.setInt(2, pageId);
            s.setInt(3, slotId);
            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    String configId = rs.getString("config_id");
                    String matchtype = rs.getString("matchtype");
                    int amount = rs.getInt("amount");
                    boolean visible = rs.getBoolean("visible");
                    String materialName = rs.getString("material");
                    String displayName = rs.getString("display_name");
                    String loreRaw = rs.getString("lore");
                    String model = rs.getString("model");
                    String color = rs.getString("color");
                    String tooltip = rs.getString("tooltip");
                    boolean enchanted = rs.getBoolean("enchanted");
                    List<String> globalActions = stringToList(rs.getString("global_actions"));
                    List<String> rightActions = stringToList(rs.getString("right_actions"));
                    List<String> leftActions = stringToList(rs.getString("left_actions"));
                    boolean confirm = rs.getBoolean("confirm");
                    String sound = rs.getString("sound");
                    String checks = rs.getString("checks");

                    ItemStack guiItem = buildItemStack(materialName, displayName, loreRaw, enchanted);

                    String conditionKey = guiId + ":" + pageId + ":" + slotId + ":" + configId;
                    List<Condition> conditions = hub.getConditionDAO().getConditions(ConditionType.GUISLOT, conditionKey);

                    configs.add(new GUISlotConfig(
                            guiId, pageId, slotId, configId, matchtype, amount,
                            visible, materialName, displayName, loreRaw, model, color, tooltip, enchanted, guiItem,
                            globalActions, rightActions, leftActions,
                            confirm, sound, buildChecks(checks), conditions
                    ));
                }
            }
        }
        return configs;
    }

    private List<GUICheck> buildChecks(String checkData) {
        List<GUICheck> checks = new ArrayList<>();
        if (checkData == null || checkData.isBlank()) return checks;

        try {
            JsonArray arr = JsonParser.parseString(checkData).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                CheckType type = CheckType.valueOf(obj.get("type").getAsString().toUpperCase());
                int amount = obj.get("amount").getAsInt();
                String id = obj.has("id") ? obj.get("id").getAsString() : null;
                checks.add(new GUICheck(type, id, amount));
            }
        } catch (Exception e) {
            plugin.getLogger().warning(logName + ": failed to parse checks JSON '" + checkData + "': " + e.getMessage());
        }
        return checks;
    }

    private ItemStack buildItemStack(String materialName, String displayName, String loreRaw, boolean enchanted) {
        Material material = materialName != null ? Material.getMaterial(materialName.toUpperCase()) : null;
        if (material == null) material = Material.PAPER;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(displayName);
        }
        if (loreRaw != null && !loreRaw.isEmpty()) {
            meta.setLore(stringToList(loreRaw));
        }
        if (enchanted) {
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        }
        item.setItemMeta(meta);
        return item;
    }

    public List<String> stringToList(String input) {
        if (input == null || input.isBlank()) return new ArrayList<>();
        return Arrays.stream(input.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}