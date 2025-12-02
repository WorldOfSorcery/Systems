package me.hektortm.woSSystems.professions.crafting;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.CRecipe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

public class CraftingManager {

    private final WoSSystems plugin = WoSSystems.getInstance();
    private final DAOHub hub;

    private final Map<String, NamespacedKey> registeredKeys = new HashMap<>();

    public CraftingManager(DAOHub hub) {
        this.hub = hub;
    }

    public void loadAll() {
        List<CRecipe> recipes = hub.getCraftingDAO().loadAllRecipes();
        for (CRecipe r : recipes) {
            registerRecipe(r);
        }
    }

    private void registerRecipe(CRecipe r) {

        NamespacedKey key = new NamespacedKey(plugin, r.id());
        registeredKeys.put(r.id(), key);

        // ----- Build result -----
        ItemStack result = hub.getCitemDAO().getCitem(r.resultCItemId()).clone();
        result.setAmount(r.resultAmount());

        if (r.shaped()) {
            registerShaped(r, key, result);
        } else {
            registerShapeless(r, key, result);
        }
    }

    public boolean hasUnlockedRecipe(String id, Player p) {
        Set<NamespacedKey> playerRecipes = p.getDiscoveredRecipes();
        List<CRecipe> recipes = hub.getCraftingDAO().loadAllRecipes();

        for (CRecipe r : recipes) {
            if (r.id().equals(id)) {
                NamespacedKey key = new NamespacedKey(plugin, r.id());
                if (playerRecipes.contains(key)) return true;
                else return false;
            }
        }
        return false;
    }

    private void registerShaped(CRecipe r, NamespacedKey key, ItemStack result) {
        ShapedRecipe shaped = new ShapedRecipe(key, result);

        shaped.shape(
                r.shape().substring(0, 3),
                r.shape().substring(3, 6),
                r.shape().substring(6, 9)
        );

        for (var entry : r.ingredients().entrySet()) {
            char symbol = entry.getKey();
            ItemStack ing = hub.getCitemDAO().getCitem(entry.getValue().citemId()).clone();
            shaped.setIngredient(symbol, new RecipeChoice.ExactChoice(ing));
        }

        Bukkit.addRecipe(shaped);
    }

    private void registerShapeless(CRecipe r, NamespacedKey key, ItemStack result) {
        ShapelessRecipe shapeless = new ShapelessRecipe(key, result);

        for (var entry : r.ingredients().entrySet()) {
            ItemStack ing = hub.getCitemDAO().getCitem(entry.getValue().citemId()).clone();
            shapeless.addIngredient(new RecipeChoice.ExactChoice(ing));
        }

        Bukkit.addRecipe(shapeless);
    }

    public Collection<NamespacedKey> getKeys() {
        return registeredKeys.values();
    }
}