package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BadgeDAO implements IDAO {
    private final Connection conn;
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public BadgeDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
        this.conn = db.getConnection();
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create the titles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS badges (
                    id TEXT PRIMARY KEY NOT NULL,
                    badge TEXT NOT NULL,
                    description TEXT
                )
            """);

            // Create the playerdata_titles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS playerdata_badges (
                    uuid TEXT NOT NULL,
                    badge_id TEXT NOT NULL,
                    equipped BOOLEAN NOT NULL,
                    PRIMARY KEY (uuid, badge_id),
                    FOREIGN KEY (badge_id) REFERENCES badges(id)
                )
            """);
        }
    }

    public void createBadge(String id, String badge) {
        String sql = "INSERT INTO badges (id, badge) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, badge.replace("&", "§"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void giveBadge(String id, Player p) {
        String sql = "INSERT INTO playerdata_badges (uuid, badge_id, equipped) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getUniqueId().toString());
            pstmt.setString(2, id);
            pstmt.setBoolean(3, false); // Default to unequipped
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentBadge(Player p) {
        String uuid = p.getUniqueId().toString();
        String sql = """
            SELECT t.badge
            FROM playerdata_badges pt
            JOIN badges t ON pt.badge_id = t.id
            WHERE pt.uuid = ? AND pt.equipped = 1
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("badge");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCurrentBadgeID(Player p) {
        String uuid = p.getUniqueId().toString();
        String sql = """
        SELECT pt.badge_id
        FROM playerdata_badges pt
        WHERE pt.uuid = ? AND pt.equipped = 1
    """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("badge_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void equipBadge(Player p, String id) {
        String uuid = p.getUniqueId().toString();

        // Unequip all other titles
        String unequipSql = "UPDATE playerdata_badges SET equipped = 0 WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(unequipSql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Equip the selected title
        String equipSql = "UPDATE playerdata_badges SET equipped = 1 WHERE uuid = ? AND badge_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(equipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPlayerBadges(Player p) {
        String uuid = p.getUniqueId().toString();
        List<String> prefixes = new ArrayList<>();
        String sql = "SELECT badge_id FROM playerdata_badges WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prefixes.add(rs.getString("badge_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefixes;
    }

    public void setBadgeDescription(String id, String desc) {
        String sql = "UPDATE badges SET description = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desc.replace("&", "§"));
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getBadgeDescription(String id) {
        String sql = "SELECT description FROM badges WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("description");
        } catch (SQLException e) {
            e.printStackTrace();
            return "§7Default";
        }
    }

    public boolean badgeExists(String id) {
        String sql = "SELECT id FROM badges WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getBadgeText(String id) {
        String sql = "SELECT badge FROM badges WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("badge");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}