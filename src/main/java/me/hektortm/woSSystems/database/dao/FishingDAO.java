package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.model.FishingItem;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * DAO for fishing item definitions stored in the {@code fishing} table.
 *
 * <p>Definitions are preloaded into an in-memory cache at startup.  The main
 * use-case is {@link #getRandomItemByRarityAndRegion(String, String)}, which
 * selects a random eligible item entirely from memory.</p>
 */
public class FishingDAO implements IDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DatabaseManager db;
    private final String logName = "FishingDAO";

    private final Map<String, FishingItem> cache = new ConcurrentHashMap<>();

    public FishingDAO(DatabaseManager db) { this.db = db; }


    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, FishingItem.class);
    }

    /**
     * Refreshes a single fishing item in the cache from the database.  If the row
     * no longer exists the entry is evicted.  Sends a title to {@code p} to confirm.
     *
     * @param id the fishing item ID to reload
     * @param p  the player who triggered the reload (receives title feedback)
     */
    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT citem_id, catch_interaction, rarity, regions, tag FROM fishing WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cache.put(id, new FishingItem(id, rs.getString("citem_id"), rs.getString("catch_interaction"), buildRegions(rs.getString("regions")), rs.getString("rarity")));
                p.sendTitle("§aUpdated Fish", "§e"+id );
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                p.sendTitle("§cDeleted Fish", "§e"+id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:reload", "Failed to reload item from DB: ", e);
        }
    }

    /**
     * Loads all fishing item definitions from the {@code fishing} table into the
     * in-memory cache.  Should be called asynchronously.
     */
    public void preloadAll() {
        String sql = "SELECT id, citem_id, catch_interaction, rarity, regions, tag FROM fishing";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    cache.put(id, new FishingItem(id, rs.getString("citem_id"), rs.getString("catch_interaction"), buildRegions(rs.getString("regions")), rs.getString("rarity")));
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": failed to preload '" + id + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info(logName + ": preloaded " + count + " fish into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "FID:preload", "Failed to preload fish into cache: ", e);
        }
    }

    private List<String> buildRegions(String data) {
        String[] regions = data.split(",");
        return new ArrayList<>(Arrays.asList(regions));

    }

    /**
     * Returns a random {@link FishingItem} matching the given rarity that is
     * eligible in the given region.  An item with no regions configured is
     * eligible everywhere.  Returns {@code null} if no eligible items are found.
     *
     * @param rarity the rarity tier to filter by
     * @param region the region the player is fishing in
     * @return a randomly selected eligible item, or {@code null}
     */
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
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "c64040b8", "Failed to get Fish by Rarity("+rarity+"): ", e
            ));
        }

        return items;
    }


}
