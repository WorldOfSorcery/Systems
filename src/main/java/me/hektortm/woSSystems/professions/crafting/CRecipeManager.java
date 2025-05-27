package me.hektortm.woSSystems.professions.crafting;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.RecipeDAO;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.utils.dataclasses.RecipeData;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class CRecipeManager {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final CitemManager citemManager = plugin.getCitemManager();
    private final CitemCommand cmd = new CitemCommand(citemManager);
    private final LogManager logManager = plugin.getLogManager();
    private final Map<NamespacedKey, RecipeData> recipeMap = new HashMap<>();
    private final DAOHub hub;


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CRecipeManager(DAOHub hub) {
        this.hub = hub;
    }


    public void loadRecipesFromDatabase() {
        List<RecipeDAO.RecipeRecord> records = hub.getRecipeDAO().getAllRecipes();

        for (RecipeDAO.RecipeRecord record : records) {
            try {
                NamespacedKey key = new NamespacedKey(plugin, record.id);
                ItemStack resultItem = citemManager.getCitemDAO().getCitem(record.output);
                if (resultItem == null) {
                    logManager.sendWarning("Failed to load result item (" + record.output + ") for recipe: \"" + record.id + "\"");
                    continue;
                }

                org.bukkit.inventory.Recipe recipe;

                if (record.type.equalsIgnoreCase("shaped")) {
                    recipe = parseShapedRecipeFromSlots(record.slots, resultItem, key);
                } else if (record.type.equalsIgnoreCase("unshaped")) {
                    recipe = parseUnshapedRecipeFromSlots(record.slots, resultItem, key);
                } else {
                    logManager.sendWarning("Unknown recipe type for ID: " + record.id);
                    continue;
                }

                recipeMap.put(key, new RecipeData(recipe, null, record.success));
                Bukkit.addRecipe(recipe);
            } catch (Exception e) {

            }
        }
    }


    public String getSuccessId(NamespacedKey key) {
        RecipeData recipeData = recipeMap.get(key);
        return recipeData != null ? recipeData.getSuccessId() : null;
    }


    private ShapedRecipe parseShapedRecipeFromSlots(String slots, ItemStack result, NamespacedKey key) {
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("ABC", "DEF", "GHI");

        String[] parts = slots.split(",");
        char[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

        for (int i = 0; i < Math.min(parts.length, 9); i++) {
            String itemId = parts[i];
            if (!itemId.equalsIgnoreCase("null")) {
                ItemStack ingredient = citemManager.getCitemDAO().getCitem(itemId);
                if (ingredient != null) {
                    recipe.setIngredient(chars[i], new RecipeChoice.ExactChoice(ingredient));
                }
            }
        }
        return recipe;
    }

    private ShapelessRecipe parseUnshapedRecipeFromSlots(String slots, ItemStack result, NamespacedKey key) {
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        String[] parts = slots.split(",");
        for (String itemId : parts) {
            if (!itemId.equalsIgnoreCase("null")) {
                ItemStack ingredient = citemManager.getCitemDAO().getCitem(itemId);
                if (ingredient != null) {
                    recipe.addIngredient(new RecipeChoice.ExactChoice(ingredient));
                }
            }
        }
        return recipe;
    }
}
