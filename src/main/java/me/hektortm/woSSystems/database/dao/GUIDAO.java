package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.*;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GUIDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "GUIDAO";

    public GUIDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS guis(" +
                    "id VARCHAR(255) NOT NULL," +
                    "size INTEGER NOT NULL," +
                    "title VARCHAR(255)," +
                    "open_actions TEXT," +
                    "close_actions TEXT" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS gui_slots(" +
                    "gui_id VARCHAR(255) NOT NULL," +
                    "slot INTEGER NOT NULL," +
                    "material VARCHAR(255)," +
                    "display_name VARCHAR(255)," +
                    "lore TEXT," +
                    "custom_model_data INTEGER," +
                    "enchanted BOOLEAN,"+
                    "right_click TEXT," +
                    "left_click TEXT," +
                    "visible BOOLEAN NOT NULL DEFAULT false)");
        }
    }

    public List<GUISlot> getSlotsForID(String id) {
        List<GUISlot> slots = new ArrayList<>();

        String sql = "SELECT * FROM gui_slots WHERE gui_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int slot = rs.getInt("slot");
                Material material = Material.getMaterial(rs.getString("material"));
                String displayName = rs.getString("display_name");
                String loreRaw = rs.getString("lore");
                int customModelData = rs.getInt("custom_model_data");
                boolean enchanted = rs.getBoolean("enchanted");
                String rightClickRaw = rs.getString("right_click");
                String leftClickRaw = rs.getString("left_click");
                boolean visible = rs.getBoolean("visible");

                // Assuming actions are stored like ["cmd1", "cmd2"]
                List<String> lore = Arrays.stream(loreRaw.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());
                List<String> parsedRightClick = Arrays.stream(rightClickRaw.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());
                List<String> parsedLeftClick = Arrays.stream(leftClickRaw.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());



                slots.add(new GUISlot(id, slot, material, displayName, lore, customModelData, enchanted, parsedRightClick, parsedLeftClick, visible));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Slots for "+id+": "+e);
        }
        return slots;
    }

    public GUI getGUIbyId(String id) {
        String sql = "SELECT * FROM guis WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String interactionId = rs.getString("id");
                int size = rs.getInt("size");
                String title = rs.getString("title");
                List<GUISlot> slots = getSlotsForID(interactionId);
                String openActions = rs.getString("open_actions");
                String closeActions = rs.getString("close_actions");

                List<String> parsedOpenActions = Arrays.stream(openActions.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());

                List<String> parsedCloseActions = Arrays.stream(closeActions.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());

                return new GUI(id, size, title, slots, parsedOpenActions, parsedCloseActions);
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get GUI for "+id+": "+e);
        }
        return null;
    }

    public List<GUI> getGUIs() {
        List<GUI> guis = new ArrayList<>();
        String sql = "SELECT * FROM gui";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                guis.add(getGUIbyId(id));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get GUIs: "+e);
        }
        return guis;
    }

}
