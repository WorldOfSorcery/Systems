package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChannelManager {
    public final WoSSystems plugin;
    private final Map<String, Channel> channels = new HashMap<>();
    private final File channelFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "channels");
    private final File channelsFile;
    private final FileConfiguration channelsConfig;
    private final File focusedFile;
    private final FileConfiguration focusedConfig;

    public ChannelManager(WoSSystems plugin) {
        this.plugin = plugin;
        channelsFile = new File(channelFolder, "channels.yml");
        focusedFile = new File(channelFolder, "focused.yml");
        channelsConfig = YamlConfiguration.loadConfiguration(channelsFile);
        focusedConfig = YamlConfiguration.loadConfiguration(focusedFile);
        if (!channelFolder.exists()) {
            channelFolder.mkdirs();
        }
        if (!focusedFile.exists()) {
            try {
                if (!focusedFile.getParentFile().exists()) {
                    focusedFile.getParentFile().mkdirs();  // Create the parent directory if it doesn't exist
                }

                if (!focusedFile.createNewFile()) {
                    plugin.getLogger().severe("Could not create focused.yml file!");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("An error occurred while creating the focused.yml file.");
                e.printStackTrace();
            }
        }


    }

    public void loadChannels() {
        if (!channelsFile.exists()) {
            try {
                if (!channelsFile.getParentFile().exists()) {
                    channelsFile.getParentFile().mkdirs();  // Create the parent directory if it doesn't exist
                }
                if (!channelsFile.createNewFile()) {
                    plugin.getLogger().severe("Could not create channels.yml file!");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("An error occurred while creating the channels.yml file.");
                e.printStackTrace();
            }
        }

        // Now, load the channels after ensuring the file exists
        for (String key : channelsConfig.getKeys(false)) {
            String name = key;
            String shortName = channelsConfig.getString(key + ".shortName");
            String format = channelsConfig.getString(key + ".format"); // Ensure the format is fetched
            List<String> recipients = channelsConfig.getStringList(key + ".recipients");
            boolean autoJoin = channelsConfig.getBoolean(key + ".autoJoin");
            boolean forceJoin = channelsConfig.getBoolean(key + ".forceJoin");
            int radius = channelsConfig.getInt(key + ".radius", -1);

            // Error handling in case the format is invalid or missing
            if (format == null || format.isEmpty()) {
                plugin.getLogger().warning("Channel format for " + name + " is invalid or missing. Defaulting to a placeholder format.");
                format = "default_format"; // Set a default value
            }

            Channel channel = new Channel(name, shortName, format, recipients, autoJoin, forceJoin, radius);
            channels.put(name.toLowerCase(), channel);
        }
    }


    public void saveChannels() {
        for (Channel channel : channels.values()) {
            String key = channel.getName();
            channelsConfig.set(key + ".shortName", channel.getShortName());
            channelsConfig.set(key + ".format", channel.getFormat());
            channelsConfig.set(key + ".recipients", channel.getRecipients());
            channelsConfig.set(key + ".autoJoin", channel.isAutoJoin());
            channelsConfig.set(key + ".forceJoin", channel.isForceJoin());
            channelsConfig.set(key + ".radius", channel.getRadius());
        }

        try {
            channelsConfig.save(channelsFile); // Save the configuration
            plugin.getLogger().info("Channels saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save channels.yml!");
            e.printStackTrace();
        }
    }


    public void joinChannel(Player player, String channel) {
        String playerS = player.getName();

        // Retrieve the current recipients list
        List<String> recipients = channelsConfig.getStringList(channel + ".recipients");
        if (!recipients.contains(playerS)) {
            recipients.add(playerS); // Add the player to the list if not already in it
        }
        channelsConfig.set(channel + ".recipients", recipients);

        // Add recipient to the in-memory Channel object
        Channel c = getChannel(channel);
        c.addRecipient(playerS);

        // Save the updated channels.yml
        try {
            channelsConfig.save(channelsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveChannels(); // Ensure all data is persisted
    }

    public void leaveChannel(Player player, String channel) {
        String playerS = player.getName();

        // Retrieve the current recipients list
        List<String> recipients = channelsConfig.getStringList(channel + ".recipients");
        recipients.remove(playerS); // Remove player from recipients list
        channelsConfig.set(channel + ".recipients", recipients);

        // Remove recipient from the in-memory Channel object
        Channel c = getChannel(channel);
        c.removeRecipient(playerS);

        // Save the updated channels.yml
        try {
            channelsConfig.save(channelsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveChannels(); // Ensure all data is persisted
    }


    public Channel getChannel(String name) {
        return channels.get(name.toLowerCase());
    }

    public Collection<Channel> getChannels() {
        return channels.values();
    }

    public void createChannel(String name, String shortName, String format, List<String> recipients, boolean autoJoin, boolean forceJoin, int radius) {
        Channel channel = new Channel(name, shortName, format, recipients, autoJoin, forceJoin, radius);
        channels.put(name.toLowerCase(), channel);
        saveChannels();
    }

    public void deleteChannel(String name) {
        channels.remove(name.toLowerCase());
    }

    public void setFocus(Player player, Channel channel) {
        UUID uuid = player.getUniqueId();
        focusedConfig.set(uuid.toString(), channel.getName());
        try {
            focusedConfig.save(focusedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Channel getFocusedChannel(Player player) {
        Channel channel = channels.get(focusedConfig.get(player.getUniqueId().toString()));

        return channel;
    }

}