package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChannelDAO {
    private final Connection conn;

    public ChannelDAO(DatabaseManager db) {
        this.conn = db.getConnection();
        createTables();
    }

    private void createTables() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS channels (
                    name TEXT PRIMARY KEY NOT NULL,
                    short_name TEXT NOT NULL,
                    format TEXT NOT NULL,
                    autojoin BOOLEAN NOT NULL,
                    forcejoin BOOLEAN NOT NULL,
                    hidden BOOLEAN NOT NULL,
                    radius INTEGER NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS playerdata_channels (
                    uuid TEXT NOT NULL,
                    channel_name TEXT NOT NULL,
                    joined BOOLEAN NOT NULL,
                    focused BOOLEAN NOT NULL,
                    PRIMARY KEY (uuid, channel_name)
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertChannel(Channel channel) {
        String sql = "INSERT INTO channels(name, short_name, format, autojoin, forcejoin, hidden, radius) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channel.getName());
            pstmt.setString(2, channel.getShortName());
            pstmt.setString(3, channel.getFormat());
            pstmt.setBoolean(4, channel.isAutoJoin());
            pstmt.setBoolean(5, channel.isForceJoin());
            pstmt.setBoolean(6, channel.isHidden());
            pstmt.setInt(7, channel.getRadius());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateChannel(Channel channel) {
        String sql = "UPDATE channels SET short_name = ?, format = ?, autojoin = ?, forcejoin = ?, hidden = ?, radius = ? WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channel.getShortName());
            pstmt.setString(2, channel.getFormat());
            pstmt.setBoolean(3, channel.isAutoJoin());
            pstmt.setBoolean(4, channel.isForceJoin());
            pstmt.setBoolean(5, channel.isHidden());
            pstmt.setInt(6, channel.getRadius());
            pstmt.setString(7, channel.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteChannel(String name) {
        String sql = "DELETE FROM channels WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<>();
        String sql = "SELECT * FROM channels";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Channel channel = new Channel(
                        rs.getString("name"),
                        rs.getString("short_name"),
                        rs.getString("format"),
                        new ArrayList<>(),
                        rs.getBoolean("autojoin"),
                        rs.getBoolean("forcejoin"),
                        rs.getBoolean("hidden"),
                        rs.getInt("radius")
                );
                channels.add(channel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return channels;
    }

    public void addRecipient(String channelName, UUID playerUUID) {
        String sql = "INSERT INTO playerdata_channels(uuid, channel_name, joined, focused) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeRecipient(String channelName, UUID playerUUID) {
        String sql = "DELETE FROM playerdata_channels WHERE uuid = ? AND channel_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setFocusedChannel(UUID playerUUID, String channelName) {
        String sql = "UPDATE playerdata_channels SET focused = ? WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, true);
            pstmt.setString(2, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getFocusedChannel(UUID playerUUID) {
        String sql = "SELECT channel_name FROM playerdata_channels WHERE uuid = ? AND focused = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setBoolean(2, true);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("channel_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}