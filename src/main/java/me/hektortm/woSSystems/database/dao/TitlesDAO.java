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

public class TitlesDAO implements IDAO {
    private final Connection conn;
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public TitlesDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
        this.conn = db.getConnection();
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create the titles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS titles (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT
                )
            """);

            // Create the playerdata_titles table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS playerdata_titles (
                    uuid TEXT NOT NULL,
                    title_id TEXT NOT NULL,
                    equipped BOOLEAN NOT NULL,
                    PRIMARY KEY (uuid, title_id),
                    FOREIGN KEY (title_id) REFERENCES titles(id)
                )
            """);
        }
    }

    public void createTitle(String id, String title) {
        String sql = "INSERT INTO titles (id, title) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, title.replace("&", "§"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void giveTitle(String id, UUID uuid) {
        String sql = "INSERT INTO playerdata_titles (uuid, title_id, equipped) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, id);
            pstmt.setBoolean(3, false); // Default to unequipped
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentTitle(Player p) {
        String uuid = p.getUniqueId().toString();
        String sql = """
            SELECT t.title
            FROM playerdata_titles pt
            JOIN titles t ON pt.title_id = t.id
            WHERE pt.uuid = ? AND pt.equipped = 1
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCurrentTitleID(Player p) {
        String uuid = p.getUniqueId().toString();
        String sql = """
        SELECT pt.title_id
        FROM playerdata_titles pt
        WHERE pt.uuid = ? AND pt.equipped = 1
    """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("title_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void equipTitle(Player p, String id) {
        String uuid = p.getUniqueId().toString();

        // Unequip all other titles
        String unequipSql = "UPDATE playerdata_titles SET equipped = 0 WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(unequipSql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Equip the selected title
        String equipSql = "UPDATE playerdata_titles SET equipped = 1 WHERE uuid = ? AND title_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(equipSql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPlayerTitles(Player p) {
        String uuid = p.getUniqueId().toString();
        List<String> titles = new ArrayList<>();
        String sql = "SELECT title_id FROM playerdata_titles WHERE uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                titles.add(rs.getString("title_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return titles;
    }

    public void setTitleDescription(String id, String desc) {
        String sql = "UPDATE titles SET description = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desc.replace("&", "§"));
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getTitleDescription(String id) {
        String sql = "SELECT description FROM titles WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("description");
        } catch (SQLException e) {
            e.printStackTrace();
            return "§7Default";
        }
    }

    public boolean titleExists(String id) {
        String sql = "SELECT id FROM titles WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getTitleText(String id) {
        String sql = "SELECT title FROM titles WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasTitle(UUID uuid, String id) {
        String sql = "SELECT 1 FROM playerdata_titles WHERE uuid = ? AND title_id = ?";
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