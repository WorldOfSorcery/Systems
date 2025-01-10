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


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage("");
    }


}
