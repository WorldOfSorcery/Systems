package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.FishingItem;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishingDAO implements IDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DatabaseManager db;
    private final DAOHub hub;

    public FishingDAO(DatabaseManager db, DAOHub hub) throws SQLException {
        this.db = db;
        this.hub = hub;
    }


    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS fishing_items(" +
                    "id VARCHAR(255) UNIQUE NOT NULL, " +
                    "item_id VARCHAR(255) NOT NULL, " +
                    "interaction_id VARCHAR(255), " +
                    "rarity VARCHAR(255) NOT NULL, " +
                    "regions VARCHAR(255))");
        }
    }

    // Create a new FishingItem in the database
    public void createFishingItem(FishingItem item) throws SQLException {
        String query = "INSERT INTO fishing_items (id, item_id, interaction_id, rarity, regions) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getCitem());
            stmt.setString(3, item.getInteraction());
            stmt.setString(4, item.getRarity());
            stmt.setString(5, String.join(",", item.getRegions()));
            stmt.executeUpdate();
        }
    }

    // Get a random FishingItem by rarity and region
    public FishingItem getRandomItemByRarityAndRegion(String rarity, String region) throws SQLException {
        String query = "SELECT * FROM fishing_items WHERE rarity = ? AND (regions IS NULL OR regions LIKE ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, rarity);
            stmt.setString(2, "%" + region + "%");
            ResultSet rs = stmt.executeQuery();

            List<FishingItem> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new FishingItem(
                        rs.getString("id"),
                        rs.getString("item_id"),
                        rs.getString("interaction_id"),
                        List.of(rs.getString("regions").split(",")),
                        rs.getString("rarity")
                ));
            }

            if (!items.isEmpty()) {
                return items.get(new Random().nextInt(items.size()));
            }
        }
        return null;
    }

    // Get a random FishingItem by region
    public FishingItem getRandomItemByRegion(String region) throws SQLException {
        String query = "SELECT * FROM fishing_items WHERE regions LIKE ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + region + "%");
            ResultSet rs = stmt.executeQuery();

            List<FishingItem> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new FishingItem(
                        rs.getString("id"),
                        rs.getString("item_id"),
                        rs.getString("interaction_id"),
                        List.of(rs.getString("regions").split(",")),
                        rs.getString("rarity")
                ));
            }

            if (!items.isEmpty()) {
                return items.get(new Random().nextInt(items.size()));
            }
        }
        return null;
    }
}
