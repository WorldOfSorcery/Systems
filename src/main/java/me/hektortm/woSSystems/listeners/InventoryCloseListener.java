package me.hektortm.woSSystems.listeners;


import me.hektortm.woSSystems.systems.guis.GUIManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;

public class InventoryCloseListener implements Listener {

    private final GUIManager guiManager;

    public InventoryCloseListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        guiManager.openGUIs.remove(player.getUniqueId());
    }
}
