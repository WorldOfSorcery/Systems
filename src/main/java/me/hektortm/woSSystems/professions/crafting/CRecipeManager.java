package me.hektortm.woSSystems.professions.crafting;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
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
import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class CRecipeManager {

    private final CitemManager citemManager;
    private final CitemCommand cmd;
    public final File recipesFolder;

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

                // Determine recipe type
                String type = (String) json.getOrDefault("type", "shaped");
                JSONObject resultJson = (JSONObject) json.get("result");
                boolean craftingBook = (boolean) json.getOrDefault("crafting_book", false); // Read the parameter


                ItemStack resultItem = citemManager.loadItemFromFile(new File(cmd.citemsFolder, resultJson.get("id") + ".json"));

                if (resultItem != null) {
                    NamespacedKey key = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), recipeId);

                    if (type.equals("shaped")) {
                        handleShapedRecipe(json, resultItem, key, craftingBook);
                    } else if (type.equals("unshaped")) {
                        handleUnshapedRecipe(json, resultItem, key, craftingBook);
                    } else {
                        Bukkit.getLogger().warning("Unknown recipe type in " + file.getName() + ": " + type);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to load recipe from " + file.getName() + ": " + e.getMessage());
            }
        }
    }


    private void handleShapedRecipe(JSONObject json, ItemStack resultItem, NamespacedKey key, boolean craftingBook) {
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

        Bukkit.addRecipe(recipe);
    }


    private void handleUnshapedRecipe(JSONObject json, ItemStack resultItem, NamespacedKey key, boolean craftingBook) {
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

        Bukkit.addRecipe(recipe);
    }




}
