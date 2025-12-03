package me.hektortm.woSSystems.professions.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.CitemDAO;
import me.hektortm.woSSystems.database.dao.CraftingDAO;
import me.hektortm.woSSystems.utils.dataclasses.CIngredient;
import me.hektortm.woSSystems.utils.dataclasses.CRecipe;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingListener implements Listener {

    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getInstance();
    private List<CRecipe> recipes;

    public CraftingListener(DAOHub hub) {
        this.hub = hub;
        this.recipes = hub.getCraftingDAO().loadAllRecipes();
    }

    private boolean isExactCItem(ItemStack provided, String citemId) {
        if (provided == null) return false;

        ItemStack required = hub.getCitemDAO().getCitem(citemId);
        if (required == null) return false;

        return provided.isSimilar(required);
    }

    private boolean matchesShapedRecipe(CraftingInventory inv, CRecipe recipe) {

        String shape = recipe.shape(); // 9 characters

        for (int slot = 0; slot < 9; slot++) {
            char key = shape.charAt(slot);

            ItemStack provided = inv.getMatrix()[slot];

            if (key == ' ' || !recipe.ingredients().containsKey(key)) {
                // Should be empty
                if (provided != null) return false;
                continue;
            }

            // Must match a CItem ingredient
            CIngredient ing = recipe.ingredients().get(key);
            if (!isExactCItem(provided, ing.citemId())) return false;
        }

        return true;
    }

    // -------------------------------------------------------------
    // Helper: Check if inventory matches a shapeless recipe
    // -------------------------------------------------------------
    private boolean matchesShapelessRecipe(CraftingInventory inv, CRecipe recipe) {

        List<ItemStack> provided = new ArrayList<>();
        for (ItemStack stack : inv.getMatrix()) {
            if (stack != null) provided.add(stack);
        }

        List<String> required = recipe.ingredients().values()
                .stream().map(CIngredient::citemId).collect(Collectors.toList());

        // must have equal count
        if (provided.size() != required.size()) return false;

        // compare ingredients ignoring order
        for (String neededId : required) {

            boolean found = false;

            for (ItemStack p : provided) {
                if (isExactCItem(p, neededId)) {
                    found = true;
                    provided.remove(p);
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    private CRecipe findMatchingRecipe(CraftingInventory inv) {
        for (CRecipe recipe : recipes) {
            if (recipe.shaped() && matchesShapedRecipe(inv, recipe))
                return recipe;

            if (!recipe.shaped() && matchesShapelessRecipe(inv, recipe))
                return recipe;
        }
        return null;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {

        CraftingInventory inv = event.getInventory();
        Player p = (Player) event.getView().getPlayer();
        CRecipe match = findMatchingRecipe(inv);

        if (match != null) {
            if (plugin.getCraftingManager().hasUnlockedRecipe(match.id(), p)) {
                inv.setResult(hub.getCitemDAO().getCitem(match.resultCItemId()));
            }
            else {
                inv.setResult(null);
            }
        } else {
            inv.setResult(null); // NOT a valid CRecipe
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {

        CraftingInventory inv = event.getInventory();
        CRecipe match = findMatchingRecipe(inv);
        Player p = (Player) event.getView().getPlayer();

        if (match == null) {
            event.setCancelled(true);
            return;
        }
       if (match.successId() != null) plugin.getInteractionManager().triggerInteraction(match.successId(), p, null);
    }
}