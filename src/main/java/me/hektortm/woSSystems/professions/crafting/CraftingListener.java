package me.hektortm.woSSystems.professions.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

public class CraftingListener implements Listener {

    private final JavaPlugin plugin;
    private final CRecipeManager recipeManager;
    private final ConditionHandler conditionHandler;

    public CraftingListener(JavaPlugin plugin, CRecipeManager recipeManager, ConditionHandler conditionHandler) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
        this.conditionHandler = conditionHandler;
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

        // Retrieve the conditions
        JSONObject conditions = recipeManager.getConditions(key);

        // Validate the conditions using ConditionHandler
        if (!event.getViewers().isEmpty() && event.getViewers().get(0) instanceof Player player) {
            if (!conditionHandler.validateConditions(player, conditions)) {
                inventory.setResult(new ItemStack(Material.AIR));
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
