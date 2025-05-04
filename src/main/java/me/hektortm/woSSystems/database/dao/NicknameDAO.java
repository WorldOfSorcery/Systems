package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NicknameDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public NicknameDAO(DatabaseManager db, DAOHub daoHub) throws SQLException {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nicknames (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "username VARCHAR(16) NOT NULL, " +
                    "nickname VARCHAR(18), " +
                    "previous_nicks TEXT" +
                    ")");

            // Create reserved_nicks table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reserved_nicks (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "nickname VARCHAR(18) NOT NULL" +
                    ")");

            // Create nick_requests table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nick_requests (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "nickname VARCHAR(18) NOT NULL" +
                    ")");
        }
    }

    // Save or update a player's nickname
    public void saveNickname(UUID uuid, String username, String nickname) {
        String query = "INSERT OR IGNORE INTO nicknames (uuid, username, nickname, previous_nicks) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, nickname);
            stmt.setString(4, nickname + ",");
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to insert nickname: " + e.getMessage());
        }

        // Update the nickname if it already exists
        query = "UPDATE nicknames SET username = ?, nickname = ?, previous_nicks = previous_nicks || ? WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, nickname);
            stmt.setString(3, nickname + ",");
            stmt.setString(4, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to update nickname: " + e.getMessage());
        }
    }

    // Reset a player's nickname
    public void resetNickname(UUID uuid) {
        String query = "UPDATE nicknames SET nickname = NULL WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to reset nickname: " + e.getMessage());
        }
    }

    // Get a player's nickname
    public String getNickname(UUID uuid) {
        String query = "SELECT nickname FROM nicknames WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to get nickname: " + e.getMessage());
        }
        return null;
    }

    // Get a player's real username or nickname
    public String getRealNameOrNickname(String input) {
        String query = "SELECT username, nickname FROM nicknames WHERE username = ? OR nickname = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, input);
            stmt.setString(2, input);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String nickname = rs.getString("nickname");
                return nickname != null ? nickname : username;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to get real name or nickname: " + e.getMessage());
        }
        return null;
    }

    // Request a nickname change
    public void requestNicknameChange(UUID uuid, String nickname) {
        String query = "INSERT OR REPLACE INTO nick_requests (uuid, nickname) VALUES (?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nickname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to request nickname change: " + e.getMessage());
        }
    }

    // Approve a nickname change
    public void approveNicknameChange(UUID uuid) {
        String query = "SELECT nickname FROM nick_requests WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nickname = rs.getString("nickname");
                if (nickname.equals("reset")) {
                    resetNickname(uuid);
                } else {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    saveNickname(uuid, player.getName(), nickname);
                }
                removeNicknameRequest(uuid);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to approve nickname change: " + e.getMessage());
        }
    }

    // Deny a nickname change
    public void denyNicknameChange(UUID uuid) {
        removeNicknameRequest(uuid);
    }

    // Remove a nickname request
    private void removeNicknameRequest(UUID uuid) {
        String query = "DELETE FROM nick_requests WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to remove nickname request: " + e.getMessage());
        }
    }

    // Get all nickname requests
    public Map<UUID, String> getNickRequests() {
        Map<UUID, String> requests = new HashMap<>();
        String query = "SELECT uuid, nickname FROM nick_requests";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requests.put(UUID.fromString(rs.getString("uuid")), rs.getString("nickname"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to get nickname requests: " + e.getMessage());
        }
        return requests;
    }

    // Reserve a nickname
    public void reserveNickname(UUID uuid, String nickname) {
        String query = "INSERT OR REPLACE INTO reserved_nicks (uuid, nickname) VALUES (?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nickname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to reserve nickname: " + e.getMessage());
        }
    }

    // Unreserve a nickname
    public void unreserveNickname(UUID uuid) {
        String query = "DELETE FROM reserved_nicks WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to unreserve nickname: " + e.getMessage());
        }
    }

    // Check if a nickname is reserved
    public boolean isNicknameReserved(String nickname) {
        String query = "SELECT uuid FROM reserved_nicks WHERE nickname = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nickname);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to check if nickname is reserved: " + e.getMessage());
        }
        return false;
    }

    // Get all reserved nicknames
    public Map<UUID, String> getReservedNicknames() {
        Map<UUID, String> reservedNicks = new HashMap<>();
        String query = "SELECT uuid, nickname FROM reserved_nicks";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservedNicks.put(UUID.fromString(rs.getString("uuid")), rs.getString("nickname"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to get reserved nicknames: " + e.getMessage());
        }
        return reservedNicks;
    }
}