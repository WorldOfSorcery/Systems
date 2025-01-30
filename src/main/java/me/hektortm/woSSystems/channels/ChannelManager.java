package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.ChannelDAO;
import me.hektortm.woSSystems.utils.Icons;
import me.hektortm.wosCore.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

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


    public void joinChannel(Player player, String channelIdentifier) {
        UUID playerUUID = player.getUniqueId();
        Channel channel = getChannel(channelIdentifier); // Try to get by normal name
        if (channel == null) {
            channel = getChannelByShortName(channelIdentifier); // Try to get by short name
        }

        if (channel != null) {
            if (channel.getPermission() != null && !player.hasPermission(channel.getPermission())) {
                Utils.error(player, "channel", "error.no-perms");
                return;
            }
            channel.addRecipient(player.getName());
            hub.getChannelDAO().addRecipient(channel.getName(), playerUUID);
        } else {
            Utils.error(player, "channel", "error.not-found");
        }
    }

    public void leaveChannel(Player player, String channelIdentifier) {
        UUID playerUUID = player.getUniqueId();
        Channel channel = getChannel(channelIdentifier); // Try to get by normal name
        if (channel == null) {
            channel = getChannelByShortName(channelIdentifier); // Try to get by short name
        }

        if (channel != null) {
            channel.removeRecipient(player.getName());
            hub.getChannelDAO().removeRecipient(channel.getName(), playerUUID);
        } else {
            Utils.error(player, "channel", "error.not-found");
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
                    setFocus(player, channel.getName());
                }
            }
        }

    }

    public void deleteChannel(String name) {
        channels.remove(name.toLowerCase());
        hub.getChannelDAO().deleteChannel(name);
    }

    public void setFocus(Player player, String channelIdentifier) {
        UUID playerUUID = player.getUniqueId();
        Channel channel = getChannel(channelIdentifier); // Try to get by normal name
        if (channel == null) {
            channel = getChannelByShortName(channelIdentifier); // Try to get by short name
        }

        if (channel != null) {
            hub.getChannelDAO().setFocusedChannel(playerUUID, channel.getName());
        } else {
            Utils.error(player, "channel", "error.not-found");
        }
    }

    public Channel getFocusedChannel(Player player) {
        String channelName = hub.getChannelDAO().getFocusedChannel(player.getUniqueId());
        return channelName != null ? getChannel(channelName) : null;
    }

    public Channel getChannelByShortName(String shortName) {
        for (Channel channel : channels.values()) {
            if (channel.getShortName().equalsIgnoreCase(shortName)) {
                return channel;
            }
        }
        return null; // No channel found with the given short name
    }

    public void sendMessage(AsyncPlayerChatEvent event, Player player, String message) {
        Channel focusedChannel = getFocusedChannel(player);
        if (focusedChannel != null) {
            event.setCancelled(true); // Cancel the default chat event

            // Get the sender's location
            Location senderLocation = player.getLocation();

            // Create the chat component for the message
            BaseComponent[] chatComponent = TextComponent.fromLegacyText(getFormattedMessage(focusedChannel, player, message));

            // Send the message to recipients based on the channel's radius
            for (UUID recipientUUID : getChannelDAO().getRecipients(focusedChannel.getName())) {
                Player recipient = Bukkit.getPlayer(recipientUUID);
                if (recipient != null) {
                    // Check if the recipient is within the radius (if radius is enabled)
                    if (focusedChannel.getRadius() == -1 || isWithinRadius(senderLocation, recipient.getLocation(), focusedChannel.getRadius())) {
                        recipient.spigot().sendMessage(chatComponent);
                    }
                }
            }
        }
    }

    public void sendMessagePerCommand(Player player, String channel, String message) {
        Channel focusedChannel = getChannel(channel);
        if (focusedChannel != null) {
            // Get the sender's location
            Location senderLocation = player.getLocation();

            // Create the chat component for the message
            BaseComponent[] chatComponent = TextComponent.fromLegacyText(getFormattedMessage(focusedChannel, player, message));

            // Send the message to recipients based on the channel's radius
            for (UUID recipientUUID : getChannelDAO().getRecipients(focusedChannel.getName())) {
                Player recipient = Bukkit.getPlayer(recipientUUID);
                if (recipient != null) {
                    // Check if the recipient is within the radius (if radius is enabled)
                    if (focusedChannel.getRadius() == -1 || isWithinRadius(senderLocation, recipient.getLocation(), focusedChannel.getRadius())) {
                        recipient.spigot().sendMessage(chatComponent);
                    }
                }
            }
        }
    }

    private boolean isWithinRadius(Location senderLocation, Location recipientLocation, int radius) {
        if (radius <= 0) {
            return true; // No radius restriction
        }

        // Ensure both locations are in the same world
        if (!senderLocation.getWorld().equals(recipientLocation.getWorld())) {
            return false;
        }

        // Calculate the distance between the sender and recipient
        return senderLocation.distanceSquared(recipientLocation) <= (radius * radius);
    }

    public String getFormattedMessage(Channel channel, Player sender, String message) {
        String format = channel.getFormat();
        NicknameManager nickManager = new NicknameManager();
        // Use the player's nickname if available, otherwise use their username
        String name = nickManager.getNickname(sender) != null ?
                nickManager.getNickname(sender).replace("_", " ") :
                sender.getName();

        // Replace {player} with a hoverable player name component
        TextComponent playerComponent = new TextComponent(name);
        playerComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(getPlayerStats(sender)).create()
        ));

        // Replace {player} and {message} placeholders
        format = format
                .replace("{player}", "%player%") // Placeholder for the player component
                .replace("{message}", message);

        // Replace [item] with the item's name and hover text
        if (format.contains("[item]")) {
            ItemStack item = sender.getInventory().getItemInMainHand();
            if (item != null && !item.getType().isAir()) {
                TextComponent itemComponent = createItemComponent(item);
                format = format.replace("[item]", "%item%"); // Placeholder for the item component
                return format.replace("%player%", "").replace("%item%", ""); // Replace with the actual components
            } else {
                format = format.replace("[item]", "nothing"); // If the player is not holding an item
            }
        }

        return format.replace("%player%", ""); // Replace with the actual player component
    }

    private TextComponent createItemComponent(ItemStack item) {
        TextComponent itemComponent = new TextComponent(getItemName(item));
        itemComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new ComponentBuilder(getItemHoverText(item)).create()
        ));
        return itemComponent;
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().toString().toLowerCase().replace("_", " ");
    }

    private String getItemHoverText(ItemStack item) {
        StringBuilder hoverText = new StringBuilder();
        hoverText.append(getItemName(item)).append("\n");

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                hoverText.append(line).append("\n");
            }
        }

        return hoverText.toString().trim();
    }
    private String getPlayerStats(Player player) {
        return  "§eRank: " + Icons.RANK_HEADSTAFF.getIcon() + "\n" +
                "§6Gold: §f" + hub.getEconomyDAO().getPlayerCurrency(player, "gold") + "\n" +
                "§aLevel: §f" + player.getLevel();
    }

    public ChannelDAO getChannelDAO() {
        return hub.getChannelDAO();
    }

}