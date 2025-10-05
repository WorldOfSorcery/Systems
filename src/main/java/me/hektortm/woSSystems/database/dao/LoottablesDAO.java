package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.LoottableItemType;
import me.hektortm.woSSystems.utils.dataclasses.Loottable;
import me.hektortm.woSSystems.utils.dataclasses.LoottableItem;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class LoottablesDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CitemDAO";

    public LoottablesDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection connection = db.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS loottables (
                    id VARCHAR(60) NOT NULL,
                    PRIMARY KEY (id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS `loottable_items` (
                    `id` VARCHAR(60) NOT NULL,
                    `weight` INT NOT NULL,
                    `type` VARCHAR(255) NOT NULL,
                    `value` VARCHAR(255) NOT NULL,
                    `parameter` INT,
                    PRIMARY KEY (`id`)
                )
               """);
        }
    }

    public Loottable getLoottable(String id) {
        Loottable lt = null;
        String sql = "SELECT * FROM loottable_items WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);

            ResultSet rs = pstmt.executeQuery();
            List<LoottableItem> items = new ArrayList<>();
            while (rs.next()) {
                int weight = rs.getInt("weight");
                String value = rs.getString("value");
                LoottableItemType type = parseItemType(rs.getString("type"));
                if (type == null) {
                    DiscordLogger.log(new DiscordLog(Level.WARNING, plugin, "8e7aaad0", "Invalid Item Type", null));
                    return null;
                }


                int parameter = rs.getInt("parameter");



                items.add(new LoottableItem(weight, type, value, parameter));
                lt = new Loottable(id, items);
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
