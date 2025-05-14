package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConditionDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "ConditionDAO";

    public ConditionDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS conditions(" +
                    "type VARCHAR(255), " +
                    "type_id VARCHAR(255), " +
                    "condition_id INT," +
                    "condition_key VARCHAR(255), " +
                    "value VARCHAR(255), " +
                    "parameter VARCHAR(255), " +
                    "PRIMARY KEY (type, id, condition_key)" + // Composite key
                    ")");
        }
    }

    public List<Condition> getConditions(ConditionType type, String id) {
        List<Condition> result = new ArrayList<>();
        String sql = "SELECT * FROM conditions WHERE type = ? AND type_id = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type.getType());
            stmt.setString(2, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new Condition(
                        rs.getString("condition_key"),
                        rs.getString("value"),
                        rs.getString("parameter")
                ));
            }

        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to load conditions for [" + type + "/" + id + "]: " + e);
        }

        return result;
    }
}
