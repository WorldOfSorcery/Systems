package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.AsyncWriteQueue;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StatsDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "StatsDAO";

    // Server-side definition caches — loaded once, refreshed via reload()
    private volatile Map<String, Stat> statDefinitions = null;
    private volatile Map<String, GlobalStat> globalStatDefinitions = null;

    // Global stat values — loaded at startup, kept in sync with DB
    private final ConcurrentHashMap<String, Long> globalStatsCache = new ConcurrentHashMap<>();

    // Per-player stat values — loaded on join, evicted on quit
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> playerCache = new ConcurrentHashMap<>();

    public StatsDAO(DatabaseManager db, DAOHub daoHub) throws SQLException {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Stat.class);
        SchemaManager.syncTable(db, GlobalStat.class);

        // playerdata_stats is a relational table (uuid × stat_id) — keep manual
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS playerdata_stats (
                    uuid CHAR(36) NOT NULL,
                    id VARCHAR(255) NOT NULL,
                    value BIGINT DEFAULT 0,
                    PRIMARY KEY (uuid, id)
                )
            """);
        }

        // Preload server-wide caches async — these are finite and don't change during play
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            loadStatDefinitions();
            loadGlobalStatDefinitions();
            preloadGlobalStatValues();
        });
    }

    // ─── Player lifecycle ───────────────────────────────────────────────────────

    /**
     * Load all stats for a player from the DB into memory.
     * Call async from PlayerJoinEvent.
     */
    public void loadPlayer(UUID uuid) {
        String sql = "SELECT id, value FROM playerdata_stats WHERE uuid = ?;";
        ConcurrentHashMap<String, Long> stats = new ConcurrentHashMap<>();
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("id"), rs.getLong("value"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "sd:load01",
                    "Failed to load stats for player: " + uuid, e));
        }
        playerCache.put(uuid, stats);
    }

    /**
     * Remove a player's stats from memory.
     * Call from PlayerQuitEvent — no DB write needed, cache was kept in sync.
     */
    public void evictPlayer(UUID uuid) {
        playerCache.remove(uuid);
    }

    // ─── Player stat reads ──────────────────────────────────────────────────────

    /**
     * Returns the player's stat value from cache.
     * Falls back to a DB query for offline players (e.g., admin lookups).
     */
    public long getPlayerStatValue(UUID uuid, String statId) {
        ConcurrentHashMap<String, Long> p = playerCache.get(uuid);
        if (p != null) return p.getOrDefault(statId, 0L);
        return queryPlayerStatFromDb(uuid, statId);
    }

    private long queryPlayerStatFromDb(UUID uuid, String statId) {
        String sql = "SELECT value FROM playerdata_stats WHERE uuid = ? AND id = ?;";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("value");
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "ca5cb436",
                    "Failed to get Stat Value:\n UUID: " + uuid + "\n ID: " + statId, e));
        }
        return 0;
    }

    public boolean isStatLimitReached(UUID uuid, String statId) {
        Stat stat = getAllStats().get(statId);
        if (stat == null || !stat.getCapped()) return false;
        return getPlayerStatValue(uuid, statId) >= stat.getMax();
    }

    // ─── Player stat writes ─────────────────────────────────────────────────────

    /**
     * Modifies a player's stat: updates the in-memory cache immediately,
     * then persists the new absolute value asynchronously.
     */
    public void modifyPlayerStat(UUID uuid, String statId, long amount, Operations operation) {
        Stat stat = getAllStats().get(statId);

        // Atomically compute the new value in the cache
        long[] newValueHolder = {0L};
        playerCache.compute(uuid, (u, playerStats) -> {
            if (playerStats == null) playerStats = new ConcurrentHashMap<>();
            long current = playerStats.getOrDefault(statId, 0L);
            long newVal = switch (operation) {
                case GIVE -> (stat != null && stat.getCapped())
                        ? Math.min(current + amount, stat.getMax())
                        : current + amount;
                case TAKE -> Math.max(0L, current - amount);
                case SET  -> amount;
                case RESET -> 0L;
            };
            newValueHolder[0] = newVal;
            playerStats.put(statId, newVal);
            return playerStats;
        });

        // Persist the final absolute value — always a SET, safe against write reordering
        final long persistValue = newValueHolder[0];
        AsyncWriteQueue.submit(() -> persistPlayerStat(uuid, statId, persistValue));
    }

    private void persistPlayerStat(UUID uuid, String statId, long value) {
        String sql = "INSERT INTO playerdata_stats (uuid, id, value) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE value = VALUES(value);";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, statId);
            stmt.setLong(3, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "15049217",
                    "Failed to persist Stat:\n UUID: " + uuid + "\n ID: " + statId
                    + "\n Value: " + value, e));
        }
    }

    /**
     * Clears all daily_ stats from every loaded player cache and queues a DB delete.
     */
    public void resetDailyStats() {
        // Evict from in-memory caches
        for (ConcurrentHashMap<String, Long> p : playerCache.values()) {
            p.keySet().removeIf(k -> k.startsWith("daily_"));
        }

        // Async DB delete
        AsyncWriteQueue.submit(() -> {
            String sql = "DELETE FROM playerdata_stats WHERE id LIKE 'daily_%'";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "sd:daily",
                        "Failed to reset daily stats", e));
            }
        });
    }

    // ─── Global stats ───────────────────────────────────────────────────────────

    public long getGlobalStatValue(String statId) {
        return globalStatsCache.getOrDefault(statId, 0L);
    }

    public boolean isGlobalStatLimitReached(String statId) {
        GlobalStat stat = getAllGlobalStats().get(statId);
        if (stat == null || !stat.getCapped()) return false;
        return getGlobalStatValue(statId) >= stat.getMax();
    }

    public void modifyGlobalStatValue(String statId, long amount, Operations operation) {
        GlobalStat stat = getAllGlobalStats().get(statId);

        long[] newValueHolder = {0L};
        globalStatsCache.compute(statId, (id, current) -> {
            if (current == null) current = 0L;
            long newVal = switch (operation) {
                case GIVE -> (stat != null && stat.getCapped())
                        ? Math.min(current + amount, stat.getMax())
                        : current + amount;
                case TAKE  -> Math.max(0L, current - amount);
                case SET   -> amount;
                case RESET -> 0L;
            };
            newValueHolder[0] = newVal;
            return newVal;
        });

        final long persistValue = newValueHolder[0];
        AsyncWriteQueue.submit(() -> {
            String sql = "UPDATE global_stats SET value = ? WHERE id = ?;";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, persistValue);
                stmt.setString(2, statId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "ee540c82",
                        "Failed to persist global Stat:\n ID: " + statId + "\n Value: " + persistValue, e));
            }
        });
    }

    // ─── Definition caches (loaded once) ───────────────────────────────────────

    /**
     * Returns the stat definitions cache. Lazy-initialised on first call.
     */
    public Map<String, Stat> getAllStats() {
        if (statDefinitions == null) loadStatDefinitions();
        return statDefinitions;
    }

    /**
     * Returns the global stat definitions cache. Lazy-initialised on first call.
     */
    public Map<String, GlobalStat> getAllGlobalStats() {
        if (globalStatDefinitions == null) loadGlobalStatDefinitions();
        return globalStatDefinitions;
    }

    /** Force-refresh stat definitions (e.g. after an admin adds a new stat). */
    public void reloadStatDefinitions() {
        statDefinitions = null;
        globalStatDefinitions = null;
        loadStatDefinitions();
        loadGlobalStatDefinitions();
    }

    private synchronized void loadStatDefinitions() {
        if (statDefinitions != null) return; // double-checked
        Map<String, Stat> defs = new HashMap<>();
        String sql = "SELECT id, max, capped FROM stats;";
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                defs.put(rs.getString("id"), new Stat(
                        rs.getString("id"), rs.getLong("max"), rs.getBoolean("capped")));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "bbbbf9f5",
                    "Failed to load stat definitions", e));
        }
        statDefinitions = defs;
        plugin.getLogger().info(logName + ": loaded " + defs.size() + " stat definition(s).");
    }

    private synchronized void loadGlobalStatDefinitions() {
        if (globalStatDefinitions != null) return;
        Map<String, GlobalStat> defs = new HashMap<>();
        String sql = "SELECT id, max, capped FROM global_stats;";
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                defs.put(rs.getString("id"), new GlobalStat(
                        rs.getString("id"), rs.getLong("max"), rs.getBoolean("capped")));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "3e124896",
                    "Failed to load global stat definitions", e));
        }
        globalStatDefinitions = defs;
    }

    private void preloadGlobalStatValues() {
        String sql = "SELECT id, value FROM global_stats;";
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                globalStatsCache.put(rs.getString("id"), rs.getLong("value"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "sd:gload",
                    "Failed to preload global stat values", e));
        }
        plugin.getLogger().info(logName + ": preloaded " + globalStatsCache.size() + " global stat value(s).");
    }
}
