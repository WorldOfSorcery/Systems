package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Cooldown;
import me.hektortm.woSSystems.utils.dataclasses.InteractionAction;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                    "start_interaction VARCHAR(255)" +
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
                return new Cooldown(id, rs.getInt("duration"), rs.getString("start_interaction"), rs.getString("end_interaction"));
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
        try (Connection conn = db.getConnection(); PreparedStatement stmt =  conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("duration");
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
        // Get the start time of the cooldown
        Timestamp start = getPlayerStartTime(p, id);

        // If no start time exists, there's no active cooldown
        if (start == null) {
            return false;
        }

        // Get current time
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Calculate the duration that has passed since the cooldown started
        long elapsedMillis = now.getTime() - start.getTime();

        // Get the cooldown duration for this specific ID (you'll need to implement getCooldownDuration)
        long cooldownDuration = getCooldownDuration(id);

        // If elapsed time is less than cooldown duration, cooldown is still active
        return elapsedMillis < cooldownDuration;
    }

}
