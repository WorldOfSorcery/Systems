package me.hektortm.woSSystems.chat;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.ChannelData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
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

        if (!chatFolder.exists()) {
            chatFolder.mkdir();
        }
        if (!channelsFolder.exists()) {
            channelsFolder.mkdir();
        }

        loadChannels();
    }

    private void createDefaultChannels() {
        createChannelFile("Global", "G", "§6Global »", 0, null);
        createChannelFile("Local", "L", "§aLocal »", 30, null);
    }

    private void createChannelFile(String name, String shortName, String prefix, int range, String permission) {
        File file = new File(channelsFolder, name.toLowerCase() + ".yml");
        if (file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("name", name);
        config.set("shortName", shortName);
        config.set("prefix", prefix);
        config.set("range", range);
        config.set("permission", permission);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default channel file for: " + name);
        }
    }

    private void loadChannels() {
        File[] files = channelsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No channel files found. Creating default channels.");
            createDefaultChannels();
            files = channelsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        }

        if (files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                String shortName = config.getString("shortName");
                String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("prefix"));
                int range = config.getInt("range", 0);
                String permission = config.getString("permission");

                if (name != null && shortName != null && prefix != null) {
                    ChannelData channel = new ChannelData(name, shortName, prefix, range, permission);
                    registerChannel(channel);
                    plugin.getLogger().info("Loaded channel: " + name);
                } else {
                    plugin.getLogger().warning("Invalid channel file: " + file.getName());
                }
            }
        }
    }

    public void registerChannel(ChannelData channel) {
        channels.put(channel.getName().toLowerCase(), channel);
        channels.put(channel.getShortName().toLowerCase(), channel);
        channelMembers.put(channel.getName().toLowerCase(), new HashSet<>());
    }

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

    public Set<ChannelData> getChannels() {
        return new HashSet<>(channels.values());
    }

    public void removePlayer(Player player) {
        for (Set<Player> members : channelMembers.values()) {
            members.remove(player);
        }
        focusedChannel.remove(player);
    }
}

