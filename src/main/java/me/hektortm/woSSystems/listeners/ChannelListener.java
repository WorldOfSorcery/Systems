package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.Icons;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Level;

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