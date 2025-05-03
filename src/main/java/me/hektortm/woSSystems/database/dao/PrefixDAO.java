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

public class PrefixDAO implements IDAO {
    private final Connection conn;
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public PrefixDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
        this.conn = db.getConnection();
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create the titles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prefixes (
                    id VARCHAR(255) PRIMARY KEY NOT NULL,
                    prefix TEXT NOT NULL,
                    description TEXT
                )
            """);

            // Create the playerdata_titles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS playerdata_prefixes (
                    uuid CHAR(36) NOT NULL,
                    prefix_id VARCHAR(255) NOT NULL,
                    equipped BOOLEAN NOT NULL,
                    PRIMARY KEY (uuid, prefix_id),
                    FOREIGN KEY (prefix_id) REFERENCES prefixes(id)
                )
            """);
        }
    }

    public void createPrefix(String id, String prefix) {
        String sql = "INSERT INTO prefixes (id, prefix) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, prefix.replace("&", "§"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void givePrefix(String id, UUID uuid) {
        String sql = "INSERT INTO playerdata_prefixes (uuid, prefix_id, equipped) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setBoolean(3, false); // Default to unequipped
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentPrefix(Player p) {
        String uuid = p.getUniqueId().toString();
        String sql = """
            SELECT t.prefix
            FROM playerdata_prefixes pt
            JOIN prefixes t ON pt.prefix_id = t.id
            WHERE pt.uuid = ? AND pt.equipped = 1
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("prefix");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCurrentPrefixID(Player p) {
        String uuid = p.getUniqueId().toString();
        String sql = """
        SELECT pt.prefix_id
        FROM playerdata_prefixes pt
        WHERE pt.uuid = ? AND pt.equipped = 1
    """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("prefix_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void equipPrefix(Player p, String id) {
        String uuid = p.getUniqueId().toString();

        // Unequip all other titles
        String unequipSql = "UPDATE playerdata_prefixes SET equipped = 0 WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(unequipSql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Equip the selected title
        String equipSql = "UPDATE playerdata_prefixes SET equipped = 1 WHERE uuid = ? AND prefix_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(equipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPlayerPrefixes(Player p) {
        String uuid = p.getUniqueId().toString();
        List<String> prefixes = new ArrayList<>();
        String sql = "SELECT prefix_id FROM playerdata_prefixes WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prefixes.add(rs.getString("prefix_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefixes;
    }

    public void setPrefixDescription(String id, String desc) {
        String sql = "UPDATE prefixes SET description = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desc.replace("&", "§"));
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPrefixDescription(String id) {
        String sql = "SELECT description FROM prefixes WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("description");
        } catch (SQLException e) {
            e.printStackTrace();
            return "§7Default";
        }
    }

    public boolean prefixExists(String id) {
        String sql = "SELECT id FROM prefixes WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPrefixText(String id) {
        String sql = "SELECT prefix FROM prefixes WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("prefix");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasPrefix(UUID uuid, String id) {
        String sql = "SELECT 1 FROM playerdata_prefixes WHERE uuid = ? AND prefix_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}