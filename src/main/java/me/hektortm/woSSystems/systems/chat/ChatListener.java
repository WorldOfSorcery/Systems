package me.hektortm.woSSystems.systems.chat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final ChatManager chatManager;
    private final UnlockableManager unlockableManager;

    public ChatListener(ChatManager chatManager, UnlockableManager unlockableManager) {
        this.chatManager = chatManager;
        this.unlockableManager = unlockableManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // Chat is being captured as mail input — leave the event alone.
        if (unlockableManager.getPlayerTempUnlockable(player, "core_mail")) {
            return;
        }

        // Render once for everyone; the line holds a one-off item snapshot and click callbacks.
        Component line = chatManager.render(player, event.message());
        event.renderer(ChatRenderer.viewerUnaware((source, displayName, message) -> line));
    }
}
