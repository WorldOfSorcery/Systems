package me.hektortm.woSSystems.database.links;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FriendLink {
    private final Connection conn;
    private final DatabaseManager db;
    private final DAOHub hub;

    public FriendLink(DatabaseManager db, DAOHub hub) {
        this.conn = db.getConnection();
        this.db = db;
        this.hub = hub;
    }


    public void addFriend(UUID uuid, UUID friendUUID) {
        String sql = "INSERT INTO friends (uuid, friend_uuid) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, friendUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a friend
    public void removeFriend(UUID uuid, UUID friendUUID) {
        String sql = "DELETE FROM friends WHERE uuid = ? AND friend_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, friendUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all friends of a player
    public List<UUID> getFriends(UUID uuid) {
        List<UUID> friends = new ArrayList<>();
        String sql = "SELECT friend_uuid FROM friends WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                friends.add(UUID.fromString(rs.getString("friend_uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    // Check if two players are friends
    public boolean isFriend(UUID uuid, UUID friendUUID) {
        String sql = "SELECT 1 FROM friends WHERE uuid = ? AND friend_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, friendUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Add a friend request
    public void addFriendRequest(UUID senderUUID, UUID receiverUUID) {
        String sql = "INSERT INTO friend_requests (sender_uuid, receiver_uuid) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, senderUUID.toString());
            pstmt.setString(2, receiverUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a friend request
    public void removeFriendRequest(UUID senderUUID, UUID receiverUUID) {
        String sql = "DELETE FROM friend_requests WHERE sender_uuid = ? AND receiver_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, senderUUID.toString());
            pstmt.setString(2, receiverUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all friend requests for a player
    public List<UUID> getFriendRequests(UUID uuid) {
        List<UUID> requests = new ArrayList<>();
        String sql = "SELECT sender_uuid FROM friend_requests WHERE receiver_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(UUID.fromString(rs.getString("sender_uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    // Add a favorite friend
    public void addFavorite(UUID uuid, UUID friendUUID) {
        String sql = "UPDATE friends SET favorite = TRUE WHERE uuid = ? AND friend_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, friendUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a favorite friend
    public void removeFavorite(UUID uuid, UUID friendUUID) {
        String sql = "UPDATE friends SET favorite = FALSE WHERE uuid = ? AND friend_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, friendUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all favorite friends of a player
    public List<UUID> getFavorites(UUID uuid) {
        List<UUID> favorites = new ArrayList<>();
        String sql = "SELECT friend_uuid FROM friends WHERE uuid = ? AND favorite = TRUE";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                favorites.add(UUID.fromString(rs.getString("friend_uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favorites;
    }

    // Get player data (last known name and last online)
    public Map<String, Object> getPlayerData(UUID uuid) {
        Map<String, Object> data = new HashMap<>();
        String sql = "SELECT last_known_name, last_online FROM playerdata WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                data.put("last_known_name", rs.getString("last_known_name"));
                data.put("last_online", rs.getTimestamp("last_online"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean hasRequestFrom(UUID sender, UUID receiver) {
        String sql = "SELECT 1 FROM friend_requests WHERE sender_uuid = ? AND receiver_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sender.toString());
            pstmt.setString(2, receiver.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
