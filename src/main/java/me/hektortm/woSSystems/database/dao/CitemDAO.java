package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
                            web_data JSON NOT NULL,
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
            WoSSystems.discordLog(Level.SEVERE, "CID:adc901cc", "Failed to intiialize CitemDAO table: ", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:7e0bb0a0", "Failed to get Citems IDs: ", e);
        }
        return citemIds;
    }

    public void saveCitem(String id, ItemStack item) {
        String itemData = itemStackToBase64(item);
        JsonObject webData = itemStackToJson(item);
        String sql = "INSERT INTO items (id, item_data, web_data) VALUES (?, ?, ?)";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, itemData);
            stmt.setString(3, webData.toString());
            stmt.execute();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:e70ccfb9", "Failed to save Citem: ", e);
        }
    }

    public void updateCitem(String id, ItemStack item) {
        String itemData = itemStackToBase64(item);
        JsonObject webData = itemStackToJson(item);
        String sql = "UPDATE items SET item_data = ?, web_data = ? WHERE id = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemData);
            stmt.setString(2, webData.toString());
            stmt.setString(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:24835abe", "Failed to update Citem: ", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:de1adeb7", "Failed to retrieve Citem: ", e);
            return null;
        }
    }

    public boolean citemExists(String id) {
        try (Connection conn = db.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement("SELECT 1 FROM items WHERE id = ?")) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "CID:ce7f7083", "Failed to check existing Citem: ", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:9592f4b1", "Failed to build Base64 String", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:d270e031", "Failed to deserialize Itemstack from Base64 String", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:9e2bb566", "Failed to create Item Display", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:4e63206a", "Failed to remove Item Display", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:b1ebfe0d", "Failed to get Owner UUID", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:621ebf18", "Failed to change Item Display", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:cbf86e39", "Failed to get Item Display Location", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:9b378252", "Failed to get Item Display ID", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:593f879f", "Failed to check Item Display", e);
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
            WoSSystems.discordLog(Level.SEVERE, "CID:f2863bfe", "Failed to check Item Display Owner", e);
        }
        return false;
    }


    private JsonObject itemStackToJson(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        JsonObject obj = new JsonObject();
        obj.addProperty("material", item.getType().toString());
        obj.addProperty("display_name", meta.getDisplayName());
        JsonArray lore = new JsonArray();
        if (meta.getLore() != null) {
            for (String line : meta.getLore()) {
                lore.add(line);
            }
        }
        obj.add("lore", lore);
        obj.addProperty("enchanted", meta.hasEnchants());
        PersistentDataContainer data = meta.getPersistentDataContainer();
        obj.addProperty("right-click", data.get(Keys.RIGHT_ACTION.get(), PersistentDataType.STRING));
        obj.addProperty("left-click", data.get(Keys.LEFT_ACTION.get(), PersistentDataType.STRING));
        return obj;
    }

}
