package me.hektortm.woSSystems.database.dao;

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
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    GUI gui = buildGui(id);
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

    public GUI getGUIbyId(String id) {
        return cache.computeIfAbsent(id, this::buildGui);
    }

    private GUI buildGui(String id) {
        String sql = "SELECT title, size, type, open_actions, close_actions FROM guis WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            String title = rs.getString("title");
            int size = rs.getInt("size");
            String type = rs.getString("type");
            List<String> openActions = stringToList(rs.getString("open_actions"));
            List<String> closeActions = stringToList(rs.getString("close_actions"));
            List<GUIPage> pages = buildPages(id);

            return new GUI(id, size, title, type, pages, openActions, closeActions);
        } catch (Exception e) {
            plugin.getLogger().warning(logName + ": failed to build gui '" + id + "': " + e.getMessage());
            return null;
        }
    }

    private List<GUIPage> buildPages(String guiId) {
        String sql = "SELECT page_id FROM gui_pages WHERE gui_id = ?";
        List<GUIPage> pages = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, guiId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int pageId = rs.getInt("page_id");
                List<GUISlot> slots = buildSlots(guiId, pageId);
                pages.add(new GUIPage(guiId, pageId, slots));
            }
        } catch (Exception e) {
            plugin.getLogger().warning(logName + ": failed to build pages for gui '" + guiId + "': " + e.getMessage());
        }
        return pages;
    }

    private List<GUISlot> buildSlots(String guiId, int pageId) {
        String sql = "SELECT slot_id, active FROM gui_slots WHERE gui_id = ? AND page_id = ?";
        List<GUISlot> slots = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, guiId);
            s.setInt(2, pageId);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                boolean active = rs.getBoolean("active");
                List<GUISlotConfig> configs = buildConfigs(guiId, pageId, slotId);
                slots.add(new GUISlot(guiId, pageId, slotId, active, configs));
            }
        } catch (Exception e) {
            plugin.getLogger().warning(logName + ": failed to build slots for gui '" + guiId + "' page " + pageId + ": " + e.getMessage());
        }
        return slots;
    }

    private List<GUISlotConfig> buildConfigs(String guiId, int pageId, int slotId) {
        String sql = "SELECT config_id, matchtype, visible, material, display_name, lore, mode, color, enchanted, " +
                "global_actions, right_actions, left_actions, confirm, sound, inv_check_id, inv_check_amount " +
                "FROM gui_slot_configs WHERE gui_id = ? AND page_id = ? AND slot_id = ?";
        List<GUISlotConfig> configs = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, guiId);
            s.setInt(2, pageId);
            s.setInt(3, slotId);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                String configId = rs.getString("config_id");
                String matchtype = rs.getString("matchtype");
                int amount = rs.getInt("amount");
                boolean visible = rs.getBoolean("visible");
                String materialName = rs.getString("material");
                String displayName = rs.getString("display_name");
                String loreRaw = rs.getString("lore");
                String mode = rs.getString("mode");
                String color = rs.getString("color");
                String tooltip = rs.getString("tooltip");
                boolean enchanted = rs.getBoolean("enchanted");
                List<String> globalActions = stringToList(rs.getString("global_actions"));
                List<String> rightActions = stringToList(rs.getString("right_actions"));
                List<String> leftActions = stringToList(rs.getString("left_actions"));
                boolean confirm = rs.getBoolean("confirm");
                String sound = rs.getString("sound");
                String invCheckId = rs.getString("inv_check_id");
                String invCheckAmount = rs.getString("inv_check_amount");

                ItemStack guiItem = buildItemStack(materialName, displayName, loreRaw, enchanted);

                String conditionKey = guiId + ":" + pageId + ":" + slotId + ":" + configId;
                List<Condition> conditions = hub.getConditionDAO().getConditions(ConditionType.GUISLOT, conditionKey);

                configs.add(new GUISlotConfig(
                        guiId, pageId, slotId, configId, matchtype, amount,
                        visible, materialName, displayName, loreRaw, mode, color, tooltip, enchanted, guiItem,
                        globalActions, rightActions, leftActions,
                        confirm, sound, invCheckId, invCheckAmount, conditions
                ));
            }
        } catch (Exception e) {
            plugin.getLogger().warning(logName + ": failed to build configs for gui '" + guiId + "' page " + pageId + " slot " + slotId + ": " + e.getMessage());
        }
        return configs;
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
