package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.logging.Level;

public class ChannelListener implements Listener {
    private final ChannelManager channelManager;
    private final NicknameManager nickManager;
    private final UnlockableManager unlockableManager;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public ChannelListener(ChannelManager channelManager, NicknameManager nickManager, UnlockableManager unlockableManager) {
        this.channelManager = channelManager;
        this.nickManager = nickManager;
        this.unlockableManager = unlockableManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the player has a temporary unlockable that prevents chat
        if (unlockableManager.getPlayerTempUnlockable(player, "core_mail")) {
            return;
        }

        // Check if the player is focused on a channel
        Channel focusedChannel = channelManager.getFocusedChannel(player);
        if (focusedChannel != null) {
            event.setCancelled(true); // Cancel the default chat event

            // Get the sender's location
            Location senderLocation = player.getLocation();

            // Send the message to recipients based on the channel's radius
            for (UUID recipientUUID : channelManager.getChannelDAO().getRecipients(focusedChannel.getName())) {
                Player recipient = Bukkit.getPlayer(recipientUUID);
                if (recipient != null) {
                    // Check if the recipient is within the radius (if radius is enabled)
                    if (focusedChannel.getRadius() == -1 || isWithinRadius(senderLocation, recipient.getLocation(), focusedChannel.getRadius())) {
                        String formattedMessage = getFormattedMessage(focusedChannel, player, message);
                        recipient.sendMessage(formattedMessage);
                    }
                }
            }
        }
    }

    /**
     * Checks if a recipient is within the specified radius of the sender.
     *
     * @param senderLocation The location of the sender.
     * @param recipientLocation The location of the recipient.
     * @param radius The radius to check (in blocks).
     * @return True if the recipient is within the radius, false otherwise.
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
     * Formats the chat message based on the channel's format.
     *
     * @param channel The channel being used.
     * @param sender The player sending the message.
     * @param message The message content.
     * @return The formatted message.
     */
    public String getFormattedMessage(Channel channel, Player sender, String message) {
        String format = channel.getFormat();

        // Use the player's nickname if available, otherwise use their username
        String name = nickManager.getNickname(sender) != null ?
                nickManager.getNickname(sender).replace("_", " ") :
                sender.getName();

        // Replace placeholders in the format
        return format
                .replace("{player}", name)
                .replace("{message}", message);
    }
}