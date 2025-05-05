package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.dataclasses.GlobalStat;
import me.hektortm.woSSystems.utils.dataclasses.Stat;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StatsDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "StatsDAO";

    public StatsDAO(DatabaseManager db, DAOHub daoHub) throws SQLException {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        String createStatsTable = """
            CREATE TABLE IF NOT EXISTS playerdata_stats (
                uuid CHAR(36) NOT NULL,
                id VARCHAR(255) NOT NULL,
                value BIGINT DEFAULT 0,
                PRIMARY KEY (uuid, id)
            );
        """;

        String createGlobalStatsTable = """
            CREATE TABLE IF NOT EXISTS global_stats (
                id VARCHAR(255) PRIMARY KEY,
                value BIGINT DEFAULT 0,
                max BIGINT DEFAULT 0,
                capped BOOLEAN DEFAULT FALSE
            );
        """;

        String createStatDefinitionsTable = """
            CREATE TABLE IF NOT EXISTS stats (
                id VARCHAR(255) PRIMARY KEY,
                max BIGINT DEFAULT 0,
                capped BOOLEAN DEFAULT FALSE
            );
        """;

        try (
                Connection conn = db.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createStatsTable);
            stmt.execute(createGlobalStatsTable);
            stmt.execute(createStatDefinitionsTable);
        }
    }

    public boolean isStatLimitReached(UUID uuid, String statId) {
        Stat stat = getAllStats().get(statId);
        if (stat == null || !stat.getCapped()) {
            return false;
        }

        long currentValue = getPlayerStatValue(uuid, statId);
        return currentValue >= stat.getMax();
    }

    public boolean isGlobalStatLimitReached(String statId) {
        GlobalStat stat = getAllGlobalStats().get(statId);
        if (stat == null || !stat.getCapped()) {
            return false;
        }

        long currentValue = getGlobalStatValue(statId);
        return currentValue >= stat.getMax();
    }




    /** Modifies a player's stat value */
    public void modifyPlayerStat(UUID uuid, String statId, long amount, Operations operation) {
        String sql = switch (operation) {
            case GIVE -> "INSERT INTO playerdata_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, id) DO UPDATE SET value = value + ?;";
            case TAKE -> "INSERT INTO playerdata_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, id) DO UPDATE SET value = value - ?;";
            case SET -> "INSERT INTO playerdata_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, id) DO UPDATE SET value = ?;";
            case RESET -> "INSERT INTO playerdata_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, id) DO UPDATE SET value = 0;";
        };

        Stat stat = getAllStats().get(statId);
        if (operation == Operations.GIVE) {
            if (stat.getCapped()) {
                long currentValue = getPlayerStatValue(uuid, statId);
                if (currentValue + amount > stat.getMax()) {
                    amount = stat.getMax() - currentValue;
                }
            }
        }

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);
            stmt.setLong(3, amount);
            stmt.setLong(4, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to modify Stat: "+e);
        }
    }

    /** Modifies a global stat value */
    public void modifyGlobalStatValue(String statId, long amount, Operations operation) {
        String sql = switch (operation) {
            case GIVE -> "UPDATE global_stats SET value = value + ? WHERE stat_id = ?;";
            case TAKE -> "UPDATE global_stats SET value = value - ? WHERE stat_id = ?;";
            case SET -> "UPDATE global_stats SET value = ? WHERE stat_id = ?;";
            case RESET -> "UPDATE global_stats SET value = 0 WHERE stat_id = ?;";
        };
        GlobalStat stat = getAllGlobalStats().get(statId);
        if (operation == Operations.GIVE) {
            if (stat.getCapped()) {
                long currentAmount = getGlobalStatValue(statId);
                if (currentAmount + amount > stat.getMax()) {
                    amount = stat.getMax() - currentAmount;
                }
            }
        }


        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, amount);
            stmt.setString(2, statId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to modify Global Stat: "+e);
        }
    }

    /** Retrieves all player stats */
    public Map<String, Stat> getAllStats() {
        Map<String, Stat> stats = new HashMap<>();
        String sql = "SELECT stat_id, max, capped FROM stats;";
        try ( Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("stat_id"), new Stat(
                        rs.getString("stat_id"),
                        rs.getLong("max"),
                        rs.getBoolean("capped")
                ));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Stats: "+e);
        }
        return stats;
    }

    /** Retrieves all global stats */
    public Map<String, GlobalStat> getAllGlobalStats() {
        Map<String, GlobalStat> stats = new HashMap<>();
        String sql = "SELECT * FROM global_stats;";
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("stat_id"), new GlobalStat(
                        rs.getString("stat_id"),
                        rs.getLong("max"),
                        rs.getBoolean("capped")
                ));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Global Stat: "+e);
        }
        return stats;
    }

    /** Retrieves a single player's stat value */
    public long getPlayerStatValue(UUID uuid, String statId) {
        String sql = "SELECT value FROM player_stats WHERE uuid = ? AND id = ?;";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Stat Value: "+e);
        }
        return 0;
    }

    /** Retrieves a global stat value */
    public long getGlobalStatValue(String statId) {
        String sql = "SELECT value FROM global_stats WHERE id = ?;";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Global Stat Value: "+e);
        }
        return 0;
    }
}
