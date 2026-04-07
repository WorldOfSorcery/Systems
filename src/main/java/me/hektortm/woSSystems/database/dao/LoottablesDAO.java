package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.model.Unlockable;
import me.hektortm.woSSystems.utils.types.LoottableItemType;
import me.hektortm.woSSystems.utils.model.Loottable;
import me.hektortm.woSSystems.utils.model.LoottableItem;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * DAO for loading loot table definitions from the {@code loottables} and
 * {@code loottable_items} tables.  Each loot table has a set of weighted items
 * that can resolve to a GUI, interaction, dialog, citem, or command.
 */
public class LoottablesDAO implements IDAO {
    private final DatabaseManager db;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CitemDAO";

    private final Map<String, Loottable> cache = new ConcurrentHashMap<>();

    public LoottablesDAO(DatabaseManager db) { this.db = db; }

    @Override
    public void initializeTable() throws SQLException {
        // Syncs 'loottables' and auto-adds missing 'name' column
        SchemaManager.syncTable(db, Loottable.class);

        // Child table is not entity-backed — keep manual
        try (Connection connection = db.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS loottable_items (
                    loottable_id VARCHAR(255) NOT NULL,
                    item_id INT NOT NULL,
                    weight INT NOT NULL,
                    type VARCHAR(255) NOT NULL,
                    value VARCHAR(255) NOT NULL,
                    parameter INT
                )
            """);
        }

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::preloadAll);
    }

    public void preloadAll() {
        String sql = "SELECT id, amount, name FROM loottables";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                Loottable lt = new Loottable(
                        id,
                        rs.getInt("amount"),
                        rs.getString("name"),
                        buildLoottableItems(conn, id)
                );
                cache.put(rs.getString("id"), lt);
                count++;

            }
            plugin.getLogger().info("DialogDAO: preloaded " + count + " dialog(s) into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "DialogDAO:preload", "Failed to preload dialogs: ", e);
        }
    }

    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT amount, name FROM loottables WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Loottable lt = new Loottable(
                        id,
                        rs.getInt("amount"),
                        rs.getString("name"),
                        buildLoottableItems(conn, id)
                );
                cache.put(id, lt);
                p.sendTitle("§aUpdated Loottable", "§e" + id);
            } else {
                cache.remove(id);
                p.sendTitle("§cDeleted Loottable", "§e" + id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, logName + ":reload", "Failed to reload interaction from DB: ", e);
        }
    }


    public List<LoottableItem> buildLoottableItems(Connection conn, String id) {
        String sql = "SELECT * FROM loottable_items WHERE id = ?";
        List<LoottableItem> items = new ArrayList<>();
        try (conn; PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LoottableItem item = new LoottableItem(
                        rs.getInt("weight"),
                        LoottableItemType.valueOf(rs.getString("type")),
                        rs.getString("value"),
                        rs.getInt("parameter")
                );
                items.add(item);
            }

        } catch (SQLException e) {
            plugin.writeLog("LoottablesDAO", Level.SEVERE, "Couldnt build items");
        }
        return items;
    }

    /**
     * Returns the number of items to award when rolling the given loot table.
     *
     * @param id the loot table ID
     * @return the configured item count, or {@code 0} if the table is not found
     */
    public int getAmount(String id) {
        String sql = "SELECT amount FROM loottables WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("amount");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "30d8689d",
                    "Failed to get Loottable Amount: " +
                            "\nID: "+id, e
            ));
        }
        return 0;
    }
    /**
     * Returns the display name of the loot table, or {@code null} if not set
     * or the table doesn't exist.
     *
     * @param id the loot table ID
     * @return the display name, or {@code null}
     */
    public String getName(String id) {
        String sql = "SELECT name FROM loottables WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name") != null ? rs.getString("name") : null;
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "30d8689d",
                    "Failed to get Loottable Name: " +
                            "\nID: "+id, e
            ));
        }
        return null;
    }

    /**
     * Builds a {@link Loottable} object for the given ID, including all weighted
     * {@link LoottableItem} entries.  Returns {@code null} if the table has no
     * items or an entry contains an unrecognised {@link LoottableItemType}.
     *
     * @param id the loot table ID
     * @return the populated loot table, or {@code null}
     */
    public Loottable getLoottable(String id) {
        Loottable lt = null;
        String sql = "SELECT * FROM loottable_items WHERE loottable_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);

            ResultSet rs = pstmt.executeQuery();
            List<LoottableItem> items = new ArrayList<>();
            while (rs.next()) {
                int weight = rs.getInt("weight");
                String value = rs.getString("value");
                String name = getName(id);
                LoottableItemType type = parseItemType(rs.getString("type"));
                if (type == null) {
                    DiscordLogger.log(new DiscordLog(Level.WARNING, plugin, "8e7aaad0", "Invalid Item Type", null));
                    return null;
                }


                int parameter = rs.getInt("parameter");



                items.add(new LoottableItem(weight, type, value, parameter));
                lt = new Loottable(id, getAmount(id),name != null ? name : id,items);
            }

        } catch(SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "30d8689d",
                    "Failed to get Loottable: " +
                            "\nID: "+id, e
            ));
        }
        return lt;
    }


    private LoottableItemType parseItemType(String itemType) {
        if (itemType == null) return null;

        String key = itemType.trim().toLowerCase(java.util.Locale.ROOT);

        return switch (key) {
            case "gui" -> LoottableItemType.GUI;
            case "interaction" -> LoottableItemType.INTERACTION;
            case "dialog" -> LoottableItemType.DIALOG;
            case "citem" -> LoottableItemType.CITEM;
            case "command" -> LoottableItemType.COMMAND;
            default -> {
                DiscordLogger.log(new DiscordLog(
                        Level.WARNING, plugin, "8e7aaad0",
                        "Invalid Item Type: " + itemType, null
                ));
                yield null;
            }
        };
    }

}
