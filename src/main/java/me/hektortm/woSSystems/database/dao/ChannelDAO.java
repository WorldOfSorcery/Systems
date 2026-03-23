package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ChannelDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;

    private final Map<String, Channel> cache = new ConcurrentHashMap<>();

    public ChannelDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Channel.class);
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS playerdata_channels (
                    uuid CHAR(36) NOT NULL,
                    channel_name VARCHAR(255) NOT NULL,
                    joined TINYINT(1) NOT NULL,
                    focused TINYINT(1) NOT NULL,
                    PRIMARY KEY (uuid, channel_name)
                )
            """);
        }
    }

    public List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<>();
        String sql = "SELECT * FROM channels";
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Channel channel = new Channel(
                        rs.getString("color"),
                        rs.getString("name"),
                        rs.getString("short_name"),
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
            WoSSystems.discordLog(Level.SEVERE, "CHD:bbcacc87", "Failed to get all Channels: ", e);
        }
        return channels;
    }

    public List<UUID> getRecipients(String channelName) {
        List<UUID> recipients = new ArrayList<>();
        String sql = "SELECT uuid FROM playerdata_channels WHERE channel_name = ? AND joined = TRUE";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channelName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recipients.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CHD:84a7455b", "Failed to get all Recipients: ", e);
        }
        return recipients;
    }

    public void addRecipient(String channelName, UUID playerUUID) {
        Player p = Bukkit.getPlayer(playerUUID);
        if (isInChannel(playerUUID, channelName)) {
            Utils.info(p, "channel", "info.already-in-channel");
            return;
        }

        String sql = "INSERT INTO playerdata_channels(uuid, channel_name, joined, focused) VALUES(?,?,?,?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();
            Utils.success(p, "channel", "joined", "%channel%", getChannelColor(channelName) + channelName);
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CHD:45982ca4", "Failed to add Recipients: ", e);
        }
    }

    private String getChannelColor(String channelName) {
        String sql = "SELECT color FROM channels WHERE name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channelName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("color");
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CHD:ff921f54", "Failed to get Channel Color: ", e);
        }
        return "";
    }

    public void removeRecipient(String channelName, UUID playerUUID) {
        Player p = Bukkit.getPlayer(playerUUID);
        if (!isInChannel(playerUUID, channelName)) {
            if (p != null) Utils.info(p, "channel", "error.not-in-channel");
            return;
        }
        String sql = "DELETE FROM playerdata_channels WHERE uuid = ? AND channel_name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.executeUpdate();
            if (p != null) Utils.successMsg1Value(p, "channel", "left", "%channel%", getChannelColor(channelName) + channelName);
            if (isFocusedChannel(playerUUID, channelName)) {
                for (Channel channel : getAllChannels()) {
                    if (channel.isDefaultChannel()) {
                        setFocusedChannel(playerUUID, channel.getName());
                    }
                }
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CHD:6cdcea77", "Failed to remove Recipients: ", e);
        }
    }

    public String getChannelPermission(String channelName) {
        String sql = "SELECT permission FROM channels WHERE name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channelName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("permission") : null;
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CHD:f83cba00", "Failed to get Channel Permission: ", e);
            return null;
        }
    }

    public void setFocusedChannel(UUID playerUUID, String channelName) {
        Player p = Bukkit.getPlayer(playerUUID);
        if(!isInChannel(playerUUID, channelName)) {
            String permission = getChannelPermission(channelName);
            if (permission != null && p != null && !p.hasPermission(permission)) {
                Utils.error(p, "channel", "error.no-perms");
                return;
            } else if (permission == null || (p != null && p.hasPermission(permission))) {
                addRecipient(channelName, playerUUID);
            }
        }
        unfocusChannel(playerUUID, getFocusedChannel(playerUUID));
        String sql = "UPDATE playerdata_channels SET focused = TRUE WHERE uuid = ? AND channel_name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.executeUpdate();
            if (p != null) Utils.success(p, "channel", "focused", "%channel%", getChannelColor(channelName) + channelName);
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "26fa1ea1", "Failed to set focused Channel: ", e);
        }
    }

    public void unfocusChannel(UUID playerUUID, String channelName) {
        if (channelName == null) return;

        String sql = "UPDATE playerdata_channels SET focused = FALSE WHERE uuid = ? AND channel_name = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "0cec198f", "Failed to unfocus Channel: ", e);
        }
    }

    public String getFocusedChannel(UUID playerUUID) {
        String sql = "SELECT channel_name FROM playerdata_channels WHERE uuid = ? AND focused = TRUE LIMIT 1";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("channel_name");
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "655992fd", "Failed to get focused Channel: ", e);
        }
        return null;
    }

    public boolean isInChannel(UUID playerUUID, String channelName) {
        String sql = "SELECT 1 FROM playerdata_channels WHERE uuid = ? AND channel_name = ? AND joined = TRUE";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, channelName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "39b72deb", "Failed to check if player is in Channel: ", e);
            return false;
        }
    }

    public boolean isFocusedChannel(UUID uuid, String channelName) {
        return Objects.equals(channelName, getFocusedChannel(uuid));
    }
}