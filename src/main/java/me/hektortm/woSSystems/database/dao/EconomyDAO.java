package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.AsyncWriteQueue;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
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

/**
 * DAO for currency definitions and per-player balance management.
 *
 * <p>Currency definitions are loaded once at startup and cached in memory.
 * Per-player balances are loaded on join, served entirely from the in-memory
 * cache during play, and persisted asynchronously via {@link AsyncWriteQueue}
 * to avoid blocking the main thread.</p>
 *
 * <p>Tables managed: {@code currencies} (definitions, via {@link SchemaManager}),
 * {@code playerdata_economy} (per-player balances), {@code eco_log} (audit log).</p>
 */
public class EconomyDAO implements IDAO {
    private final DatabaseManager db;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "EconomyDAO";

    // Currency definitions — loaded once, refreshable via reloadCurrencies()
    private Map<String, Currency> currencyCache = null;

    // Per-player balance cache — loaded on join, evicted on quit
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> balanceCache = new ConcurrentHashMap<>();

    public EconomyDAO(DatabaseManager db) { this.db = db; }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Currency.class);

        // Relational and log tables — keep manual
        try (Connection conn = db.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata_economy(" +
                    "uuid CHAR(36), " +
                    "currency VARCHAR(200), " +
                    "amount BIGINT NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (uuid, currency), " +
                    "FOREIGN KEY (uuid) REFERENCES playerdata(uuid))");
            statement.execute("CREATE TABLE IF NOT EXISTS eco_log (" +
                    "uuid CHAR(36) NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "currency VARCHAR(255)," +
                    "previous_amount BIGINT," +
                    "new_amount BIGINT," +
                    "change_amount BIGINT," +
                    "source_type VARCHAR(255)," +
                    "source VARCHAR(255))");
        }

        // Preload currency definitions async
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::loadCurrencies);
    }

    // ─── Player lifecycle ───────────────────────────────────────────────────────

    /**
     * Load all currency balances for a player from the DB into memory.
     * Call async from PlayerJoinEvent.
     */
    public void loadPlayer(UUID uuid) {
        String sql = "SELECT currency, amount FROM playerdata_economy WHERE uuid = ?;";
        ConcurrentHashMap<String, Long> balances = new ConcurrentHashMap<>();
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                balances.put(rs.getString("currency"), rs.getLong("amount"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "eco:load01",
                    "Failed to load economy data for player: " + uuid, e));
        }
        balanceCache.put(uuid, balances);
    }

    /**
     * Remove a player's balances from memory.
     * Call from PlayerQuitEvent.
     */
    public void evictPlayer(UUID uuid) {
        balanceCache.remove(uuid);
    }

    // ─── Balance reads ──────────────────────────────────────────────────────────

    /**
     * Returns the player's balance for the given currency from cache.
     * Falls back to a DB query for offline players.
     */
    public long getPlayerCurrency(UUID uuid, String currency) {
        ConcurrentHashMap<String, Long> p = balanceCache.get(uuid);
        if (p != null) return p.getOrDefault(currency, 0L);
        return queryBalanceFromDb(uuid, currency);
    }

    private long queryBalanceFromDb(UUID uuid, String currency) {
        String sql = "SELECT amount FROM playerdata_economy WHERE uuid = ? AND currency = ?;";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, currency);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("amount");
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "27afb2aa",
                    "Failed to get Player Currency(" + currency + "): ", e));
        }
        return 0;
    }

    // ─── Balance writes ─────────────────────────────────────────────────────────

    /**
     * Updates the player's balance in the cache immediately and persists async.
     * Replaces the old ensurePlayerEconomyEntry + updatePlayerCurrency pattern.
     */
    public void updatePlayerCurrency(UUID uuid, String currency, long amount) {
        balanceCache.compute(uuid, (u, balances) -> {
            if (balances == null) balances = new ConcurrentHashMap<>();
            balances.put(currency, amount);
            return balances;
        });

        AsyncWriteQueue.submit(() -> {
            String sql = "INSERT INTO playerdata_economy (uuid, currency, amount) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE amount = VALUES(amount);";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, currency);
                stmt.setLong(3, amount);
                stmt.executeUpdate();
            } catch (SQLException e) {
                DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "59dfee1c",
                        "Failed to persist Player Currency(" + currency + "): ", e));
            }
        });
    }

    // ─── Eco log ────────────────────────────────────────────────────────────────

    /**
     * Appends an economy audit log entry asynchronously via {@link AsyncWriteQueue}.
     *
     * @param uuid           the player's UUID
     * @param currency       the affected currency ID
     * @param previousAmount the balance before the change
     * @param newAmount      the balance after the change
     * @param changeAmount   the delta (positive or negative)
     * @param sourceType     category of the action (e.g. {@code "INTERACTION"})
     * @param source         specific source identifier (e.g. interaction ID)
     */
    public void ecoLog(UUID uuid, String currency, long previousAmount, long newAmount,
                       long changeAmount, String sourceType, String source) {
        AsyncWriteQueue.submit(() -> {
            String sql = "INSERT INTO eco_log (uuid, currency, previous_amount, new_amount, " +
                         "change_amount, source_type, source) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, currency);
                stmt.setLong(3, previousAmount);
                stmt.setLong(4, newAmount);
                stmt.setLong(5, changeAmount);
                stmt.setString(6, sourceType);
                stmt.setString(7, source);
                stmt.executeUpdate();
            } catch (SQLException e) {
                DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "9e047279",
                        "Failed to write eco log: ", e));
            }
        });
    }

    // ─── Currency definitions ───────────────────────────────────────────────────

    /**
     * Returns all currency definitions. Lazy-initialised on first call.
     */
    public synchronized Map<String, Currency> getCurrencies() {
        if (currencyCache == null) loadCurrencies();
        return currencyCache;
    }

    /**
     * Checks whether a currency definition exists (via the cached definitions map).
     *
     * @param id the currency ID
     * @return {@code true} if the currency is defined
     */
    public boolean currencyExists(String id) {
        return getCurrencies().containsKey(id);
    }

    /**
     * Force-refreshes the currency definitions cache from the database.
     * Use after an admin adds or modifies a currency definition.
     */
    public void reloadCurrencies() {
        currencyCache = null;
        loadCurrencies();
    }

    private synchronized void loadCurrencies() {
        if (currencyCache != null) return; // double-checked
        Map<String, Currency> map = new HashMap<>();
        String sql = "SELECT * FROM currencies;";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Currency c = new Currency(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("short_name"),
                        rs.getString("icon"),
                        rs.getString("color"),
                        rs.getBoolean("hidden_if_zero"));
                map.put(c.getId(), c);
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "10a473af",
                    "Failed to load currencies: ", e));
        }
        currencyCache = map;
        plugin.getLogger().info(logName + ": loaded " + map.size() + " currency definition(s).");
    }
}
