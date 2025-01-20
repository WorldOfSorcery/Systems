package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.database.DatabaseManager;
import me.hektortm.woSSystems.systems.stats.utils.Operation;
import me.hektortm.woSSystems.utils.dataclasses.GlobalStat;
import me.hektortm.woSSystems.utils.dataclasses.Stat;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsDAO {
    private Connection connection;

    public StatsDAO(DatabaseManager database) {
        this.connection = database.getConnection();
        createTables();
    }

    /** Creates necessary tables if they do not exist */
    private void createTables() {
        String createStatsTable = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid TEXT NOT NULL,
                stat_id TEXT NOT NULL,
                value BIGINT DEFAULT 0,
                PRIMARY KEY (uuid, stat_id)
            );
        """;

        String createGlobalStatsTable = """
            CREATE TABLE IF NOT EXISTS global_stats (
                stat_id TEXT PRIMARY KEY,
                value BIGINT DEFAULT 0,
                max BIGINT DEFAULT 0,
                capped BOOLEAN DEFAULT FALSE
            );
        """;

        String createStatDefinitionsTable = """
            CREATE TABLE IF NOT EXISTS stats (
                stat_id TEXT PRIMARY KEY,
                max BIGINT DEFAULT 0,
                capped BOOLEAN DEFAULT FALSE
            );
        """;

        try (
             Statement stmt = connection.createStatement()) {
            stmt.execute(createStatsTable);
            stmt.execute(createGlobalStatsTable);
            stmt.execute(createStatDefinitionsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Creates a new player stat definition */
    public void createStat(Stat stat) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO stats (stat_id, max, capped) VALUES (?, ?, ?) ON CONFLICT(stat_id) DO NOTHING;")) {
            stmt.setString(1, stat.getId());
            stmt.setLong(2, stat.getMax());
            stmt.setBoolean(3, stat.getCapped());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Creates a new global stat definition */
    public void createGlobalStat(GlobalStat globalStat) {
        String sql = "INSERT INTO global_stats (stat_id, value, max, capped) VALUES (?, 0, ?, ?) ON CONFLICT(stat_id) DO NOTHING;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, globalStat.getId());
            stmt.setLong(2, globalStat.getMax());
            stmt.setBoolean(3, globalStat.getCapped());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Deletes a player stat definition */
    public void deleteStat(String statId) {
        String sql = "DELETE FROM stats WHERE stat_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Deletes a global stat */
    public void deleteGlobalStat(String statId) {
        String sql = "DELETE FROM global_stats WHERE stat_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Modifies a player's stat value */
    public void modifyPlayerStat(UUID uuid, String statId, long amount, Operation operation) {
        String sql = switch (operation) {
            case GIVE -> "INSERT INTO player_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, stat_id) DO UPDATE SET value = value + ?;";
            case TAKE -> "INSERT INTO player_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, stat_id) DO UPDATE SET value = value - ?;";
            case SET -> "INSERT INTO player_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, stat_id) DO UPDATE SET value = ?;";
            case RESET -> "INSERT INTO player_stats (uuid, stat_id, value) VALUES (?, ?, ?) ON CONFLICT(uuid, stat_id) DO UPDATE SET value = 0;";
        };

        Stat stat = getAllStats().get(statId);
        if (operation == Operation.GIVE) {
            if (stat.getCapped()) {
                long currentValue = getPlayerStatValue(uuid, statId);
                if (currentValue + amount > stat.getMax()) {
                    amount = stat.getMax() - currentValue;
                }
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);
            stmt.setLong(3, amount);
            stmt.setLong(4, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Modifies a global stat value */
    public void modifyGlobalStatValue(String statId, long amount, Operation operation) {
        String sql = switch (operation) {
            case GIVE -> "UPDATE global_stats SET value = value + ? WHERE stat_id = ?;";
            case TAKE -> "UPDATE global_stats SET value = value - ? WHERE stat_id = ?;";
            case SET -> "UPDATE global_stats SET value = ? WHERE stat_id = ?;";
            case RESET -> "UPDATE global_stats SET value = 0 WHERE stat_id = ?;";
        };
        GlobalStat stat = getAllGlobalStats().get(statId);
        if (operation == Operation.GIVE) {
            if (stat.getCapped()) {
                long currentAmount = getGlobalStatValue(statId);
                if (currentAmount + amount > stat.getMax()) {
                    amount = stat.getMax() - currentAmount;
                }
            }
        }


        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, amount);
            stmt.setString(2, statId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Retrieves all player stats */
    public Map<String, Stat> getAllStats() {
        Map<String, Stat> stats = new HashMap<>();
        String sql = "SELECT stat_id, max, capped FROM stats;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("stat_id"), new Stat(
                        rs.getString("stat_id"),
                        rs.getLong("max"),
                        rs.getBoolean("capped")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /** Retrieves all global stats */
    public Map<String, GlobalStat> getAllGlobalStats() {
        Map<String, GlobalStat> stats = new HashMap<>();
        String sql = "SELECT * FROM global_stats;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("stat_id"), new GlobalStat(
                        rs.getString("stat_id"),
                        rs.getLong("max"),
                        rs.getBoolean("capped")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /** Retrieves a single player's stat value */
    public long getPlayerStatValue(UUID uuid, String statId) {
        String sql = "SELECT value FROM player_stats WHERE uuid = ? AND stat_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Retrieves a global stat value */
    public long getGlobalStatValue(String statId) {
        String sql = "SELECT value FROM global_stats WHERE stat_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
