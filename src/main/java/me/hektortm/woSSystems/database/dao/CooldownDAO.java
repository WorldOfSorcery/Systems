package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Cooldown;
import me.hektortm.woSSystems.utils.dataclasses.InteractionAction;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CooldownDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CooldownDAO";

    public CooldownDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS cooldowns(" +
                    "id VARCHAR(255)," +
                    "duration BIGINT," +
                    "start_interaction VARCHAR(255)," +
                    "end_interaction VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS playerdata_cooldowns(" +
                    "uuid VARCHAR(255)," +
                    "id VARCHAR(255)," +
                    "start_time TIMESTAMP" +
                    ")");
        }
    }

    public Cooldown getCooldownByID(String id) {
        String sql = "SELECT * FROM cooldowns WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Duration is now in seconds
                return new Cooldown(id, rs.getLong("duration"), rs.getString("start_interaction"), rs.getString("end_interaction"));
            }
            return null;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to fetch all Cooldowns");
            return null;
        }
    }

    public void giveCooldown(OfflinePlayer p, String id) {
        String sql = "INSERT INTO playerdata_cooldowns (uuid, id, start_time) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to give cooldown");
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to fetch cooldown duration");
            return 0;
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to fetch Timestamp");
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

    public Map<String, Cooldown> getAllCooldowns() {
        Map<String, Cooldown> cooldowns = new HashMap<>();
        String sql = "SELECT * FROM cooldowns";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                long duration = rs.getLong("duration");
                String startInteraction = rs.getString("start_interaction");
                String endInteraction = rs.getString("end_interaction");

                cooldowns.put(id, new Cooldown(id, duration, startInteraction, endInteraction));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to fetch all Cooldowns: " + e);
        }
        return cooldowns;
    }

    public String formatRemainingTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d min %d sec", minutes, remainingSeconds);
    }

}
