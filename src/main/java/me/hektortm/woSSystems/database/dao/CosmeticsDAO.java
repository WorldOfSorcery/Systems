package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CosmeticsDAO implements IDAO {
    public enum CosmeticType {
        PREFIX, TITLE, BADGE
    }

    private final Connection conn;
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public CosmeticsDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
        this.conn = db.getConnection();
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create the cosmetics table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cosmetics (
                    id VARCHAR(255) NOT NULL,
                    type VARCHAR(255) NOT NULL,
                    display VARCHAR(255) NOT NULL,
                    description VARCHAR(255),
                    PRIMARY KEY (id, type)
                )
            """);

            // Create the player_cosmetics table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_cosmetics (
                    uuid CHAR(36) NOT NULL,
                    cosmetic_id TEXT NOT NULL,
                    cosmetic_type TEXT NOT NULL,
                    equipped BOOLEAN NOT NULL,
                    PRIMARY KEY (uuid, cosmetic_id, cosmetic_type),
                    FOREIGN KEY (cosmetic_id, cosmetic_type) REFERENCES cosmetics(id, type)
                )
            """);
        }
    }

    public void createCosmetic(CosmeticType type, String id, String display) {
        String sql = "INSERT INTO cosmetics (id, type, display) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, type.name());
            pstmt.setString(3, display.replace("&", "§"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void giveCosmetic(CosmeticType type, String id, UUID uuid) {
        String sql = "INSERT INTO player_cosmetics (uuid, cosmetic_id, cosmetic_type, equipped) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.setBoolean(4, false); // Default to unequipped
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("display");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("cosmetic_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void equipCosmetic(Player p, CosmeticType type, String id) {
        String uuid = p.getUniqueId().toString();

        // Unequip all other cosmetics of the same type
        String unequipSql = "UPDATE player_cosmetics SET equipped = 0 WHERE uuid = ? AND cosmetic_type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(unequipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Equip the selected cosmetic
        String equipSql = "UPDATE player_cosmetics SET equipped = 1 WHERE uuid = ? AND cosmetic_id = ? AND cosmetic_type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(equipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPlayerCosmetics(Player p, CosmeticType type) {
        String uuid = p.getUniqueId().toString();
        List<String> cosmetics = new ArrayList<>();
        String sql = "SELECT cosmetic_id FROM player_cosmetics WHERE uuid = ? AND cosmetic_type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cosmetics.add(rs.getString("cosmetic_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cosmetics;
    }

    public void setCosmeticDescription(CosmeticType type, String id, String desc) {
        String sql = "UPDATE cosmetics SET description = ? WHERE id = ? AND type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desc.replace("&", "§"));
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getCosmeticDescription(CosmeticType type, String id) {
        String sql = "SELECT description FROM cosmetics WHERE id = ? AND type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "§7Default";
    }

    public boolean cosmeticExists(CosmeticType type, String id) {
        String sql = "SELECT 1 FROM cosmetics WHERE id = ? AND type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getCosmeticDisplay(CosmeticType type, String id) {
        String sql = "SELECT display FROM cosmetics WHERE id = ? AND type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, type.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("display");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasCosmetic(UUID uuid, CosmeticType type, String id) {
        String sql = "SELECT 1 FROM player_cosmetics WHERE uuid = ? AND cosmetic_id = ? AND cosmetic_type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setString(3, type.name());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}