package me.hektortm.woSSystems.professions.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftingListener implements Listener {

    private final JavaPlugin plugin;
    private final CRecipeManager recipeManager;

    public CraftingListener(JavaPlugin plugin, CRecipeManager recipeManager) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        Recipe recipe = inventory.getRecipe();

        if (recipe == null) {
            inventory.setResult(null);
            return;
        }

        if (!isCustomRecipe(recipe)) {
            inventory.setResult(new ItemStack(Material.AIR));
            return;
        }

        // Get the key of the recipe
        NamespacedKey key = null;
        if (recipe instanceof Keyed keyed) {
            key = keyed.getKey();
        }

        if (key == null) {
            inventory.setResult(new ItemStack(Material.AIR));
            return;
        }

        // Retrieve the conditions and success ID
        //JSONArray conditions = recipeManager.getConditions(key);
       // String successId = recipeManager.getSuccessId(key);  // Add this method to get success ID

        // Validate the conditions using ConditionHandler
        if (!event.getViewers().isEmpty() && event.getViewers().get(0) instanceof Player player) {
            //
        }
    }
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        // Ensure we're dealing with the right player and recipe
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Get the recipe used for crafting
        Recipe recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }

        // Get the key of the recipe
        NamespacedKey key = null;
        if (recipe instanceof Keyed) {
            key = ((Keyed) recipe).getKey();
        }

        if (key == null) {
            return;
        }

        // Retrieve the conditions and success ID
       // JSONArray conditions = recipeManager.getConditions(key);
       // String successId = recipeManager.getSuccessId(key);

        // Check if the player is shift-clicking
        if (event.isShiftClick()) {
            // Shift-click detected
            //

            // Allow bulk crafting if conditions are met
//            if (successId != null) {
//                triggerSuccessInteraction(player, successId);
//            }
//            return;
        }

        // For regular crafting
        //

        // If conditions are valid, trigger the success interaction if available
//        if (successId != null) {
//            triggerSuccessInteraction(player, successId);
//        }
    }

    // Helper method to trigger success interaction
    private void triggerSuccessInteraction(Player player, String successId) {
        // Retrieve the interaction configuration for the successId
        //InteractionData interaction = interactionManager.getInteractionByID(successId);
        //if (interaction != null) {
            // Trigger the interaction
            //interactionManager.triggerInteraction(player, successId);
        //}
    }


    private boolean isCustomRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            if (shapedRecipe.getKey().getNamespace().equals(plugin.getName().toLowerCase())) {
                return true;
            }
        }

        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            return shapelessRecipe.getKey().getNamespace().equals(plugin.getName().toLowerCase());
        }

        return false;
    }
}
