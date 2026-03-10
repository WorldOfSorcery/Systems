package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.dataclasses.RecipeRecord;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RecipeDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "RecipeDAO";

    public RecipeDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, RecipeRecord.class);
    }

    public void insertRecipe(String id, String type, String slots, String output, String success) {
        String sql = "INSERT OR REPLACE INTO recipes (id, type, slots, output, success) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, type);
            stmt.setString(3, slots);
            stmt.setString(4, output);
            stmt.setString(5, success);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to insert recipe " + id + ": " + e.getMessage());
        }
    }

    public List<RecipeRecord> getAllRecipes() {
        List<RecipeRecord> recipes = new ArrayList<>();
        String sql = "SELECT * FROM recipes";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recipes.add(new RecipeRecord(
                        rs.getString("id"),
                        rs.getString("type"),
                        rs.getString("slots"),
                        rs.getString("output"),
                        rs.getString("success")
                ));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to retrieve recipes: " + e.getMessage());
        }
        return recipes;
    }
}
