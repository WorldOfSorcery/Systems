package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.types.ConditionType;
import me.hektortm.woSSystems.utils.model.Condition;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * DAO for condition definitions attached to interactions, GUI slots, particles,
 * holograms, and other typed entities.
 *
 * <p>Results are cached per {@code (type, id)} key using {@link java.util.concurrent.ConcurrentHashMap}
 * and {@code computeIfAbsent}.  This prevents the per-tick DB storm that would
 * otherwise occur since conditions are checked on every tick for every active
 * entity.  Call {@link #invalidate(ConditionType, String)}
 * after updating a condition, or {@link #invalidateAll()} on a full reload.</p>
 *
 * <p>Table managed: {@code conditions}.</p>
 */
public class ConditionDAO implements IDAO {
    private final DatabaseManager db;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "ConditionDAO";

    private final Map<String, List<Condition>> cache = new ConcurrentHashMap<>();

    public ConditionDAO(DatabaseManager db) { this.db = db; }

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

    /**
     * Returns all conditions for the given typed entity, serving from cache on
     * subsequent calls.  The first call for a given {@code (type, id)} pair fetches
     * from the database and populates the cache.
     *
     * @param type the entity type owning the conditions
     * @param id   the entity ID
     * @return list of conditions; empty if none are defined
     */
    public List<Condition> getConditions(ConditionType type, String id) {
        return cache.computeIfAbsent(type.getType() + ":" + id, k -> fetchConditions(type, id));
    }

    private List<Condition> fetchConditions(ConditionType type, String id) {
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
            WoSSystems.discordLog(Level.SEVERE, "3800b1ef", "Failed to load conditions for [" + type + "/" + id + "]:", e);
        }

        return result;
    }

    /**
     * Evicts the cached conditions for the given typed entity so that the next
     * call to {@link #getConditions(ConditionType, String)} re-fetches from the DB.
     *
     * @param type the entity type
     * @param id   the entity ID
     */
    public void invalidate(ConditionType type, String id) {
        cache.remove(type.getType() + ":" + id);
    }

    /**
     * Clears the entire condition cache.  All subsequent reads will re-fetch from
     * the database.  Use on a full server reload.
     */
    public void invalidateAll() {
        cache.clear();
    }
}
