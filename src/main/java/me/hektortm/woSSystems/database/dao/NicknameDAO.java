package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * DAO for managing player nicknames, nickname reservations, and the
 * staff-approval workflow for nickname change requests.
 *
 * <p>Tables managed: {@code nicknames}, {@code reserved_nicks}, {@code nick_requests}.</p>
 */
public class NicknameDAO implements IDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final String logName = "NicknameDAO";

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

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reserved_nicks (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "nickname VARCHAR(18) NOT NULL" +
                    ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nick_requests (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "nickname VARCHAR(18) NOT NULL" +
                    ")");
        }
    }

    /**
     * Saves or updates a player's nickname and appends it to their nick history.
     * Uses INSERT IGNORE to create a new row, followed by UPDATE to keep the
     * username current and append the new nick to {@code previous_nicks}.
     *
     * @param uuid     the player's UUID
     * @param username the player's current Minecraft username
     * @param nickname the new nickname to apply
     */
    public void saveNickname(UUID uuid, String username, String nickname) {
        // Insert if not exists
        String query = "INSERT IGNORE INTO nicknames (uuid, username, nickname, previous_nicks) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, nickname);
            stmt.setString(4, nickname + ",");
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "7e93a1b8", "Failed to save Nickname: \n User: "+username+" \n Nick: "+nickname, e
            ));
        }

        // Always update
        query = "UPDATE nicknames SET username = ?, nickname = ?, previous_nicks = CONCAT(previous_nicks, ?) WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, nickname);
            stmt.setString(3, nickname + ",");
            stmt.setString(4, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "234c2ba5", "Failed to update Nickname: \n User: "+username+" \n Nick: "+nickname, e
            ));
        }
    }

    /**
     * Clears the active nickname for a player, setting the {@code nickname}
     * column to {@code NULL}.  The username and history are preserved.
     *
     * @param uuid the player's UUID
     */
    public void resetNickname(UUID uuid) {
        String query = "UPDATE nicknames SET nickname = NULL WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "cd77afd2", "Failed to reset Nickname: \n UUID: "+uuid, e
            ));
        }
    }

    /**
     * Returns the current nickname for the given UUID, or {@code null} if the
     * player has no active nickname or is not in the table.
     *
     * @param uuid the player's UUID
     * @return the nickname string, or {@code null}
     */
    public String getNickname(UUID uuid) {
        String query = "SELECT nickname FROM nicknames WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "4a789df0", "Failed to get Nickname: \n UUID: "+uuid, e
            ));
        }
        return null;
    }

    /**
     * Looks up {@code input} against both the {@code username} and {@code nickname}
     * columns and returns the preferred display name (nickname if set, otherwise
     * username).  Returns {@code null} if no match is found.
     *
     * @param input a username or nickname to search for
     * @return the resolved display name, or {@code null}
     */
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
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "5f9e4aea", "Failed to get real- or nickname: \n Query: "+input, e
            ));
        }
        return null;
    }

    /**
     * Submits a nickname change request for staff review.  Uses upsert semantics
     * so re-submitting with a different nickname replaces the previous request.
     *
     * @param uuid     the requesting player's UUID
     * @param nickname the desired new nickname (or {@code "reset"} to clear)
     */
    public void requestNicknameChange(UUID uuid, String nickname) {
        String query = "INSERT INTO nick_requests (uuid, nickname) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE nickname = VALUES(nickname)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nickname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "a70497da", "Failed to request nickname change: \n UUID: "+uuid+" \n Nickname: "+nickname, e
            ));
        }
    }

    /**
     * Approves a pending nickname change request: applies the nickname (or resets
     * it if the requested value is {@code "reset"}), then removes the request row.
     *
     * @param uuid the player whose request should be approved
     */
    public void approveNicknameChange(UUID uuid) {
        String query = "SELECT nickname FROM nick_requests WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nickname = rs.getString("nickname");
                if ("reset".equalsIgnoreCase(nickname)) {
                    resetNickname(uuid);
                } else {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    saveNickname(uuid, player.getName(), nickname);
                }
                removeNicknameRequest(uuid);
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "7aec601a", "Failed to approve nickname: \n UUID: "+uuid, e
            ));
        }
    }

    /**
     * Rejects a pending nickname change request by deleting the request row.
     * The player's current nickname remains unchanged.
     *
     * @param uuid the player whose request should be denied
     */
    public void denyNicknameChange(UUID uuid) {
        removeNicknameRequest(uuid);
    }

    private void removeNicknameRequest(UUID uuid) {
        String query = "DELETE FROM nick_requests WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "0936e8f4", "Failed to remove nickname request: \n UUID: "+uuid, e
            ));
        }
    }

    /**
     * Returns all pending nickname change requests as a {@code UUID → nickname} map.
     *
     * @return map of player UUID to requested nickname
     */
    public Map<UUID, String> getNickRequests() {
        Map<UUID, String> requests = new HashMap<>();
        String query = "SELECT uuid, nickname FROM nick_requests";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requests.put(UUID.fromString(rs.getString("uuid")), rs.getString("nickname"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "748598ef", "Failed to get nickname requests:", e
            ));
        }
        return requests;
    }

    /**
     * Reserves a nickname for a specific player so no one else can claim it.
     * Uses upsert semantics; re-calling replaces the existing reservation.
     *
     * @param uuid     the player the reservation is for
     * @param nickname the nickname to reserve
     */
    public void reserveNickname(UUID uuid, String nickname) {
        String query = "INSERT INTO reserved_nicks (uuid, nickname) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE nickname = VALUES(nickname)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nickname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "94ffe050", "Failed to get reserve nickname: \n UUID: "+uuid+" \n Nickname: "+nickname, e
            ));
        }
    }

    /**
     * Removes the nickname reservation for the given player.
     *
     * @param uuid the player whose reservation should be released
     */
    public void unreserveNickname(UUID uuid) {
        String query = "DELETE FROM reserved_nicks WHERE uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "56847cd8", "Failed to unreserve nickname: \n UUID: "+uuid, e
            ));
        }
    }

    /**
     * Checks whether the given nickname string is reserved by any player.
     *
     * @param nickname the nickname to check
     * @return {@code true} if the nickname is reserved
     */
    public boolean isNicknameReserved(String nickname) {
        String query = "SELECT uuid FROM reserved_nicks WHERE nickname = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nickname);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "32cf0db6", "Failed to check if nickname is reserved: \n Nickname: "+nickname, e
            ));
        }
        return false;
    }

    /**
     * Returns all reserved nicknames as a {@code UUID → nickname} map.
     *
     * @return map of player UUID to their reserved nickname
     */
    public Map<UUID, String> getReservedNicknames() {
        Map<UUID, String> reservedNicks = new HashMap<>();
        String query = "SELECT uuid, nickname FROM reserved_nicks";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservedNicks.put(UUID.fromString(rs.getString("uuid")), rs.getString("nickname"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "32cf0db6", "Failed to get reserved nicknames: ", e
            ));
        }
        return reservedNicks;
    }
}
