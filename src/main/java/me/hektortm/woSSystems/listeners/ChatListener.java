package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.chat.ChatManager;
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

    public ChatListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String focusedChannelName = chatManager.focusedChannel.get(p);
        if (focusedChannelName == null) {
            p.sendMessage(ChatColor.RED + "You are not focused on any channel.");
            return;
        }

        ChannelData channel = chatManager.channels.get(focusedChannelName.toLowerCase());
        if (channel == null) {
            p.sendMessage(ChatColor.RED + "The channel you are focused on does not exist.");
            return;
        }

        Set<Player> members = chatManager.channelMembers.get(channel.getName().toLowerCase());
        for (Player recipient : members) {
            if (channel.getRange() > 0) {
                Location senderLocation = p.getLocation();
                Location recipientLocation = recipient.getLocation();
                if (senderLocation.distance(recipientLocation) > channel.getRange()) {
                    continue; // Skip if out of range
                }
            }

            String prefix = channel.getPrefix();
            String name = p.getName();

            e.setFormat(prefix + " " + name +": "+ e.getMessage());
        }
    }


}
