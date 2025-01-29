package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.database.DatabaseManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChannelDAO {
    private final Connection conn;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

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
                    color TEXT NOT NULL,
                    format TEXT NOT NULL,
                    default_channel BOOLEAN NOT NULL,
                    autojoin BOOLEAN NOT NULL,
                    forcejoin BOOLEAN NOT NULL,
                    hidden BOOLEAN NOT NULL,
                    broadcastable BOOLEAN NOT NULL,
                    permission TEXT,
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
        String sql = "INSERT INTO channels(name, short_name, color, format, default_channel, autojoin, forcejoin, hidden, broadcastable, permission,  radius) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channel.getName());
            pstmt.setString(2, channel.getShortName());
            pstmt.setString(3, channel.getColor());
            pstmt.setString(4, channel.getFormat());
            pstmt.setBoolean(5, channel.isDefaultChannel());
            pstmt.setBoolean(6, channel.isAutoJoin());
            pstmt.setBoolean(7, channel.isForceJoin());
            pstmt.setBoolean(8, channel.isHidden());
            pstmt.setBoolean(9, channel.isBroadcastable());
            pstmt.setString(10, channel.getPermission());
            pstmt.setInt(11, channel.getRadius());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateChannel(Channel channel) {
        String sql = "UPDATE channels SET short_name = ?, color = ?, format = ?, default_channel = ?, autojoin = ?, forcejoin = ?, hidden = ?, permission = ?, broadcastable = ?, radius = ? WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channel.getShortName());
            pstmt.setString(2, channel.getColor());
            pstmt.setString(3, channel.getFormat());
            pstmt.setBoolean(4, channel.isDefaultChannel());
            pstmt.setBoolean(5, channel.isAutoJoin());
            pstmt.setBoolean(6, channel.isForceJoin());
            pstmt.setBoolean(7, channel.isHidden());
            pstmt.setString(8, channel.getPermission());
            pstmt.setBoolean(9, channel.isBroadcastable());
            pstmt.setInt(10, channel.getRadius());
            pstmt.setString(11, channel.getName());
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
                        rs.getString("color"),
                        rs.getString("format"),
                        new ArrayList<>(),
                        rs.getBoolean("default_channel"),
                        rs.getBoolean("autojoin"),
                        rs.getBoolean("forcejoin"),
                        rs.getBoolean("hidden"),
                        rs.getString("permission"),
                        rs.getBoolean("broadcastable"),
                        rs.getInt("radius")
                );
                channels.add(channel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return channels;
    }

    public List<UUID> getRecipients(String channelName) {
        List<UUID> recipients = new ArrayList<>();
        String sql = "SELECT uuid FROM playerdata_channels WHERE channel_name = ? AND joined = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channelName);
            pstmt.setBoolean(2, true);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recipients.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipients;
    }


    public void addRecipient(String channelName, UUID playerUUID) {
        Player p = Bukkit.getPlayer(playerUUID);
        if (isInChannel(playerUUID, channelName)) {
            p.sendMessage("You are already in this Channel.");
            Utils.error(p, "channel", "error.arleady-in-channel");
            return;
        }


        String sql = "INSERT INTO playerdata_channels(uuid, channel_name, joined, focused) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();
            Utils.successMsg1Value(p, "channel", "joined", "%channel%", getChannelColor(channelName) + channelName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getChannelColor(String channelName) {
        String sql = "SELECT color FROM channels WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channelName);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("color");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeRecipient(String channelName, UUID playerUUID) {
        Player p = Bukkit.getPlayer(playerUUID);
        if (!isInChannel(playerUUID, channelName)) {
            Utils.error(p, "channel", "error.not-in-channel");
            return;
        }
        String sql = "DELETE FROM playerdata_channels WHERE uuid = ? AND channel_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.executeUpdate();
            Utils.successMsg1Value(p, "channel", "left", "%channel%", getChannelColor(channelName) + channelName);
            if (isFocusedChannel(playerUUID, channelName)) {
                for (Channel channel : getAllChannels()) {
                    if (channel.isDefaultChannel()) {
                        setFocusedChannel(playerUUID, channel.getName());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getChannelPermission(String channelName) {
        String sql = "SELECT permission FROM channels WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channelName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("permission") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setFocusedChannel(UUID playerUUID, String channelName) {
        Player p = Bukkit.getPlayer(playerUUID);
        if(!isInChannel(playerUUID, channelName)) {

            String permission = getChannelPermission(channelName);
            if (permission != null && !p.hasPermission(permission)) {
                p.sendMessage("you cannot join this channel.");
                return;
            } else if (permission == null || p.hasPermission(permission)) {
                addRecipient(channelName, playerUUID); // if not in channel, add player to channel

            }
        }
        unfocusChannel(playerUUID, getFocusedChannel(playerUUID));
        String sql = "UPDATE playerdata_channels SET focused = ? WHERE uuid = ? AND channel_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, true);
            pstmt.setString(2, playerUUID.toString());
            pstmt.setString(3, channelName);
            pstmt.executeUpdate();
            p.sendMessage("§7You are now §afocused§7 on channel §e" + channelName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unfocusChannel(UUID playerUUID, String channelName) {
        Player p = Bukkit.getPlayer(playerUUID);
        String sql = "UPDATE playerdata_channels SET focused = ? WHERE uuid = ? AND channel_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, false);
            pstmt.setString(2, playerUUID.toString());
            pstmt.setString(3, channelName);
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

    public boolean isInChannel(UUID playerUUID, String channelName) {
        String sql = "SELECT joined FROM playerdata_channels WHERE uuid = ? AND channel_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            ResultSet rs = pstmt.executeQuery();
            return rs.getBoolean("joined");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFocusedChannel(UUID uuid, String channelName) {
        if (channelName == getFocusedChannel(uuid)) {
            return true;
        }
        return false;
    }


}