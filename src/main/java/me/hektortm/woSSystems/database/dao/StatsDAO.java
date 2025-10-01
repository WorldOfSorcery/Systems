package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.dataclasses.GlobalStat;
import me.hektortm.woSSystems.utils.dataclasses.Stat;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;

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
            case GIVE -> "INSERT INTO playerdata_stats (uuid, id, value) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE value = value + VALUES(value);";
            case TAKE -> "INSERT INTO playerdata_stats (uuid, id, value) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE value = value - VALUES(value);";
            case SET -> "INSERT INTO playerdata_stats (uuid, id, value) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE value = VALUES(value);";
            case RESET -> "INSERT INTO playerdata_stats (uuid, id, value) VALUES (?, ?, 0) " +
                    "ON DUPLICATE KEY UPDATE value = 0;";
        };

        Stat stat = getAllStats().get(statId);

        if (operation == Operations.GIVE && stat.getCapped()) {
            long currentValue = getPlayerStatValue(uuid, statId);
            if (currentValue + amount > stat.getMax()) {
                amount = stat.getMax() - currentValue;
            }
        }

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);

            if (operation == Operations.RESET) {
                stmt.setLong(3, 0); // for the VALUES clause
            } else {
                stmt.setLong(3, amount); // for INSERT
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "15049217", "Failed to modify Stat: "
                    + "\n UUID: "+uuid
                    + "\n ID: "+statId
                    + "\n Amount: "+amount
                    + "\n Operation: "+operation, e
            ));
        }
    }

    /** Modifies a global stat value */
    public void modifyGlobalStatValue(String statId, long amount, Operations operation) {
        String sql = switch (operation) {
            case GIVE -> "UPDATE global_stats SET value = value + ? WHERE id = ?;";
            case TAKE -> "UPDATE global_stats SET value = value - ? WHERE id = ?;";
            case SET -> "UPDATE global_stats SET value = ? WHERE id = ?;";
            case RESET -> "UPDATE global_stats SET value = 0 WHERE id = ?;";
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
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "ee540c82", "Failed to modify global Stat: "
                    + "\n ID: "+statId
                    + "\n Amount: "+amount
                    + "\n Operation: "+operation, e
            ));
        }
    }

    /** Retrieves all player stats */
    public Map<String, Stat> getAllStats() {
        Map<String, Stat> stats = new HashMap<>();
        String sql = "SELECT id, max, capped FROM stats;";
        try ( Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("id"), new Stat(
                        rs.getString("id"),
                        rs.getLong("max"),
                        rs.getBoolean("capped")
                ));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Stats: "+e);
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "bbbbf9f5", "Failed to get all Stats: ", e
            ));
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
                stats.put(rs.getString("id"), new GlobalStat(
                        rs.getString("id"),
                        rs.getLong("max"),
                        rs.getBoolean("capped")
                ));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "3e124896", "Failed to get all Global Stats: ", e
            ));
        }
        return stats;
    }

    /** Retrieves a single player's stat value */
    public long getPlayerStatValue(UUID uuid, String statId) {
        String sql = "SELECT value FROM playerdata_stats WHERE uuid = ? AND id = ?;";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "ca5cb436", "Failed to get Stat Value: "
                    + "\n UUID: "+uuid
                    + "\n ID: "+statId, e
            ));
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
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "e3e23ccd", "Failed to get Global Stat Value: "
                    + "\n ID: "+statId, e
            ));
        }
        return 0;
    }
}
