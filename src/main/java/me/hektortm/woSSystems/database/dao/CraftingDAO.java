package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.model.CIngredient;
import me.hektortm.woSSystems.utils.model.CRecipe;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for loading custom crafting recipe definitions ({@link CRecipe}) from the
 * {@code recipes} table.  Recipes are shaped or shapeless and may reference
 * a success interaction to trigger after a successful craft.
 */
public class CraftingDAO implements IDAO {

    private final WoSSystems plugin = WoSSystems.getInstance();
    private final DatabaseManager db;
    private final Map<NamespacedKey, Recipe> loadedRecipes = new HashMap<>();

    public CraftingDAO(DatabaseManager db) { this.db = db; }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection connection = db.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS recipes(" +
                    "id VARCHAR(255) PRIMARY KEY," +
                    "type ENUM('SHAPED', 'SHAPELESS') NOT NULL," +
                    "success_interaction VARCHAR(255) NULL," +
                    "result_citem VARCHAR(255) NOT NULL," +
                    "result_amount TINYINT NOT NULL DEFAULT 1," +
                    "shape VARCHAR(9) NULL," +
                    "ingredients_json JSON NOT NULL," +
                    "successid VARCHAR(255) NULL)");
        } catch (SQLException e) {
            // TODO Discord log
        }
    }

    /**
     * Loads all custom crafting recipes from the {@code recipes} table.
     * Ingredients are deserialized from the stored JSON object.
     *
     * @return list of all {@link CRecipe} definitions; empty if none exist or on error
     */
    public List<CRecipe> loadAllRecipes() {
        List<CRecipe> recipes = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recipes");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                recipes.add(parseRecipe(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recipes;
    }

    private CRecipe parseRecipe(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        boolean shaped = rs.getString("type").equals("SHAPED");
        String shape = rs.getString("shape");
        String json = rs.getString("ingredients_json");

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        Map<Character, CIngredient> ingMap = new HashMap<>();

        for (String key : obj.keySet()) {
            ingMap.put(key.charAt(0), new CIngredient(obj.get(key).getAsString()));
        }

        String resultId = rs.getString("result_citem");
        int resultAmount = rs.getInt("result_amount");

        String successId = rs.getString("successid");

        return new CRecipe(id, shaped, shape, ingMap, resultId, resultAmount,  successId);
    }

    /**
     * Returns the map of recipes that have been registered with Bukkit's recipe
     * system, keyed by their {@link NamespacedKey}.
     *
     * @return mutable map of loaded recipes
     */
    public Map<NamespacedKey, Recipe> getLoadedRecipes() {
        return loadedRecipes;
    }

}