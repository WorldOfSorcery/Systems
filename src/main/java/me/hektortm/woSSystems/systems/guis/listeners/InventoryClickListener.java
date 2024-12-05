package me.hektortm.woSSystems.systems.guis.listeners;


import me.hektortm.woSSystems.systems.interactions.actions.InventoryInteraction;
import me.hektortm.woSSystems.systems.interactions.config.InteractionConfig;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final InventoryInteraction inventoryInteraction;
    private final InteractionConfig config;
    private final GUIManager guiManager;

    public InventoryClickListener(InventoryInteraction inventoryInteraction, InteractionConfig config, GUIManager guiManager) {
        this.inventoryInteraction = inventoryInteraction;
        this.config = config;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Check if the player is interacting with a custom GUI
        if (!guiManager.hasCustomGUIOpen(player)) {
            return; // If no custom GUI is open for this player, ignore the event
        }

        // Get the configuration for the open GUI
        InteractionConfig config = guiManager.getOpenGUIConfig(player);

        int slot = event.getSlot();
        boolean clickable = config.isSlotClickable(slot);

        // Get actions based on click type
        String leftClickAction = config.getSlotAction(slot, "left-click");
        String rightClickAction = config.getSlotAction(slot, "right-click");
        String shiftClickAction = config.getSlotAction(slot, "shift-click");

        // Handle the click in the custom inventory
        inventoryInteraction.handleInventoryClick(event, clickable, leftClickAction, rightClickAction, shiftClickAction);
        return;
    }
}
