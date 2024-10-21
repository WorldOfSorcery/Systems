package me.hektortm.woSSystems.systems.interactions.actions;

import me.hektortm.woSSystems.systems.interactions.core.ActionHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class InventoryInteraction {

    private Plugin plugin;
    private ActionHandler actionHandler;

    public InventoryInteraction(Plugin plugin, ActionHandler actionHandler) {
        this.plugin = plugin;
        this.actionHandler = actionHandler;
    }

    // Handle interaction in the custom inventory
    public void handleInventoryClick(InventoryClickEvent event, boolean clickable, String leftClickAction, String rightClickAction, String shiftClickAction) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();  // Custom GUI inventory

        // Ensure the click is in the custom GUI, not the player's inventory
        if (clickedInventory == null || !clickedInventory.equals(topInventory)) {
            return; // Exit if the click was not in the custom GUI
        }

        // Ensure the clicked slot is within the custom GUI (avoid clicks outside of inventory bounds)
        if (event.getSlot() < 0 || event.getSlot() >= topInventory.getSize()) {
            return;
        }

        if (event.isCancelled()) {
            return;  // If event is already canceled, do nothing
        }

        // Determine the action to perform based on the click type
        String actionToPerform = null;
        switch (event.getClick()) {
            case LEFT:
                actionToPerform = leftClickAction;
                break;
            case RIGHT:
                actionToPerform = rightClickAction;
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                actionToPerform = shiftClickAction;
                break;
            default:
                return; // Ignore other click types (e.g., MIDDLE, NUMBER_KEY)
        }

        // Trigger action if it's not null
        if (actionToPerform != null) {
            actionHandler.triggerCommand(actionToPerform, player);
        }

        // Cancel the event only after processing the action if the item is not clickable
        if (!clickable) {
            event.setCancelled(true);
        }
    }

}
