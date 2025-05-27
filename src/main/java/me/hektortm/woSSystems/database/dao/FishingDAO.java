package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.FishingItem;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class FishingDAO implements IDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DatabaseManager db;
    private final DAOHub hub;
    private final String logName = "FishingDAO";

    public FishingDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }


    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS fishing(" +
                    "id VARCHAR(255) UNIQUE NOT NULL, " +
                    "citem_id VARCHAR(255) NOT NULL, " +
                    "catch_interaction VARCHAR(255), " +
                    "rarity VARCHAR(255) NOT NULL, " +
                    "regions TEXT)");
        }
    }

    public FishingItem getRandomItemByRarityAndRegion(String rarity, String region) {
        List<FishingItem> itemsByRarity = getItemsByRarity(rarity);
        List<FishingItem> eligibleItems = new ArrayList<>();

        for (FishingItem item : itemsByRarity) {
            List<String> regions = item.getRegions();
            if (regions.isEmpty() || regions.contains(region)) {
                eligibleItems.add(item);
            }
        }

        if (eligibleItems.isEmpty()) {
            return null;
        }

        Random random = new Random();
        return eligibleItems.get(random.nextInt(eligibleItems.size()));
    }

    private List<FishingItem> getItemsByRarity(String rarity) {
        List<FishingItem> items = new ArrayList<>();

        String query = "SELECT id, citem_id, catch_interaction, regions, rarity FROM fishing WHERE rarity = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, rarity);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id");
                String citemId = rs.getString("citem_id");
                String interaction = rs.getString("catch_interaction");
                String regionsStr = rs.getString("regions");
                List<String> regions = new ArrayList<>();

                if (regionsStr != null && !regionsStr.trim().isEmpty()) {
                    regions = Arrays.asList(regionsStr.replace("\"", "").split(","));
                }

                FishingItem item = new FishingItem(id, citemId, interaction, regions, rs.getString("rarity"));
                items.add(item);
            }

        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Item By Rarity: "+e);
        }

        return items;
    }


}
