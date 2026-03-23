package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.dataclasses.Constant;
import me.hektortm.woSSystems.utils.dataclasses.Cooldown;
import me.hektortm.woSSystems.utils.dataclasses.InteractionKey;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * DAO for cooldown definitions and per-player cooldown tracking.
 *
 * <p>Cooldown definitions ({@link Cooldown}) are preloaded into an in-memory
 * cache at startup.  Two flavours of player cooldowns are supported:</p>
 * <ul>
 *   <li><b>Global cooldowns</b> ({@code playerdata_cooldowns}) — scoped to a
 *       player and a cooldown ID.</li>
 *   <li><b>Local cooldowns</b> ({@code playerdata_local_cooldowns}) — additionally
 *       scoped to an {@link InteractionKey} so the same cooldown ID can be
 *       independent per location or NPC.</li>
 * </ul>
 */
public class CooldownDAO implements IDAO {
    private final DatabaseManager db;

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CooldownDAO";

    private final Map<String, Cooldown> cache = new ConcurrentHashMap<>();

    public CooldownDAO(DatabaseManager db) {
        this.db = db;

    }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Cooldown.class);

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS playerdata_cooldowns(" +
                    "uuid VARCHAR(255)," +
                    "id VARCHAR(255)," +
                    "start_time TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS playerdata_local_cooldowns(" +
                    "uuid VARCHAR(255)," +
                    "id VARCHAR(255)," +
                    "start_time TIMESTAMP," +
                    "interaction_key VARCHAR(255))");
        }

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::preloadAll);
    }

    /**
     * Loads all cooldown definitions from the {@code cooldowns} table into the
     * in-memory cache.  Called asynchronously from {@link #initializeTable()}.
     */
    public void preloadAll() {
        String sql = "SELECT * FROM cooldowns";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                long duration = rs.getLong("duration");
                String startInteraction = rs.getString("start_interaction");
                String endInteraction = rs.getString("end_interaction");
                try {
                    cache.put(id, new Cooldown(id, duration, startInteraction, endInteraction));
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": failed to preload '" + id + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info(logName + ": preloaded " + count + " cooldown(s) into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COOD:preload", "Failed to preload cooldowns into cache: ", e);
        }
    }

    /**
     * Refreshes a single cooldown definition in the cache from the database.
     * If the row no longer exists the entry is evicted.  Sends a title to {@code p}.
     *
     * @param id the cooldown ID to reload
     * @param p  the player who triggered the reload (receives title feedback)
     */
    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT * FROM cooldowns WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long duration = rs.getLong("duration");
                String startInteraction = rs.getString("start_interaction");
                String endInteraction = rs.getString("end_interaction");

                cache.put(id, new Cooldown(id, duration, startInteraction, endInteraction));
                p.sendTitle("§aUpdated Cooldown", "§e"+id );
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                p.sendTitle("§cDeleted Cooldown", "§e"+id );
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COOD:reload", "Failed to reload constant from DB: ", e);
        }
    }


    /**
     * Returns the {@link Cooldown} definition for the given ID from the in-memory
     * cache, or {@code null} if no such cooldown is defined.
     *
     * @param id the cooldown ID
     * @return the cached cooldown definition, or {@code null}
     */
    public Cooldown getCooldown(String id) {
        return cache.get(id);
    }

    /**
     * Starts a global cooldown for the player by inserting a row with the
     * current system time as the start timestamp.
     *
     * @param p  the player to apply the cooldown to
     * @param id the cooldown definition ID
     */
    public void giveCooldown(OfflinePlayer p, String id) {
        String sql = "INSERT INTO playerdata_cooldowns (uuid, id, start_time) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "bb176a6d", "Failed to give cooldown for ID("+id+"): ", e
            ));
        }
    }
    /**
     * Starts a location-scoped cooldown for the player.  The same cooldown ID
     * can be active independently at different locations via the {@link InteractionKey}.
     *
     * @param p   the player to apply the cooldown to
     * @param id  the cooldown definition ID
     * @param key the interaction key scoping this cooldown to a specific location/NPC
     */
    public void giveLocalCooldown(OfflinePlayer p, String id, InteractionKey key) {
        String sql = "INSERT INTO playerdata_local_cooldowns (uuid, id, start_time, interaction_key) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, key.getKey());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "bb176a6d", "Failed to give cooldown for ID("+id+"): ", e
            ));
        }
    }

    /**
     * Returns the duration of the given cooldown in seconds, querying the
     * {@code cooldowns} table directly.  Returns {@code 0} if not found.
     *
     * @param id the cooldown ID
     * @return duration in seconds
     */
    public long getCooldownDuration(String id) {
        String sql = "SELECT duration FROM cooldowns WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("duration"); // Returns duration in seconds
            }
            return 0;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "47688a99", "Failed to fetch cooldown duration for ID("+id+"): ", e
            ));
            return 0;
        }
    }
    /**
     * Returns the start timestamp of a local cooldown, or {@code null} if no
     * active row exists.
     *
     * @param p   the player
     * @param id  the cooldown ID
     * @param key the interaction key scoping the cooldown
     * @return start timestamp, or {@code null}
     */
    public Timestamp getPlayerLocalStartTime(OfflinePlayer p, String id, InteractionKey key) {
        String sql = "SELECT start_time FROM playerdata_local_cooldowns WHERE uuid = ? AND id = ? AND interaction_key = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            stmt.setString(3, key.getKey());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Timestamp(rs.getTimestamp("start_time").getTime());
            }
            return null;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "02ac3572", "Failed to fetch Timestamp for ID("+id+"): ", e
            ));
            return null;
        }
    }


    /**
     * Returns the start timestamp of a global cooldown for the player, or
     * {@code null} if no active row exists.
     *
     * @param p  the player
     * @param id the cooldown ID
     * @return start timestamp, or {@code null}
     */
    public Timestamp getPlayerStartTime(OfflinePlayer p, String id) {
        String sql = "SELECT start_time FROM playerdata_cooldowns WHERE uuid = ? AND id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Timestamp(rs.getTimestamp("start_time").getTime());
            }
            return null;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "02ac3572", "Failed to fetch Timestamp for ID("+id+"): ", e
            ));
            return null;
        }
    }

    /**
     * Returns {@code true} if the player's global cooldown for {@code id} has not
     * yet elapsed.
     *
     * @param p  the player
     * @param id the cooldown ID
     * @return {@code true} if the cooldown is still active
     */
    public boolean isCooldownActive(OfflinePlayer p, String id) {
        Timestamp start = getPlayerStartTime(p, id);
        if (start == null) {
            return false;
        }

        // Convert both to seconds for comparison
        long elapsedSeconds = (System.currentTimeMillis() - start.getTime()) / 1000;
        long cooldownDuration = getCooldownDuration(id);

        return elapsedSeconds < cooldownDuration;
    }

    /**
     * Returns {@code true} if the player's local cooldown for {@code id} at the
     * given {@link InteractionKey} has not yet elapsed.
     *
     * @param p   the player
     * @param id  the cooldown ID
     * @param key the interaction key scoping the cooldown
     * @return {@code true} if the local cooldown is still active
     */
    public boolean isLocalCooldownActive(OfflinePlayer p, String id, InteractionKey key) {
        Timestamp start = getPlayerLocalStartTime(p, id, key);
        if (start == null) {
            return false;
        }

        // Convert both to seconds for comparison
        long elapsedSeconds = (System.currentTimeMillis() - start.getTime()) / 1000;
        long cooldownDuration = getCooldownDuration(id);

        return elapsedSeconds < cooldownDuration;
    }

    /**
     * Returns the number of seconds remaining on the player's global cooldown,
     * or {@code null} if no active cooldown exists.
     *
     * @param p  the player
     * @param id the cooldown ID
     * @return remaining seconds, or {@code null}
     */
    public Long getRemainingSeconds(OfflinePlayer p, String id) {
        if (!isCooldownActive(p, id)) {
            return null; // No active cooldown
        }

        Timestamp start = getPlayerStartTime(p, id);
        long durationSeconds = getCooldownDuration(id);
        long elapsedSeconds = (System.currentTimeMillis() - start.getTime()) / 1000;

        return durationSeconds - elapsedSeconds;
    }

    /**
     * Returns all global cooldown rows from {@code playerdata_cooldowns} grouped
     * by player UUID.
     *
     * @return map of {@code UUID → list of active cooldown IDs}
     * @throws SQLException if the query fails
     */
    public Map<UUID, List<String>> getAllActiveCooldowns() throws SQLException {
        Map<UUID, List<String>> cooldowns = new HashMap<>();
        String sql = "SELECT uuid, id FROM playerdata_cooldowns";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("uuid"));
                String cooldownId = rs.getString("id");
                cooldowns.computeIfAbsent(playerId, k -> new ArrayList<>()).add(cooldownId);
            }
        }
        return cooldowns;
    }
    /**
     * Returns all local cooldown rows from {@code playerdata_local_cooldowns}
     * grouped by player UUID, with each entry mapping cooldown ID to its
     * {@code interaction_key} string.
     *
     * @return map of {@code UUID → (cooldownId → interactionKey)}
     * @throws SQLException if the query fails
     */
    public Map<UUID, Map<String, String>> getAllActiveLocalCooldowns() throws SQLException {
        Map<UUID, Map<String, String>> cooldowns = new HashMap<>();
        String sql = "SELECT uuid, id, interaction_key FROM playerdata_local_cooldowns";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("uuid"));
                String cooldownId = rs.getString("id");
                cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(cooldownId, rs.getString("interaction_key"));
            }
        }
        return cooldowns;
    }

    /**
     * Deletes a specific global cooldown row for a player.
     *
     * @param oP         the player
     * @param cooldownId the cooldown ID to remove
     * @throws SQLException if the delete fails
     */
    public void removeCooldown(OfflinePlayer oP, String cooldownId) throws SQLException {
        String sql = "DELETE FROM playerdata_cooldowns WHERE uuid = ? AND id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, oP.getUniqueId().toString());
            stmt.setString(2, cooldownId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a specific local cooldown row for a player scoped to an interaction key.
     *
     * @param oP         the player
     * @param cooldownId the cooldown ID to remove
     * @param key        the interaction key scoping the cooldown
     * @throws SQLException if the delete fails
     */
    public void removeLocalCooldown(OfflinePlayer oP, String cooldownId, InteractionKey key) throws SQLException {
        String sql = "DELETE FROM playerdata_local_cooldowns WHERE uuid = ? AND id = ? AND interaction_key = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, oP.getUniqueId().toString());
            stmt.setString(2, cooldownId);
            stmt.setString(3, key.getKey());
            stmt.executeUpdate();
        }
    }

    /**
     * Returns {@code true} if the player's global cooldown has elapsed (i.e. the
     * start time plus duration is in the past).  Returns {@code false} if no row
     * exists.
     *
     * @param oP         the player
     * @param cooldownId the cooldown ID to check
     * @return {@code true} if the cooldown has expired
     * @throws SQLException if the query fails
     */
    public boolean isCooldownExpired(OfflinePlayer oP, String cooldownId) throws SQLException {
        String sql = "SELECT start_time FROM playerdata_cooldowns WHERE uuid = ? AND id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, oP.getUniqueId().toString());
            stmt.setString(2, cooldownId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long startTime = rs.getTimestamp("start_time").getTime();
                long duration = getCooldownDuration(cooldownId) * 1000L;
                return System.currentTimeMillis() > (startTime + duration);
            }
            return false;
        }
    }

    /**
     * Returns {@code true} if the player's local cooldown has elapsed.  Returns
     * {@code false} if no matching row exists.
     *
     * @param oP         the player
     * @param cooldownId the cooldown ID to check
     * @param key        the interaction key scoping the cooldown
     * @return {@code true} if the local cooldown has expired
     * @throws SQLException if the query fails
     */
    public boolean isLocalCooldownExpired(OfflinePlayer oP, String cooldownId, InteractionKey key) throws SQLException {
        String sql = "SELECT start_time FROM playerdata_local_cooldowns WHERE uuid = ? AND id = ? AND interaction_key = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, oP.getUniqueId().toString());
            stmt.setString(2, cooldownId);
            stmt.setString(3, key.getKey());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long startTime = rs.getTimestamp("start_time").getTime();
                long duration = getCooldownDuration(cooldownId) * 1000L;
                return System.currentTimeMillis() > (startTime + duration);
            }
            return false;
        }
    }
}
