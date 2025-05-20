package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class ProfileDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "ProfileDAO";

    public ProfileDAO(DatabaseManager db, DAOHub hub) throws SQLException {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        // Create profile table
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS playerdata_profile (
                    uuid VARCHAR(36) NOT NULL,
                    background_id VARCHAR(36),
                    background VARCHAR(10),
                    picture_id VARCHAR(36),
                    picture VARCHAR(10),
                    PRIMARY KEY (uuid)
                )
            """);
        }
    }

    public String getBackground(UUID uuid) {
        String sql = "SELECT background FROM playerdata_profile WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("background");
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to retrieve background: " + e);
        }
        return null;
    }

    public String getBackgroundID(UUID uuid) {
        String sql = "SELECT background_id FROM playerdata_profile WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("background_id");
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to retrieve background ID: " + e);
        }
        return null;
    }

    public String getProfilePicture(UUID uuid) {
        String sql = "SELECT picture FROM playerdata_profile WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("picture");
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to retrieve profile picture: " + e);
        }
        return null;
    }

    public String getProfilePictureID(UUID uuid) {
        String sql = "SELECT picture_id FROM playerdata_profile WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("picture_id");
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to retrieve profile picture ID: " +e);
        }
        return null;
    }

    public void updateBackground(UUID uuid, String background) {
        String sql = "INSERT OR REPLACE INTO playerdata_profile (uuid, background, background_id) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, background);
            pstmt.setString(3, "e");

            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to update background: "+e);
        }
    }

    public void updateProfilePicture(UUID uuid, String pictureItem, String id) {
        String sql = "INSERT OR REPLACE INTO playerdata_profile (uuid, picture, picture_id) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, pictureItem);
            pstmt.setString(3, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to update profile picture: "+e);
        }
    }

    public void insertOrUpdateProfile(UUID uuid, String background, String backgroundId, String picture, String pictureId) {
        String sql = """
            INSERT INTO playerdata_profile (uuid, background, background_id, picture, picture_id)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                background = VALUES(background),
                background_id = VALUES(background_id),
                picture = VALUES(picture),
                picture_id = VALUES(picture_id)
        """;
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, background);
            pstmt.setString(3, backgroundId);
            pstmt.setString(4, picture);
            pstmt.setString(5, pictureId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to insert or update profile: "+e);
        }
    }
}