package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.chat.ChatManager;
import me.hektortm.woSSystems.utils.dataclasses.ChannelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JoinListener implements Listener {

    private final ChatManager chatManager;

    public JoinListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String playerKey = playerUUID.toString();

        // Restore joined channels
        List<String> joinedChannels = chatManager.playerDataConfig.getStringList(playerKey + ".joined");
        for (String channelName : joinedChannels) {
            ChannelData channel = chatManager.channels.get(channelName.toLowerCase());
            if (channel != null) {
                Set<Player> members = chatManager.channelMembers.get(channel.getName().toLowerCase());
                if (members != null) {
                    members.add(player);
                }
            }
        }

        // Restore focused channel
        String focusedChannelName = chatManager.playerDataConfig.getString(playerKey + ".focused");
        if (focusedChannelName != null && chatManager.channels.containsKey(focusedChannelName.toLowerCase())) {
            chatManager.focusedChannel.put(player, focusedChannelName);
        }
    }


}
