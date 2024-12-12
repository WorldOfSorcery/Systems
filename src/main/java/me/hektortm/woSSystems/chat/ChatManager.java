package me.hektortm.woSSystems.chat;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.ChannelData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatManager {

    public final Map<String, ChannelData> channels = new HashMap<>(); // Channel name to ChannelData
    public final Map<Player, String> focusedChannel = new HashMap<>(); // Player to current focused channel
    public final Map<String, Set<Player>> channelMembers = new HashMap<>(); // Channel name to members
    private final WoSSystems plugin;
    public final File chatFolder;
    public final File channelsFolder;

    public ChatManager(WoSSystems plugin) {
        this.plugin = plugin;
        chatFolder = new File(plugin.getDataFolder(), "chat");
        channelsFolder = new File (chatFolder, "channels");
    }

    /**
     * Registers a new chat channel.
     */
    public void registerChannel(ChannelData channel) {
        channels.put(channel.getName().toLowerCase(), channel);
        channels.put(channel.getShortName().toLowerCase(), channel);
        channelMembers.put(channel.getName().toLowerCase(), new HashSet<>());
    }

    /**
     * Joins a channel.
     */
    public boolean joinChannel(Player player, String channelName) {
        ChannelData channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            player.sendMessage(ChatColor.RED + "Channel " + channelName + " does not exist.");
            return false;
        }

        if (channel.getPermission() != null && !player.hasPermission(channel.getPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to join this channel.");
            return false;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        if (members.add(player)) {
            player.sendMessage(ChatColor.GREEN + "You have joined the channel: " + channel.getName());
        }

        return true;
    }

    /**
     * Leaves a channel.
     */
    public boolean leaveChannel(Player player, String channelName) {
        ChannelData channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            player.sendMessage(ChatColor.RED + "Channel " + channelName + " does not exist.");
            return false;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        if (members.remove(player)) {
            player.sendMessage(ChatColor.GREEN + "You have left the channel: " + channel.getName());
            if (focusedChannel.get(player).equalsIgnoreCase(channel.getName())) {
                focusedChannel.remove(player);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are not a member of this channel.");
        }

        return true;
    }

    /**
     * Sets a player's focused channel.
     */
    public boolean focusChannel(Player player, String channelName) {
        ChannelData channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            player.sendMessage(ChatColor.RED + "Channel " + channelName + " does not exist.");
            return false;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        if (!members.contains(player)) {
            player.sendMessage(ChatColor.RED + "You must join the channel before focusing it.");
            return false;
        }

        focusedChannel.put(player, channel.getName());
        player.sendMessage(ChatColor.GREEN + "You are now focused on channel: " + channel.getName());
        return true;
    }

    /**
     * Sends a message to the player's focused channel.
     */



    public void sendMessage(Player sender, String message) {
        String focusedChannelName = focusedChannel.get(sender);
        if (focusedChannelName == null) {
            sender.sendMessage(ChatColor.RED + "You are not focused on any channel.");
            return;
        }

        ChannelData channel = channels.get(focusedChannelName.toLowerCase());
        if (channel == null) {
            sender.sendMessage(ChatColor.RED + "The channel you are focused on does not exist.");
            return;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        for (Player recipient : members) {
            if (channel.getRange() > 0) {
                Location senderLocation = sender.getLocation();
                Location recipientLocation = recipient.getLocation();
                if (senderLocation.distance(recipientLocation) > channel.getRange()) {
                    continue; // Skip if out of range
                }
            }

            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', channel.getPrefix()) +
                    ChatColor.RESET + " " + sender.getName() + ": " + message);
        }
    }

    /**
     * Gets all registered channels.
     */
    public Set<ChannelData> getChannels() {
        return new HashSet<>(channels.values());
    }

    /**
     * Removes a player from all channels.
     */
    public void removePlayer(Player player) {
        for (Set<Player> members : channelMembers.values()) {
            members.remove(player);
        }
        focusedChannel.remove(player);
    }
}

