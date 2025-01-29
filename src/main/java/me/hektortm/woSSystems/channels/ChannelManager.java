package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.ChannelDAO;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class ChannelManager {
    public final WoSSystems plugin;
    private final Map<String, Channel> channels = new HashMap<>();
    private final DAOHub hub;

    public ChannelManager(WoSSystems plugin, DAOHub hub) {
        this.plugin = plugin;
        this.hub = hub;
        loadChannels();
    }

    public void loadChannels() {
        List<Channel> loadedChannels = hub.getChannelDAO().getAllChannels();
        for (Channel channel : loadedChannels) {
            plugin.writeLog("ChannelManager", Level.INFO, "loaded Channel: " + channel.getName());
            plugin.writeLog("ChannelManager", Level.INFO, "short name: " + channel.getShortName());
            channels.put(channel.getName().toLowerCase(), channel);
        }
    }

    public void saveChannels() {
        for (Channel channel : channels.values()) {
            hub.getChannelDAO().updateChannel(channel);
        }
    }

    public void joinChannel(Player player, String channelName) {
        UUID playerUUID = player.getUniqueId();
        Channel channel = getChannel(channelName);
        if (channel != null) {
            channel.addRecipient(player.getName());
            hub.getChannelDAO().addRecipient(channelName, playerUUID);
        }
    }

    public void leaveChannel(Player player, String channelName) {
        UUID playerUUID = player.getUniqueId();
        Channel channel = getChannel(channelName);
        if (channel != null) {
            channel.removeRecipient(player.getName());
            hub.getChannelDAO().removeRecipient(channelName, playerUUID);
        }
    }

    public Channel getChannel(String name) {
        return channels.get(name.toLowerCase());
    }

    public Collection<Channel> getChannels() {
        return channels.values();
    }

    public void createChannel(String color, String name, String shortName, String format, List<String> recipients,
                              boolean defaultChannel, boolean autoJoin, boolean forceJoin, boolean hidden,
                              String permission, boolean broadcastable, int radius) {
        Channel channel = new Channel(color, name, shortName, format, recipients, defaultChannel, autoJoin, forceJoin, hidden, permission, broadcastable, radius);
        channels.put(name.toLowerCase(), channel);
        hub.getChannelDAO().insertChannel(channel);
    }

    public void autoJoin(Player player) {
        for (Channel channel : getChannels()) {
            if (channel.isAutoJoin()) {
                String name = channel.getName();
                joinChannel(player, name);
            }
        }
    }

    public void forceJoin(Player player) {
        for (Channel channel : getChannels()) {
            if (channel.isForceJoin()) {
                String name = channel.getName();
                if (!hub.getChannelDAO().isInChannel(player.getUniqueId(), name)) {
                    joinChannel(player, name);
                }

            }
        }
    }

    public void joinDefault(Player player) {
        if (getChannelDAO().getFocusedChannel(player.getUniqueId()) == null) {
            for (Channel channel : getChannels()) {
                if (channel.isDefaultChannel()) {
                    setFocus(player, channel);
                }
            }
        }

    }

    public void deleteChannel(String name) {
        channels.remove(name.toLowerCase());
        hub.getChannelDAO().deleteChannel(name);
    }

    public void setFocus(Player player, Channel channel) {
        UUID playerUUID = player.getUniqueId();
        hub.getChannelDAO().setFocusedChannel(playerUUID, channel.getName());
    }

    public Channel getFocusedChannel(Player player) {
        String channelName = hub.getChannelDAO().getFocusedChannel(player.getUniqueId());
        return channelName != null ? getChannel(channelName) : null;
    }

    public ChannelDAO getChannelDAO() {
        return hub.getChannelDAO();
    }

}