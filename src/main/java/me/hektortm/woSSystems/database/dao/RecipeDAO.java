package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
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
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS recipes (" +
                    "id VARCHAR(255) PRIMARY KEY," +
                    "type VARCHAR(255)," +
                    "slots TEXT," +
                    "output VARCHAR(255)," +
                    "success VARCHAR(255))");
        }
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

    public static class RecipeRecord {
        public final String id;
        public final String type;
        public final String slots;
        public final String output;
        public final String success;

        public RecipeRecord(String id, String type, String slots, String output, String success) {
            this.id = id;
            this.type = type;
            this.slots = slots;
            this.output = output;
            this.success = success;
        }
    }
}
