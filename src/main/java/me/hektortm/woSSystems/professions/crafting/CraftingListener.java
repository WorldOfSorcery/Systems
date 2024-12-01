package me.hektortm.woSSystems.professions.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftingListener implements Listener {

    private final JavaPlugin plugin;

    public CraftingListener(JavaPlugin plugin) {
        this.plugin = plugin;
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
        }
    }

    private boolean isCustomRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            if (shapedRecipe.getKey().getNamespace().equals(plugin.getName().toLowerCase())) {
                return true;
            }
        }

        // Check for ShapelessRecipe
        if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            if (shapelessRecipe.getKey().getNamespace().equals(plugin.getName().toLowerCase())) {
                return true;
            }
        }

        // Return false for default recipes
        return false;
    }

}
