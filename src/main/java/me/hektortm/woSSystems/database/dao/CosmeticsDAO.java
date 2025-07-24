package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.CosmeticType;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
                    PRIMARY KEY (uuid, cosmetic_id, cosmetic_type),
                    FOREIGN KEY (cosmetic_id, cosmetic_type) REFERENCES cosmetics(id, type)
                )
            """);
        }
    }

    public void giveCosmetic(CosmeticType type, String id, UUID uuid) {
        String sql = "INSERT INTO player_cosmetics (uuid, cosmetic_id, cosmetic_type, equipped) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.setBoolean(4, false); // Default to unequipped
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to give Cosmetic: " + e);
        }
    }

    public void takeCosmetic(CosmeticType type, String id, UUID uuid) {
        String sql = "DELETE FROM player_cosmetics WHERE uuid = ? AND id = ? AND cosmetic_type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to take Cosmetic: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get current Cosmetic: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get current Cosmetic ID: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to unequip Cosmetic: " + e);
        }

        // Equip the selected cosmetic
        String equipSql = "UPDATE player_cosmetics SET equipped = 1 WHERE uuid = ? AND cosmetic_id = ? AND cosmetic_type = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(equipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to equip Cosmetic: " + e);
        }
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Player Cosmetics: " + e);
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
            plugin.writeLog("CosmeticsDAO", Level.SEVERE, "Failed to retrieve description for cosmetic ID: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to check if Cosmetic exists: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Cosmetic Display: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to check if player has cosmetic: " + e);
            return false;
        }
    }
}