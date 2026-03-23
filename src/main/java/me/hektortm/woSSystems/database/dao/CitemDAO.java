package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.systems.citems.CitemBuilder;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.woSSystems.utils.dataclasses.Citem;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CitemDAO implements IDAO {
    private final me.hektortm.wosCore.database.DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CitemDAO";

    /** In-memory cache — all reads go here after startup preload. */
    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    public CitemDAO(me.hektortm.wosCore.database.DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Citem.class);

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS placed_citems("+
                    "citem_id VARCHAR(255) NOT NULL, " +
                    "owner_uuid CHAR(36) NOT NULL, " +
                    "block_location VARCHAR(255) NOT NULL, " +
                    "display_location VARCHAR(255) NOT NULL," +
                    "creative_placed BOOLEAN NOT NULL DEFAULT FALSE," +
                    "PRIMARY KEY (citem_id))");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:adc901cc", "Failed to initialize CitemDAO table: ", e);
        } finally {
            plugin.getLogger().info(logName + ": CitemDAO table initialized successfully.");
        }

        // Preload all items into memory so GUI opens never touch the database.
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::preloadAll);
    }

    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT id, data FROM citems WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ItemStack built = CitemBuilder.build(id, rs.getString("data"));
                cache.put(id, built);
                plugin.getLogger().info(logName + ": reloaded '" + id + "' from DB.");
                p.sendMessage(plugin.getLangManager().getMessage("general", "prefix") + "§aUpdated Constant: §e"+id);
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                plugin.getLogger().info(logName + ": evicted '" + id + "' (not found in DB).");
                p.sendMessage(plugin.getLangManager().getMessage("general", "prefix") + "§cDeleted Constant: §e"+id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:reload", "Failed to reload item from DB: ", e);
        }
    }

    public void preloadAll() {
        String sql = "SELECT id, data FROM citems";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    cache.put(id, CitemBuilder.build(id, rs.getString("data")));
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": failed to preload '" + id + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info(logName + ": preloaded " + count + " item(s) into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:preload", "Failed to preload items into cache: ", e);
        }
    }

    /**
     * Returns a clone of the cached item. Never touches the database after startup.
     * Returns null if the item doesn't exist.
     */
    public ItemStack getCitem(String id) {
        ItemStack cached = cache.get(id);
        return cached != null ? cached.clone() : null;
    }

    public boolean citemExists(String id) {
        return cache.containsKey(id);
    }

    public void createItemDisplay(String id, UUID ownerUUID, Location blockLocation, Location displayLocation, boolean isCreative) {
        String bLoc = Parsers.locationToString(blockLocation);
        String dLoc = Parsers.locationToString(displayLocation);
        String sql = "INSERT INTO placed_citems (citem_id, owner_uuid, block_location, display_location, creative_placed) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, ownerUUID.toString());
            pstmt.setString(3, bLoc);
            pstmt.setString(4, dLoc);
            pstmt.setBoolean(5, isCreative);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "9e2bb566", "Failed to create Item Display", e);
        }
    }

    public void removeItemDisplay(UUID ownerUUID, Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "DELETE FROM placed_citems WHERE owner_uuid = ? AND block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ownerUUID.toString());
            pstmt.setString(2, loc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "4e63206a", "Failed to remove Item Display", e);
        }
    }

    public UUID getUUID(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT owner_uuid FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) { // Check if result exists
                return UUID.fromString(rs.getString("owner_uuid"));
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "b1ebfe0d", "Failed to get Owner UUID", e);
        }
        return null; // Return null if no result
    }

    public void changeDisplay(Location oldLocation, Location newLocation) {
        String oldLoc = Parsers.locationToString(oldLocation);
        String newLoc = Parsers.locationToString(newLocation);
        String sql = "UPDATE placed_citems SET display_location = ? WHERE display_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newLoc);
            pstmt.setString(2, oldLoc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "621ebf18", "Failed to change Item Display", e);
        }
    }

    public boolean isCreativePlaced(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT * FROM placed_citems WHERE block_location = ? AND creative_placed = true";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("creative_placed");
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "d3041663", "Failed to check if Creative Placed", e);
            return false;
        }
        return false;
    }

    public Location getDisplayLocation(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT display_location FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Parsers.stringToLocation(rs.getString("display_location"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "cbf86e39", "Failed to get Item Display Location", e);
            return null;
        }
    }

    public String getItemDisplayID(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT citem_id FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) { // Check if result exists
                return rs.getString("citem_id");
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "9b378252", "Failed to get Item Display ID", e);
        }
        return null; // Return null if not found
    }


    public boolean isItemDisplay(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT  1 FROM placed_citems WHERE block_location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "593f879f", "Failed to check Item Display", e);
            return false;
        }
    }

    public boolean isItemDisplayOwner(Location location, UUID uuid) {
        String loc = Parsers.locationToString(location); // Fix incorrect location format
        String sql = "SELECT 1 FROM placed_citems WHERE block_location = ? AND owner_uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            pstmt.setString(2, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // If there's a result, return true
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "f2863bfe", "Failed to check Item Display Owner", e);
        }
        return false;
    }
}
