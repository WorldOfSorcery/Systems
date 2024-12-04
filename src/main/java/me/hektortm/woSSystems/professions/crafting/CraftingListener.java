package me.hektortm.woSSystems.professions.crafting;

import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.interactions.config.InteractionConfig;
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
import org.json.simple.JSONObject;

public class CraftingListener implements Listener {

    private final JavaPlugin plugin;
    private final CRecipeManager recipeManager;
    private final ConditionHandler conditionHandler;
    private final InteractionManager interactionManager;

    public CraftingListener(JavaPlugin plugin, CRecipeManager recipeManager, ConditionHandler conditionHandler, InteractionManager interactionManager) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
        this.conditionHandler = conditionHandler;
        this.interactionManager = interactionManager;
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
        JSONObject conditions = recipeManager.getConditions(key);
        String successId = recipeManager.getSuccessId(key);  // Add this method to get success ID

        // Validate the conditions using ConditionHandler
        if (!event.getViewers().isEmpty() && event.getViewers().get(0) instanceof Player player) {
            if (!conditionHandler.validateConditions(player, conditions)) {
                inventory.setResult(new ItemStack(Material.AIR));
            }
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
        JSONObject conditions = recipeManager.getConditions(key);
        String successId = recipeManager.getSuccessId(key);

        // Validate the conditions using ConditionHandler
        if (!conditionHandler.validateConditions(player, conditions)) {
            event.setCancelled(true);  // Cancel the crafting event if conditions are not met
            return;
        }

        // If conditions are valid, trigger the success interaction if available
        if (successId != null) {
            // Retrieve the interaction configuration for the successId
            InteractionConfig interactionConfig = interactionManager.getInteractionConfig(successId);
            if (interactionConfig != null) {
                // Trigger the interaction
                interactionManager.triggerInteraction(interactionConfig, player);
            }
        }
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
