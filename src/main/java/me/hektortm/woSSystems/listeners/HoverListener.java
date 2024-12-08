package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class HoverListener implements Listener {
    private final CitemManager data;

    public HoverListener(CitemManager data) {
        this.data = data;
    }

    @EventHandler
    public void onItemheld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        data.updateItem(player);
    }
}