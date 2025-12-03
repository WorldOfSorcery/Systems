package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.CIngredient;
import me.hektortm.woSSystems.utils.dataclasses.CRecipe;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingDAO implements IDAO {

    private final WoSSystems plugin = WoSSystems.getInstance();
    private final DatabaseManager db;
    private final DAOHub hub;
    private final Map<NamespacedKey, Recipe> loadedRecipes = new HashMap<>();

    public CraftingDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

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

    public Map<NamespacedKey, Recipe> getLoadedRecipes() {
        return loadedRecipes;
    }

}