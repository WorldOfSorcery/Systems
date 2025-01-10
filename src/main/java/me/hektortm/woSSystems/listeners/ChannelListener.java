package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.channels.NicknameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChannelListener implements Listener {
    private final ChannelManager channelManager;
    private final NicknameManager nickManager;

    public ChannelListener(ChannelManager channelManager, NicknameManager nickManager) {
        this.channelManager = channelManager;
        this.nickManager = nickManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the player is focused on a channel
        Channel focusedChannel = channelManager.getFocusedChannel(player);
        if (focusedChannel != null) {
            event.setCancelled(true);
            for (UUID recipients : focusedChannel.getRecipients()) {
                Player p = Bukkit.getPlayer(recipients);

                String formattedMessage = getFormattedMessage(focusedChannel, player, message);
                p.sendMessage(formattedMessage);
            }
        }
    }

    public String getFormattedMessage(Channel channel, Player sender, String message) {
        String format = channel.getFormat();

        String name;
        if (nickManager.getNickname(sender) != null) {
            name = nickManager.getNickname(sender).replace("_", " ");
        } else {
            name = sender.getName();
        }

        return format
                .replace("{player}", name)
                .replace("{message}", message);
    }

}