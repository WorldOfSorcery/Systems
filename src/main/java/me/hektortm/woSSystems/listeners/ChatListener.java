package me.hektortm.woSSystems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        String nick = "nick system";
        String channelPrefix = "channeldata prefix";
        String prefix = "placeholder";

        String finalName;

        e.setFormat(channelPrefix + " ");

    }


}
