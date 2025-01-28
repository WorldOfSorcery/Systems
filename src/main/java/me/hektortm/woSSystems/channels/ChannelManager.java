package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DatabaseManager;
import me.hektortm.woSSystems.database.dao.ChannelDAO;
import org.bukkit.entity.Player;

import java.util.*;

public class ChannelManager {
    public final WoSSystems plugin;
    private final Map<String, Channel> channels = new HashMap<>();
    private final DatabaseManager db;

    public ChannelManager(WoSSystems plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        loadChannels();
    }

    public void loadChannels() {
        List<Channel> loadedChannels = db.getChannelDAO().getAllChannels();
        for (Channel channel : loadedChannels) {
            channels.put(channel.getName().toLowerCase(), channel);
        }
    }

    public void saveChannels() {
        for (Channel channel : channels.values()) {
            db.getChannelDAO().updateChannel(channel);
        }
    }

    public void joinChannel(Player player, String channelName) {
        UUID playerUUID = player.getUniqueId();
        Channel channel = getChannel(channelName);
        if (channel != null) {
            channel.addRecipient(player.getName());
            db.getChannelDAO().addRecipient(channelName, playerUUID);
        }
    }

    public void leaveChannel(Player player, String channelName) {
        UUID playerUUID = player.getUniqueId();
        Channel channel = getChannel(channelName);
        if (channel != null) {
            channel.removeRecipient(player.getName());
            db.getChannelDAO().removeRecipient(channelName, playerUUID);
        }
    }

    public Channel getChannel(String name) {
        return channels.get(name.toLowerCase());
    }

    public Collection<Channel> getChannels() {
        return channels.values();
    }

    public void createChannel(String name, String shortName, String format, List<String> recipients, boolean autoJoin, boolean forceJoin, boolean hidden, int radius) {
        Channel channel = new Channel(name, shortName, format, recipients, autoJoin, forceJoin, hidden, radius);
        channels.put(name.toLowerCase(), channel);
        db.getChannelDAO().insertChannel(channel);
    }

    public void deleteChannel(String name) {
        channels.remove(name.toLowerCase());
        db.getChannelDAO().deleteChannel(name);
    }

    public void setFocus(Player player, Channel channel) {
        UUID playerUUID = player.getUniqueId();
        db.getChannelDAO().setFocusedChannel(playerUUID, channel.getName());
    }

    public Channel getFocusedChannel(Player player) {
        String channelName = db.getChannelDAO().getFocusedChannel(player.getUniqueId());
        return channelName != null ? getChannel(channelName) : null;
    }
}