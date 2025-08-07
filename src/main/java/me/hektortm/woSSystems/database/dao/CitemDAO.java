package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CitemDAO implements IDAO {
    private final me.hektortm.wosCore.database.DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CitemDAO";

    public CitemDAO(me.hektortm.wosCore.database.DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                            CREATE TABLE IF NOT EXISTS items (
                            id VARCHAR(40) NOT NULL,
                            item_data TEXT NOT NULL,
                            PRIMARY KEY (id)
                            )
                    """);
            stmt.execute("CREATE TABLE IF NOT EXISTS placed_citems("+
                    "citem_id VARCHAR(255) NOT NULL, " +
                    "owner_uuid CHAR(36) NOT NULL, " +
                    "block_location VARCHAR(255) NOT NULL, " +
                    "display_location VARCHAR(255) NOT NULL," +
                    "PRIMARY KEY (citem_id))");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize CitemDAO table: " + e.getMessage());
            throw e;
        } finally {
            plugin.getLogger().info(logName + ": CitemDAO table initialized successfully.");
        }
    }

    public List<String> getCitemIds() {
        String sql = "SELECT id FROM items";
        List<String> citemIds = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                citemIds.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Citem IDs: " + e);
        }
        return citemIds;
    }

    public void saveCitem(String id, ItemStack item) {
        String itemData = itemStackToBase64(item);
        String sql = "INSERT INTO items (id, item_data) VALUES (?, ?)";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, itemData);
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save Citem: " + e.getMessage());
        }
    }

    public void updateCitem(String id, ItemStack item) {
        String itemData = itemStackToBase64(item);
        String sql = "UPDATE items SET item_data = ? WHERE id = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemData);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update Citem: " + e.getMessage());
        }
    }

    public ItemStack getCitem(String id) {
        String sql = "SELECT item_data FROM items WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);

            ResultSet rs = stmt.executeQuery();

            ItemStack item = null;
            if (rs.next()) {
                String base64 = rs.getString("item_data");
                item = itemStackFromBase64(base64);
            }

            rs.close();
            stmt.close();
            conn.close();
            return item;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve Citem: " + e.getMessage());
            return null;
        }
    }

    public boolean citemExists(String id) {
        try (Connection conn = db.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement("SELECT 1 FROM items WHERE id = ?")) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to check existing Citem: " + e);
            return false;
        }
    }

    public static String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public ItemStack itemStackFromBase64(String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            ObjectInputStream inputStream = new BukkitObjectInputStream(new ByteArrayInputStream(data));

            ItemStack item = (ItemStack) inputStream.readObject();
            inputStream.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize ItemStack from Base64: " + e.getMessage(), e);
        }
    }


    public void createItemDisplay(String id, UUID ownerUUID, Location blockLocation, Location displayLocation) {
        String bLoc = Parsers.locationToString(blockLocation);
        String dLoc = Parsers.locationToString(displayLocation);
        String sql = "INSERT INTO placed_citems (citem_id, owner_uuid, block_location, display_location) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, ownerUUID.toString());
            pstmt.setString(3, bLoc);
            pstmt.setString(4, dLoc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to create Item Display: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to remove Item Display: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Owner UUID: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to change Item Display: " + e);
        }
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Item Display Location: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Item Display ID: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to check Item Display: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to check Item Display Owner: " + e);
        }
        return false;
    }

}
