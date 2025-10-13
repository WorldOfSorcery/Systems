package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.CosmeticType;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class CosmeticsDAO implements IDAO {


    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CosmeticsDAO";

    public CosmeticsDAO(DatabaseManager db, DAOHub daoHub) throws SQLException {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            // Create the cosmetics table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cosmetics (
                    type VARCHAR(255) NOT NULL,
                    id VARCHAR(255) NOT NULL,
                    display VARCHAR(255) NOT NULL,
                    description VARCHAR(255),
                    permission VARCHAR(255),
                    PRIMARY KEY (id, type)
                )
            """);

            // Create the player_cosmetics table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_cosmetics (
                    uuid CHAR(36) NOT NULL,
                    cosmetic_id VARCHAR(255) NOT NULL,
                    cosmetic_type VARCHAR(255) NOT NULL,
                    equipped BOOLEAN NOT NULL,
                    obtained_at VARCHAR(255) NOT NULL,
                    PRIMARY KEY (uuid, cosmetic_id),
                    FOREIGN KEY (cosmetic_id, cosmetic_type) REFERENCES cosmetics(id, type)
                )
            """);
        }
    }

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