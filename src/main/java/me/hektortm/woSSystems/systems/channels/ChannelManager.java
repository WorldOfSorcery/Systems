package me.hektortm.woSSystems.systems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.ChannelDAO;
import me.hektortm.woSSystems.systems.regions.RegionHandler;
import me.hektortm.woSSystems.utils.model.Channel;
import me.hektortm.woSSystems.utils.types.CosmeticType;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.wosCore.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages chat channel state, membership, focus, and message formatting.
 *
 * <p>Channel definitions are loaded from the database on startup and kept in
 * an in-memory map.  Player subscription and focus state is persisted via
 * {@link ChannelDAO}.</p>
 *
 * <p>The formatted message pipeline ({@link #getFormattedMessage}) resolves
 * player cosmetics, nicknames, held-item previews and region display names
 * into a rich Adventure {@link Component}.</p>
 */
public class ChannelManager {
    public Inventory itemPreview = Bukkit.createInventory(null, InventoryType.DISPENSER, "Viewing Item");
    public final WoSSystems plugin;
    private final Map<String, Channel> channels = new HashMap<>();
    private final DAOHub hub;

    /**
     * @param plugin the plugin instance (used for logging and click-action storage)
     * @param hub    the DAO hub
     */
    public ChannelManager(WoSSystems plugin, DAOHub hub) {
        this.plugin = plugin;
        this.hub = hub;
        loadChannels();
    }

    /**
     * Loads all channel definitions from the database into the in-memory map.
     * Called automatically from the constructor and can be called again to refresh.
     */
    public void loadChannels() {
        List<Channel> loadedChannels = hub.getChannelDAO().getAllChannels();
        for (Channel channel : loadedChannels) {
            plugin.writeLog("ChannelManager", Level.INFO, "loaded Channel: " + channel.getName());
            plugin.writeLog("ChannelManager", Level.INFO, "short name: " + channel.getShortName());
            channels.put(channel.getName().toLowerCase(), channel);
        }
    }

    /**
     * Persists channel state to the database.
     * Currently a no-op; channel membership is written live via {@link ChannelDAO}.
     */
    public void saveChannels() {
        for (Channel channel : channels.values()) {
            plugin.writeLog("ChannelDAO", Level.FINE, "This is where the update used to be");
        }
    }

    /**
     * Subscribes a player to a channel.  Accepts either the channel's full name
     * or its short name as the identifier.  Sends an error message if the channel
     * is not found or the player lacks the required permission.
     *
     * @param player            the player joining
     * @param channelIdentifier the channel name or short name
     */
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

    /**
     * Removes a player's subscription from a channel.  Accepts either the
     * channel's full name or its short name.  Sends an error if the channel
     * is not found.
     *
     * @param player            the player leaving
     * @param channelIdentifier the channel name or short name
     */
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

    /**
     * Returns the {@link Channel} with the given name (case-insensitive), or
     * {@code null} if no such channel is loaded.
     *
     * @param name the channel name
     * @return the channel, or {@code null}
     */
    public Channel getChannel(String name) {
        return channels.get(name.toLowerCase());
    }

    /** @return an unmodifiable view of all loaded channels */
    public Collection<Channel> getChannels() {
        return channels.values();
    }

    /**
     * Automatically joins the player to every channel flagged as {@code autoJoin}.
     *
     * @param player the player to auto-join
     */
    public void autoJoin(Player player) {
        for (Channel channel : getChannels()) {
            if (channel.isAutoJoin()) {
                String name = channel.getName();
                joinChannel(player, name);
            }
        }
    }

    /**
     * Joins the player to every channel flagged as {@code forceJoin}, skipping
     * channels the player is already subscribed to.
     *
     * @param player the player to force-join
     */
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

    /**
     * Sets the player's focused channel to the default channel if they do not
     * currently have any focused channel.
     *
     * @param player the player to assign a default focus to
     */
    public void joinDefault(Player player) {
        if (getChannelDAO().getFocusedChannel(player.getUniqueId()) == null) {
            for (Channel channel : getChannels()) {
                if (channel.isDefaultChannel()) {
                    setFocus(player, channel.getName());
                }
            }
        }

    }

    /**
     * Sets the player's active (focused) channel to the given channel.  Accepts
     * either a full name or a short name.  Sends an error if the channel is not
     * found.
     *
     * @param player            the player
     * @param channelIdentifier the channel name or short name to focus
     */
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

    /**
     * Returns the {@link Channel} the player is currently focused on, or
     * {@code null} if no channel is focused.
     *
     * @param player the player to query
     * @return the focused channel, or {@code null}
     */
    public Channel getFocusedChannel(Player player) {
        String channelName = hub.getChannelDAO().getFocusedChannel(player.getUniqueId());
        return channelName != null ? getChannel(channelName) : null;
    }

    /**
     * Finds a channel by its short name (case-insensitive).
     *
     * @param shortName the short name to search for
     * @return the matching {@link Channel}, or {@code null} if not found
     */
    public Channel getChannelByShortName(String shortName) {
        for (Channel channel : channels.values()) {
            if (channel.getShortName().equalsIgnoreCase(shortName)) {
                return channel;
            }
        }
        return null; // No channel found with the given short name
    }

    /**
     * Handles an async chat event by routing the message to the player's focused
     * channel.  Cancels the default chat event and delivers the formatted message
     * only to recipients who are within the channel's configured radius (or all
     * recipients if radius is {@code -1}).
     *
     * @param event   the original async chat event (will be cancelled)
     * @param player  the sending player
     * @param message the raw chat message
     */
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

    /**
     * Sends a message to a specific channel by name (bypasses focus).  Used by
     * channel-specific slash commands.
     *
     * @param player  the sending player
     * @param channel the target channel name
     * @param message the raw chat message
     */
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

    /**
     * Returns {@code true} if the recipient is within {@code radius} blocks of
     * the sender and both are in the same world.  A radius {@code <= 0} is
     * treated as unrestricted.
     *
     * @param senderLocation    the sender's location
     * @param recipientLocation the recipient's location
     * @param radius            the maximum allowed distance in blocks
     * @return {@code true} if within range
     */
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

    /**
     * Builds a fully-formatted Adventure {@link Component} for a chat message.
     *
     * <p>The component resolves the following placeholders in the channel's
     * format string:</p>
     * <ul>
     *   <li>{@code {badge}} — equipped badge cosmetic with hover description</li>
     *   <li>{@code {prefix_player}} — equipped prefix + player name with stat hover</li>
     *   <li>{@code {player}} — plain player name with stat hover</li>
     *   <li>{@code {message}} — chat message, with {@code [item]} replaced by a
     *       clickable item preview component</li>
     * </ul>
     *
     * @param channel the channel whose format string is used
     * @param sender  the sending player
     * @param message the raw chat message
     * @return the fully-assembled chat component
     */
    public Component getFormattedMessage(Channel channel, Player sender, String message) {
        String format = channel.getFormat();

        NicknameManager nickManager = new NicknameManager(hub);
        String name = nickManager.getNickname(sender) != null ?
                nickManager.getNickname(sender).replace("_", " ") :
                sender.getName();

        // BADGE COMPONENT

        String badgeID = hub.getCosmeticsDAO().getCurrentCosmeticId(sender, CosmeticType.BADGE);
        Component badgeC = Utils.parseColorCodes(hub.getCosmeticsDAO().getCurrentCosmetic(sender, CosmeticType.BADGE));
        if (badgeC == null) {
            badgeC = Component.text("");  // Set a default empty string if null
        }

        Component badgeComponent;
        Component badgeString = Utils.parseColorCodes(hub.getCosmeticsDAO().getCosmeticDescription(CosmeticType.BADGE, badgeID));
        if (badgeID != null) {
            badgeComponent = badgeC
                    .hoverEvent(HoverEvent.showText(badgeString));
        } else {
            badgeComponent = Component.text("");
        }

        // PREFIX COMPONENT

        String prefixID = hub.getCosmeticsDAO().getCurrentCosmeticId(sender, CosmeticType.PREFIX);
        Component prefix = Utils.parseColorCodes(hub.getCosmeticsDAO().getCurrentCosmetic(sender, CosmeticType.PREFIX) + " " + name);
        if (prefix == null) {
            prefix = Component.text("");  // Set a default empty string if null
        }

        Component prefixComponent;
        if (prefixID != null) {
            prefixComponent = prefix
                    .hoverEvent(HoverEvent.showText(getPlayerStats(sender)));
        } else {
            prefixComponent = Component.text("");
        }

        // PLAYER COMPONENT

        Component playerComponent = Component.text(name).style(Style.empty())
                .hoverEvent(HoverEvent.showText(getPlayerStats(sender)));

        // Handle the item in the player's main hand
        ItemStack item = sender.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        String itemName;
        if (item.hasItemMeta() && meta.hasDisplayName()) {
            itemName = meta.getDisplayName();
        } else {
            String typeName = item.getType().name().toLowerCase().replace("_", " ");
            String[] parts = typeName.split(" ");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    result.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1))
                            .append(" ");
                }
            }
            itemName = result.toString().trim();
        }

        List<String> lore = item.hasItemMeta() ? meta.getLore() : null;
        String loreString = "";
        if (item.hasItemMeta()) {
            if (lore != null) {
                StringBuilder loreBuilder = new StringBuilder();
                for (int i = 0; i < lore.size(); i++) {
                    loreBuilder.append(lore.get(i));
                    if (i < lore.size() - 1) {
                        loreBuilder.append("\n");
                    }
                }
                loreString = loreBuilder.toString();
            }
        }

        if (item == null || item.getType() == Material.AIR) {
            itemName = "Air";
            loreString = "";
        }
        if (loreString.isEmpty()) {
            loreString = "§bClick to view";
        } else {
            loreString = loreString + "\n \n§bClick to view";
        }
        loreString = itemName + "\n" + loreString;
        // Create the item component with hover text
        UUID clickId = UUID.randomUUID();
        Component itemComponent = LegacyComponentSerializer.legacySection().deserialize("§7[" + itemName + "§7]")
                .hoverEvent(HoverEvent.showText(Component.text("").append(Utils.parseColorCodes(loreString))))
                .clickEvent(ClickEvent.runCommand("/internalviewitem " + clickId));

        // Store the action in the clickActions map
        plugin.getClickActions().put(clickId, viewItem(item));

        // Replace [item] in the message with the item component
        Component messageComponent = LegacyComponentSerializer.legacySection().deserialize(message)
                .replaceText(builder -> builder.matchLiteral("[item]").replacement(itemComponent));

        // Parse the format into a Component using LegacyComponentSerializer
        Component formatComponent = LegacyComponentSerializer.legacySection().deserialize(format);

        // Replace {player} and {message} placeholders in the format
        Component finalMessage = formatComponent
                .replaceText(builder -> builder.matchLiteral("{badge}").replacement(badgeComponent))
                .replaceText(builder -> builder.matchLiteral("{prefix_player}").replacement(prefixComponent))
                .replaceText(builder -> builder.matchLiteral("{player}").replacement(playerComponent))
                .replaceText(builder -> builder.matchLiteral("{message}").replacement(messageComponent));

        return finalMessage;
    }

    // TODO clear null cases

    /**
     * Builds the hover-card component shown when a player hovers over another
     * player's name in chat.  Includes display name, username, location,
     * equipped title, and gold balance.
     *
     * @param player the player whose info to display
     * @return the assembled multi-line hover component
     */
    private @NotNull Component getPlayerStats(Player player) {
        String nickname = hub.getNicknameDAO().getNickname(player.getUniqueId());
        String shownName = nickname != null ? nickname : player.getName();
        String location = plugin.playerRegions.get(player.getUniqueId());
        Component display = hub.getCosmeticsDAO().getCurrentCosmetic(player, CosmeticType.PREFIX) == null
                ? Utils.parseColorCodes(shownName)
                : Utils.parseColorCodes(hub.getCosmeticsDAO().getCurrentCosmetic(player, CosmeticType.PREFIX) + " " + shownName);

        // Build the player info line
        Component playerInfo = Component.empty()
                .append(display);

        // Build the username line
        Component username = Component.text()
                .append(Component.text("§7"+Parsers.parseUniStatic("Username:"+" ")))
                .append(Component.text(player.getName()))
                .build();

        Component locationComponent = Component.text()
                .append(Component.text("§7"+Parsers.parseUniStatic("Location:"+ " ")))
                .append(Component.text(Utils.parseColorCodeString(RegionHandler.getRegionDisplayName(player))))
                .build();

        // Build the title line
        String titleText = hub.getCosmeticsDAO().getCurrentCosmetic(player, CosmeticType.TITLE);
        Component title = Component.text()
                .append(Component.text("§7"+Parsers.parseUniStatic("Title:")+" "))
                .append(titleText != null ? Component.text("").append(fromString(titleText)) : Component.text(""))
                .build();

        // Build the gold line
        String goldAmount = String.valueOf(hub.getEconomyDAO().getPlayerCurrency(player.getUniqueId(), "gold"));
        Component gold = Component.text()
                .append(Component.text("§7"+Parsers.parseUniStatic("Gold:")+" "))
                .append(Component.text("§e"+goldAmount))
                .build();

        // Combine all components into a single component
        return Component.join(JoinConfiguration.newlines(), playerInfo, username, locationComponent, title, gold);
    }

    private Component fromString(String string) {
        if (string == null || string.isEmpty()) {
            return Component.text("");
        }
        // Use LegacyComponentSerializer to parse legacy color codes, including hex colors
        return LegacyComponentSerializer.legacySection().deserialize(string);
    }

    /**
     * Places the given item into the shared item-preview inventory slot and
     * returns the inventory.  Used to back the clickable {@code [item]} link
     * in chat.
     *
     * @param item the item to display in the preview
     * @return the populated preview inventory
     */
    public Inventory viewItem(ItemStack item) {
        itemPreview.setItem(4, item);
        return itemPreview;
    }

    /** @return the underlying {@link ChannelDAO} for direct DB access */
    public ChannelDAO getChannelDAO() {
        return hub.getChannelDAO();
    }

}
