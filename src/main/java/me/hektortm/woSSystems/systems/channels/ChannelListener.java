package me.hektortm.woSSystems.systems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChannelListener implements Listener {
    private final ChannelManager channelManager;
    private final NicknameManager nickManager;
    private final UnlockableManager unlockableManager;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public ChannelListener(ChannelManager channelManager, NicknameManager nickManager, UnlockableManager unlockableManager, DAOHub hub) {
        this.channelManager = channelManager;
        this.nickManager = nickManager;
        this.unlockableManager = unlockableManager;
        this.hub = hub;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the player has a temporary unlockable that prevents chat
        if (unlockableManager.getPlayerTempUnlockable(player, "core_mail")) {
            return;
        }
        channelManager.sendMessage(event, player, message);
    }
}