package me.hektortm.woSSystems.chat;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.ChannelData;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChatManager {

    public final Map<String, ChannelData> channels = new HashMap<>(); // Channel name to ChannelData
    public final Map<Player, String> focusedChannel = new HashMap<>(); // Player to current focused channel
    public final Map<String, Set<Player>> channelMembers = new HashMap<>(); // Channel name to members
    private final WoSSystems plugin;
    public final File chatFolder;
    public final File channelsFolder;
    private final File playerDataFile;
    public FileConfiguration playerDataConfig;


    public ChatManager(WoSSystems plugin) {
        this.plugin = plugin;
        chatFolder = new File(plugin.getDataFolder(), "chat");
        channelsFolder = new File (chatFolder, "channels");
        playerDataFile = new File(chatFolder, "player_channels.yml");

        if (!chatFolder.exists()) {
            chatFolder.mkdir();
        }
        if (!channelsFolder.exists()) {
            channelsFolder.mkdir();
        }
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create player_channels.yml!");
            }
        }

        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

        loadChannels();
        loadPlayerData();
    }

    public void savePlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerKey = player.getUniqueId().toString();
            Set<String> joinedChannels = new HashSet<>();

            // Collect joined channels
            for (Map.Entry<String, Set<Player>> entry : channelMembers.entrySet()) {
                if (entry.getValue().contains(player)) {
                    joinedChannels.add(entry.getKey());
                }
            }

            // Save joined channels and focused channel
            playerDataConfig.set(playerKey + ".joined", new ArrayList<>(joinedChannels));
            playerDataConfig.set(playerKey + ".focused", focusedChannel.get(player));
        }

        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player channel data!");
        }
    }


    private void loadPlayerData() {
        for (String playerKey : playerDataConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(playerKey);

            // Load joined channels
            List<String> joinedChannels = playerDataConfig.getStringList(playerKey + ".joined");
            for (String channelName : joinedChannels) {
                ChannelData channel = channels.get(channelName.toLowerCase());
                if (channel != null) {
                    Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
                    if (members != null) {
                        // Add player to channelMembers when they come online
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            Player player = Bukkit.getPlayer(playerUUID);
                            if (player != null && player.isOnline()) {
                                members.add(player);
                            }
                        });
                    }
                }
            }

            // Load focused channel
            String focusedChannelName = playerDataConfig.getString(playerKey + ".focused");
            if (focusedChannelName != null && channels.containsKey(focusedChannelName.toLowerCase())) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null && player.isOnline()) {
                        focusedChannel.put(player, focusedChannelName);
                    }
                });
            }
        }
    }





    private void createDefaultChannels() {
        createChannelFile("Global", "G", "§6Global »", 0, null,
                "%channel% %prefix% §e%name%: %msg%");
        createChannelFile("Local", "L", "§aLocal »", 30, null,
                "%channel% §7%name%: %msg%");
    }

    private void createChannelFile(String name, String shortName, String prefix, int range, String permission, String format) {
        File file = new File(channelsFolder, name.toLowerCase() + ".yml");
        if (file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("name", name);
        config.set("shortName", shortName);
        config.set("prefix", prefix);
        config.set("range", range);
        config.set("permission", permission);
        config.set("format", format);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default channel file for: " + name);
        }
    }

    public void loadChannels() {
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
                String format = config.getString("format");

                if (name != null && shortName != null && prefix != null) {
                    ChannelData channel = new ChannelData(name, shortName, prefix, range, permission, format);
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

    public void serverChat(String channelName, String message) {
        ChannelData channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            Bukkit.getLogger().warning("Channel '"+channelName+"' does not exist.");
            return;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        for (Player recipient : members) {
            recipient.sendMessage(channel.getPrefix() + " §f§lNotice§7: " + message.replace("&", "§"));
        }
    }


    public boolean joinChannel(Player player, String channelName) {
        ChannelData channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            Utils.error(player, "chat", "error.channel-exist");
            return false;
        }

        if (channel.getPermission() != null && !player.hasPermission(channel.getPermission())) {
            Utils.error(player, "chat", "error.channel-exist");
            return false;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        if (members.add(player)) {
            Utils.successMsg1Value(player, "chat", "channel.joined", "%channel%", channel.getName());
            focusChannel(player, channelName); // Automatically focuses the channel when joining
        }

        savePlayerData(); // Save immediately after joining
        return true;
    }


    public boolean leaveChannel(Player player, String channelName) {
        ChannelData channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            Utils.error(player, "chat", "error.channel-exist");
            return false;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        if (members.remove(player)) {
            Utils.successMsg1Value(player, "chat", "channel.left", "%channel%", channel.getName());
            if (focusedChannel.get(player).equalsIgnoreCase(channel.getName())) {
                focusedChannel.remove(player);
            }
        } else {
            Utils.error(player, "chat", "error.not-member");
        }

        savePlayerData(); // Save immediately after leaving
        return true;
    }


    public boolean focusChannel(Player player, String channelName) {
        ChannelData channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            Utils.error(player, "chat", "error.channel-exist");
            return false;
        }

        Set<Player> members = channelMembers.get(channel.getName().toLowerCase());
        if (!members.contains(player)) {
            joinChannel(player, channelName); // Automatically joins the channel if not already joined
        }

        focusedChannel.put(player, channel.getName());
        Utils.successMsg1Value(player, "chat", "channel.focused", "%channel%", channel.getName());

        savePlayerData(); // Save immediately after focusing
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

