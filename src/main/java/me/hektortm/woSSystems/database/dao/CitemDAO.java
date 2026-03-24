package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.systems.citems.CitemBuilder;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.woSSystems.utils.model.Citem;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CitemDAO implements IDAO {
    private final DatabaseManager db;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CitemDAO";

    /** In-memory cache — all reads go here after startup preload. */
    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    public CitemDAO(DatabaseManager db) { this.db = db; }

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

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::preloadAll);
    }

    /**
     * Refreshes a single custom item in the cache from the database.  If the row
     * no longer exists the entry is evicted.  Sends a title to {@code p} to confirm.
     *
     * @param id the citem ID to reload
     * @param p  the player who triggered the reload (receives title feedback)
     */
    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT id, data FROM citems WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ItemStack built = CitemBuilder.build(id, rs.getString("data"));
                cache.put(id, built);
                p.sendTitle("§aUpdated Citem", "§e"+id );
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                p.sendTitle("§cDeleted CItem", "§e"+id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:reload", "Failed to reload item from DB: ", e);
        }
    }

    /**
     * Loads all custom item definitions from the {@code citems} table and builds
     * their {@link ItemStack} representations via {@link me.hektortm.woSSystems.systems.citems.CitemBuilder}.
     * Called asynchronously from {@link #initializeTable()}.
     */
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

    /**
     * Checks whether a custom item definition exists in the cache.
     *
     * @param id the citem ID
     * @return {@code true} if the item is cached
     */
    public boolean citemExists(String id) {
        return cache.containsKey(id);
    }

    /**
     * Records a placed item display in {@code placed_citems}.
     *
     * @param id              the citem ID of the displayed item
     * @param ownerUUID       the UUID of the player who placed it
     * @param blockLocation   the block position where the item was placed
     * @param displayLocation the location of the display entity
     * @param isCreative      whether the item was placed while in creative mode
     */
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

    /**
     * Removes a placed item display record, identified by the owner and block location.
     *
     * @param ownerUUID the UUID of the player who placed the item
     * @param location  the block location of the placed item
     */
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

    /**
     * Returns the UUID of the player who placed the item display at the given
     * block location, or {@code null} if no record exists.
     *
     * @param location the block location to look up
     * @return owner UUID, or {@code null}
     */
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

    /**
     * Updates the display entity location for a placed item display.
     *
     * @param oldLocation the current (old) display location
     * @param newLocation the new display location to store
     */
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

    /**
     * Checks whether the item display at the given block location was placed in
     * creative mode.
     *
     * @param location the block location to check
     * @return {@code true} if it was placed in creative mode
     */
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

    /**
     * Returns the display entity location for the item placed at the given block
     * location, or {@code null} if no record exists.
     *
     * @param location the block location
     * @return the display entity location, or {@code null}
     */
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

    /**
     * Returns the citem ID of the item display at the given block location, or
     * {@code null} if not found.
     *
     * @param location the block location
     * @return citem ID, or {@code null}
     */
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


    /**
     * Returns {@code true} if there is a placed item display record at the given
     * block location.
     *
     * @param location the block location to check
     * @return {@code true} if an item display exists there
     */
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

    /**
     * Checks whether the given player is the owner of the item display at the
     * specified block location.
     *
     * @param location the block location to check
     * @param uuid     the player's UUID to verify ownership
     * @return {@code true} if the player owns the item display there
     */
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
