package me.hektortm.woSSystems.citems.listeners;

import me.hektortm.woSSystems.citems.core.DataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class HoverListener implements Listener {
    private final DataManager data;

    public HoverListener(DataManager data) {
        this.data = data;
    }

    @EventHandler
    public void onItemheld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        data.updateItem(player);
    }
}
