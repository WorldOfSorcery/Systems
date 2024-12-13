package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.chat.ChatManager;
import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.woSSystems.utils.dataclasses.ChannelData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class ChatListener implements Listener {

    private final ChatManager chatManager;
    private final NicknameManager nickManager;

    public ChatListener(ChatManager chatManager, NicknameManager nickManager) {
        this.chatManager = chatManager;
        this.nickManager = nickManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String focusedChannelName = chatManager.focusedChannel.get(sender);

        // Check if player is focused on a channel
        if (focusedChannelName == null) {
            sender.sendMessage(ChatColor.RED + "You are not focused on any channel.");
            e.setCancelled(true); // Cancel the event to prevent broadcasting
            return;
        }

        ChannelData channel = chatManager.channels.get(focusedChannelName.toLowerCase());
        if (channel == null) {
            sender.sendMessage(ChatColor.RED + "The channel you are focused on does not exist.");
            e.setCancelled(true); // Cancel the event to prevent broadcasting
            return;
        }

        Set<Player> members = chatManager.channelMembers.get(channel.getName().toLowerCase());
        if (!members.contains(sender)) {
            sender.sendMessage(ChatColor.RED + "You are not a member of the channel you are trying to chat in.");
            e.setCancelled(true); // Cancel the event to prevent broadcasting
            return;
        }

        // Modify recipients based on channel membership and range
        e.getRecipients().clear(); // Remove all default recipients
        for (Player recipient : members) {
            if (channel.getRange() > 0) {
                Location senderLocation = sender.getLocation();
                Location recipientLocation = recipient.getLocation();
                if (senderLocation.distance(recipientLocation) > channel.getRange()) {
                    continue; // Skip if out of range
                }
            }
            e.getRecipients().add(recipient); // Add valid recipients
        }

        // Set the format for the message
        String name;
        if (nickManager.getNickname(sender) != null) {
            name = nickManager.getNickname(sender);
        } else {
            name = sender.getName();
        }
        String prefix = ChatColor.translateAlternateColorCodes('&', channel.getPrefix());
        e.setFormat(prefix + " "+name+"§7: %s");
    }



}
