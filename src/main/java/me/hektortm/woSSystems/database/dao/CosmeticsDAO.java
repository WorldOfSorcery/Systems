package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.types.CosmeticType;
import me.hektortm.woSSystems.utils.model.Cosmetic;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * DAO for cosmetic definitions and per-player cosmetic ownership/equip state.
 *
 * <p>Cosmetic definitions are preloaded into an in-memory cache at startup.
 * Player ownership data ({@code player_cosmetics}) is always read directly
 * from the database so that it stays authoritative across sessions.</p>
 *
 * <p>Tables managed: {@code cosmetics} (definitions), {@code player_cosmetics}
 * (per-player ownership and equipped state).</p>
 */
public class CosmeticsDAO implements IDAO {


    private final DatabaseManager db;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CosmeticsDAO";

    private final Map<String, Cosmetic> cache = new ConcurrentHashMap<>();

    public CosmeticsDAO(DatabaseManager db) { this.db = db; }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Cosmetic.class);
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_cosmetics (
                    uuid CHAR(36) NOT NULL,
                    cosmetic_id VARCHAR(255) NOT NULL,
                    cosmetic_type VARCHAR(255) NOT NULL,
                    equipped BOOLEAN NOT NULL,
                    obtained_at VARCHAR(255) NOT NULL,
                    PRIMARY KEY (uuid, cosmetic_id)
                )
            """);
        }

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::preloadAll);
    }

    /**
     * Loads all cosmetic definitions from the {@code cosmetics} table into the
     * in-memory cache.  Called asynchronously from {@link #initializeTable()}.
     */
    public void preloadAll() {
        String sql = "SELECT * FROM cosmetics";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                String type = rs.getString("type");
                String display = rs.getString("display");
                String description = rs.getString("description");
                String permission = rs.getString("permission");
                try {
                    cache.put(id, new Cosmetic(id, type, display, description, permission));
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": failed to preload '" + id + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info(logName + ": preloaded " + count + " cosmetic(s) into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COSD:preload", "Failed to preload cosmetics into cache: ", e);
        }
    }

    /**
     * Refreshes a single cosmetic definition in the cache from the database.
     * If the row no longer exists the entry is evicted.  Sends a title to {@code p}.
     *
     * @param id the cosmetic ID to reload
     * @param p  the player who triggered the reload (receives title feedback)
     */
    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT * FROM cooldowns WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String type = rs.getString("type");
                String display = rs.getString("display");
                String description = rs.getString("description");
                String permission = rs.getString("permission");

                cache.put(id, new Cosmetic(id, type, display, description, permission));
                p.sendTitle("§aUpdated Cosmetic", "§e"+id );
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                p.sendTitle("§cDeleted Cosmetic", "§e"+id );
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COSD:reload", "Failed to reload cosmetic from DB: ", e);
        }
    }

    /**
     * Grants ownership of a cosmetic to a player, recording the current date as
     * the obtained timestamp.  The cosmetic is inserted in the unequipped state.
     *
     * @param type the type of cosmetic
     * @param id   the cosmetic ID
     * @param uuid the player's UUID
     */
    public void giveCosmetic(CosmeticType type, String id, UUID uuid) {
        LocalDate date = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM dd yyyy", Locale.ENGLISH);

        String now = formatter.format(date);

        String sql = "INSERT INTO player_cosmetics (uuid, cosmetic_id, cosmetic_type, equipped, obtained_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.setBoolean(4, false); // Default to unequipped
            pstmt.setString(5, now);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "72dd1794", "Failed to give Cosmetic(type: "+type.name()+") ID("+id+"): ", e
            ));
        }
    }

    /**
     * Removes a cosmetic from a player's ownership, deleting the row from
     * {@code player_cosmetics}.
     *
     * @param type the type of cosmetic
     * @param id   the cosmetic ID
     * @param uuid the player's UUID
     */
    public void takeCosmetic(CosmeticType type, String id, UUID uuid) {
        String sql = "DELETE FROM player_cosmetics WHERE uuid = ? AND cosmetic_id = ? AND cosmetic_type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "1f2c1ce8", "Failed to take Cosmetic(type: "+type.name()+") ID("+id+"): ", e
            ));
        }
    }

    /**
     * Force-equips a cosmetic for a player, inserting an ownership row if needed
     * (bypasses the normal grant workflow).
     *
     * @param type the type of cosmetic
     * @param id   the cosmetic ID
     * @param uuid the player's UUID
     */
    public void setCosmetic(CosmeticType type, String id, UUID uuid) {
        String sql =  "INSERT INTO player_cosmetics (uuid, cosmetic_id, cosmetic_type, equipped) " +
                      "VALUES (?, ?, ?, 1) " +
                      "ON DUPLICATE KEY UPDATE equipped = 1";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "84e13729", "Failed to set Cosmetic(type: "+type.name()+") ID("+id+"): ", e
            ));
        }
    }

    /**
     * Returns the {@code display} value of the player's currently equipped
     * cosmetic of the given type, joining {@code player_cosmetics} with
     * {@code cosmetics}.  Returns {@code null} if nothing is equipped.
     *
     * @param p    the player
     * @param type the cosmetic type to query
     * @return the equipped cosmetic's display string, or {@code null}
     */
    public String getCurrentCosmetic(Player p, CosmeticType type) {
        String uuid = p.getUniqueId().toString();
        String sql = """
            SELECT c.display
            FROM player_cosmetics pc
            JOIN cosmetics c ON pc.cosmetic_id = c.id AND pc.cosmetic_type = c.type
            WHERE pc.uuid = ? AND pc.equipped = 1 AND pc.cosmetic_type = ?
        """;
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("display");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "5ff7bce2", "Failed to get Cosmetic(type: "+type.name()+"): ", e
            ));
        }
        return null;
    }

    /**
     * Returns the ID of the player's currently equipped cosmetic of the given
     * type, or {@code null} if nothing is equipped.
     *
     * @param p    the player
     * @param type the cosmetic type to query
     * @return the equipped cosmetic ID, or {@code null}
     */
    public String getCurrentCosmeticId(Player p, CosmeticType type) {
        String uuid = p.getUniqueId().toString();
        String sql = """
            SELECT pc.cosmetic_id
            FROM player_cosmetics pc
            WHERE pc.uuid = ? AND pc.equipped = 1 AND pc.cosmetic_type = ?
        """;
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("cosmetic_id");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "bcee9aa2", "Failed to get Cosmetic(type: "+type.name()+") ID: ", e
            ));
        }
        return null;
    }

    /**
     * Equips a cosmetic for the player: first unequips all cosmetics of the same
     * type, then marks the specified cosmetic as equipped.
     *
     * @param p    the player
     * @param type the cosmetic type
     * @param id   the cosmetic ID to equip
     */
    public void equipCosmetic(Player p, CosmeticType type, String id) {
        String uuid = p.getUniqueId().toString();

        // Unequip all other cosmetics of the same type
        String unequipSql = "UPDATE player_cosmetics SET equipped = 0 WHERE uuid = ? AND cosmetic_type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(unequipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "cb649023", "Failed to unequip Cosmetic(type: "+type.name()+") ID: ", e
            ));
        }

        // Equip the selected cosmetic
        String equipSql = "UPDATE player_cosmetics SET equipped = 1 WHERE uuid = ? AND cosmetic_id = ? AND cosmetic_type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(equipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "a229d69c", "Failed to equip Cosmetic(type: "+type.name()+") ID: ", e
            ));
        }
    }

    /**
     * Returns the formatted date string for when the player obtained the specified
     * cosmetic, or {@code null} if not found.
     *
     * @param p  the player
     * @param id the cosmetic ID
     * @return obtained-at date string (e.g. {@code "Mon, Jan 01 2024"}), or {@code null}
     */
    public String getPlayerObtainedTime(Player p, String id) {
        String uuid = p.getUniqueId().toString();
        String sql = "SELECT obtained_at FROM player_cosmetics WHERE uuid = ? and cosmetic_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("obtained_at");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "4d4c8392", "Failed to fetch Cosmetic ID("+id+") Obtained time: ", e
            ));
        }
        return null;
    }

    /**
     * Returns a map of cosmetic ID to permission node for all cosmetics of the
     * given type that require a permission.  Used to auto-grant cosmetics to
     * players who have the required permission.
     *
     * @param type the cosmetic type to query
     * @return {@code id → permission} map; empty if none
     */
    public Map<String, String> getPermissionCosmetics(CosmeticType type) {
        String sql = "SELECT id, permission FROM cosmetics WHERE type = ? AND permission IS NOT NULL";
        Map<String, String> cosmetics = new HashMap<>();
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cosmetics.put(rs.getString("id"), rs.getString("permission"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "203458f8", "Failed to fetch Cosmetic for Permission: ", e
            ));
        }
        return cosmetics;
    }

    /**
     * Returns all cosmetic IDs of the given type that the player currently owns.
     *
     * @param p    the player
     * @param type the cosmetic type to query
     * @return list of owned cosmetic IDs; empty if none
     */
    public List<String> getPlayerCosmetics(Player p, CosmeticType type) {
        String uuid = p.getUniqueId().toString();
        List<String> cosmetics = new ArrayList<>();
        String sql = "SELECT cosmetic_id FROM player_cosmetics WHERE uuid = ? AND cosmetic_type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cosmetics.add(rs.getString("cosmetic_id"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "502824a2", "Failed to fetch Player Cosmetic: ", e
            ));
        }
        return cosmetics;
    }

    /**
     * Returns the description text for a cosmetic, or {@code "§7Default"} if not
     * found in the database.
     *
     * @param type the cosmetic type
     * @param id   the cosmetic ID
     * @return description string
     */
    public String getCosmeticDescription(CosmeticType type, String id) {
        String sql = "SELECT description FROM cosmetics WHERE id = ? AND type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("description");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "b8dbd364", "Failed to fetch description for Cosmetic ID("+id+"): ", e
            ));
        }
        return "§7Default";
    }

    /**
     * Checks whether a cosmetic definition exists in the {@code cosmetics} table.
     *
     * @param type the cosmetic type
     * @param id   the cosmetic ID to check
     * @return {@code true} if the cosmetic exists
     */
    public boolean cosmeticExists(CosmeticType type, String id) {
        String sql = "SELECT 1 FROM cosmetics WHERE id = ? AND type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "afcf3b82", "Failed to check if Cosmetic ID("+id+") exists: ", e
            ));
        }
        return false;
    }

    /**
     * Returns the display string for the given cosmetic, or {@code null} if not found.
     *
     * @param type the cosmetic type
     * @param id   the cosmetic ID
     * @return display value, or {@code null}
     */
    public String getCosmeticDisplay(CosmeticType type, String id) {
        String sql = "SELECT display FROM cosmetics WHERE id = ? AND type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("display");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "ee187dde", "Failed to fetch Cosmetic ID("+id+") Display: ", e
            ));
        }
        return null;
    }

    /**
     * Checks whether a player owns a specific cosmetic.
     *
     * @param uuid the player's UUID
     * @param type the cosmetic type
     * @param id   the cosmetic ID
     * @return {@code true} if the player owns the cosmetic
     */
    public boolean hasCosmetic(UUID uuid, CosmeticType type, String id) {
        String sql = "SELECT 1 FROM player_cosmetics WHERE uuid = ? AND cosmetic_id = ? AND cosmetic_type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "a9a1f260", "Failed to check if Player has Cosmetic ID("+id+"): ", e
            ));
            return false;
        }
    }
}