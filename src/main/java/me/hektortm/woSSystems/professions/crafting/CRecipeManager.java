package me.hektortm.woSSystems.professions.crafting;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.utils.dataclasses.RecipeData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class CRecipeManager {

    private final CitemManager citemManager;
    private final CitemCommand cmd;
    public final File recipesFolder;
    private final Map<NamespacedKey, RecipeData> recipeMap = new HashMap<>();



    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CRecipeManager(CitemManager citemManager, CitemCommand cmd) {
        this.citemManager = citemManager;
        this.cmd = cmd;
        this.recipesFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "CRecipes");
        if(!recipesFolder.exists()) recipesFolder.mkdirs();
    }


    public void loadRecipes() {
        File[] recipeFiles = recipesFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (recipeFiles == null || recipeFiles.length == 0) {
            Bukkit.getLogger().info("No CRecipes found in " + recipesFolder.getPath());
            return;
        }

        for (File file : recipeFiles) {
            String recipeId = file.getName().replace(".json", ""); // Use file name as recipe ID
            try (FileReader reader = new FileReader(file)) {
                JSONObject json = (JSONObject) new JSONParser().parse(reader);

                // Load recipe type and result
                String type = (String) json.getOrDefault("type", "shaped");
                JSONObject resultJson = (JSONObject) json.get("result");
                boolean craftingBook = (boolean) json.getOrDefault("crafting_book", false);
                JSONObject condition = (JSONObject) json.get("condition");

                ItemStack resultItem = citemManager.loadItemFromFile(new File(cmd.citemsFolder, resultJson.get("id") + ".json"));
                if (resultItem == null) {
                    Bukkit.getLogger().warning("Failed to load result item for recipe: " + recipeId);
                    continue;
                }

                NamespacedKey key = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), recipeId);
                org.bukkit.inventory.Recipe recipe;

                // Handle recipe type
                if (type.equals("shaped")) {
                    recipe = createShapedRecipe(json, resultItem, key, craftingBook);
                } else if (type.equals("unshaped")) {
                    recipe = createUnshapedRecipe(json, resultItem, key, craftingBook);
                } else {
                    Bukkit.getLogger().warning("Unknown recipe type in " + file.getName() + ": " + type);
                    continue;
                }

                // Store recipe and conditions
                recipeMap.put(key, new RecipeData(recipe, condition));

                // Add the recipe to Bukkit
                Bukkit.addRecipe(recipe);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to load recipe from " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private ShapedRecipe createShapedRecipe(JSONObject json, ItemStack resultItem, NamespacedKey key, boolean craftingBook) {
        ShapedRecipe recipe = new ShapedRecipe(key, resultItem);

        // Parse ingredients
        List<List<String>> ingredients = (List<List<String>>) json.get("ingredients");
        recipe.shape("ABC", "DEF", "GHI");
        char[] rows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        int index = 0;

        for (List<String> row : ingredients) {
            for (String itemId : row) {
                if (itemId != null && !itemId.equals("null")) {
                    ItemStack ingredient = citemManager.loadItemFromFile(new File(cmd.citemsFolder, itemId + ".json"));
                    if (ingredient != null) {
                        recipe.setIngredient(rows[index], new RecipeChoice.ExactChoice(ingredient));
                    }
                }
                index++;
            }
        }

        // Add to crafting book if specified
        if (craftingBook) {
            recipe.setGroup(key.getKey());
        }

        return recipe;
    }


    private ShapelessRecipe createUnshapedRecipe(JSONObject json, ItemStack resultItem, NamespacedKey key, boolean craftingBook) {
        ShapelessRecipe recipe = new ShapelessRecipe(key, resultItem);

        // Parse ingredients
        List<String> ingredients = (List<String>) json.get("ingredients");
        for (String itemId : ingredients) {
            if (itemId != null && !itemId.equals("null")) {
                ItemStack ingredient = citemManager.loadItemFromFile(new File(cmd.citemsFolder, itemId + ".json"));
                if (ingredient != null) {
                    recipe.addIngredient(new RecipeChoice.ExactChoice(ingredient));
                }
            }
        }

        // Add to crafting book if specified
        if (craftingBook) {
            recipe.setGroup(key.getKey());
        }

        return recipe;
    }

    public JSONObject getConditions(NamespacedKey key) {
        RecipeData recipeData = recipeMap.get(key);
        return recipeData != null ? recipeData.getConditions() : null;
    }




}
