package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.ChannelDAO;
import me.hektortm.woSSystems.utils.Icons;
import me.hektortm.wosCore.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class ChannelManager {
    public Inventory itemPreview = Bukkit.createInventory(null, InventoryType.DISPENSER, "");
    public final WoSSystems plugin;
    private final Map<String, Channel> channels = new HashMap<>();
    public final Map<UUID, Runnable> clickActions = new HashMap<>();
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
            Component chatComponent = getFormattedMessage(focusedChannel, player, message);

            // Send the message to recipients based on the channel's radius
            for (UUID recipientUUID : getChannelDAO().getRecipients(focusedChannel.getName())) {
                Player recipient = Bukkit.getPlayer(recipientUUID);
                if (recipient != null) {
                    // Check if the recipient is within the radius (if radius is enabled)
                    if (focusedChannel.getRadius() == -1 || isWithinRadius(senderLocation, recipient.getLocation(), focusedChannel.getRadius())) {
                        recipient.sendMessage(chatComponent);
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
            Component chatComponent = getFormattedMessage(focusedChannel, player, message);

            // Send the message to recipients based on the channel's radius
            for (UUID recipientUUID : getChannelDAO().getRecipients(focusedChannel.getName())) {
                Player recipient = Bukkit.getPlayer(recipientUUID);
                if (recipient != null) {
                    // Check if the recipient is within the radius (if radius is enabled)
                    if (focusedChannel.getRadius() == -1 || isWithinRadius(senderLocation, recipient.getLocation(), focusedChannel.getRadius())) {
                        recipient.sendMessage(chatComponent);
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


    public Component getFormattedMessage(Channel channel, Player sender, String message) {
        String format = channel.getFormat();

        NicknameManager nickManager = new NicknameManager();
        // Use the player's nickname if available, otherwise use their username
        String name = nickManager.getNickname(sender) != null ?
                nickManager.getNickname(sender).replace("_", " ") :
                sender.getName();

        // Create a hoverable player name component
        Component playerComponent = Component.text(name)
                .hoverEvent(HoverEvent.showText(getPlayerStats(sender)));

        // Handle the item in the player's main hand
        ItemStack item = sender.getInventory().getItemInMainHand();
        String itemName = "air";
        String loreString = "";
        if (item != null && !item.getType().isAir()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            String id = data.get(plugin.getCitemManager().getIdKey(), PersistentDataType.STRING);

            if (id != null) {
                ItemStack citem = plugin.getCitemManager().getCitemDAO().getCitem(id);

                if (citem != null) {
                    // Get display name and lore from the custom item
                    itemName = hub.getCitemDAO().getDisplayName(id);
                    List<String> lore = hub.getCitemDAO().getLore(id);

                    // Build the lore into a single string with line breaks
                    StringBuilder loreBuilder = new StringBuilder();
                    for (int i = 0; i < lore.size(); i++) {
                        loreBuilder.append(lore.get(i));
                        if (i < lore.size() - 1) {
                            loreBuilder.append("\n");
                        }
                    }
                    loreString = loreBuilder.toString();
                } else {
                    itemName = "§cInvalid CItem";
                }
            }
        }

        // Create the item component with hover text
        Component itemComponent = Component.text("§7[" + itemName + "§7]")
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text(loreString)));

        // Replace [item] in the message with the item component
        Component messageComponent = Component.text(message)
                .replaceText(builder -> builder.matchLiteral("[item]").replacement(itemComponent));

        // Split the format into parts and build the final message
        String[] formatParts = format.split("\\{player\\}|\\{message\\}");
        Component finalMessage = Component.empty();

        for (int i = 0; i < formatParts.length; i++) {
            finalMessage = finalMessage.append(Component.text(formatParts[i]));

            if (i == 0) {
                // Insert the player component after the first part
                finalMessage = finalMessage.append(playerComponent);
            } else if (i == 1) {
                // Insert the message component after the second part
                finalMessage = finalMessage.append(messageComponent);
            }
        }

        // Send the final message to the player

        return finalMessage; // Return the message as a string (optional)
    }

    private @NotNull ComponentLike getPlayerStats(Player player) {
        return  Component.text("§eRank: §f" + Icons.RANK_HEADSTAFF.getIcon() + "\n" +
                "§6Gold: §f" + hub.getEconomyDAO().getPlayerCurrency(player, "gold") + "\n" +
                "§aLevel: §f" + player.getLevel());
    }

    public void viewItem(Player p, ItemStack item) {
        itemPreview.setItem(4, item);
        p.openInventory(itemPreview);
    }

    public ChannelDAO getChannelDAO() {
        return hub.getChannelDAO();
    }

}