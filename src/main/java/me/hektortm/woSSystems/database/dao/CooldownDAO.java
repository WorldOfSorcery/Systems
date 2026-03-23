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

public class CooldownDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CooldownDAO";

    private final Map<String, Cooldown> cache = new ConcurrentHashMap<>();

    public CooldownDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
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
                plugin.getLogger().info(logName + ": reloaded '" + id + "' from DB.");
                p.sendMessage(plugin.getLangManager().getMessage("general", "prefix") + "§aUpdated Cooldown: §e"+id);
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                plugin.getLogger().info(logName + ": evicted '" + id + "' (not found in DB).");
                p.sendMessage(plugin.getLangManager().getMessage("general", "prefix") + "§cDeleted Constant: §e"+id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COOD:reload", "Failed to reload constant from DB: ", e);
        }
    }


    public Cooldown getCooldown(String id) {
        return cache.get(id);
    }

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

    public Long getRemainingSeconds(OfflinePlayer p, String id) {
        if (!isCooldownActive(p, id)) {
            return null; // No active cooldown
        }

        Timestamp start = getPlayerStartTime(p, id);
        long durationSeconds = getCooldownDuration(id);
        long elapsedSeconds = (System.currentTimeMillis() - start.getTime()) / 1000;

        return durationSeconds - elapsedSeconds;
    }

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

    // Remove specific cooldown for a player
    public void removeCooldown(OfflinePlayer oP, String cooldownId) throws SQLException {
        String sql = "DELETE FROM playerdata_cooldowns WHERE uuid = ? AND id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, oP.getUniqueId().toString());
            stmt.setString(2, cooldownId);
            stmt.executeUpdate();
        }
    }

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

    // Check if cooldown is expired
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
