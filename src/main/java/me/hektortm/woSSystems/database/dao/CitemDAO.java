package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;

import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class CitemDAO implements IDAO {
    private final me.hektortm.wosCore.database.DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "CitemDAO";

    public CitemDAO(DatabaseManager db, DAOHub daoHub) throws SQLException {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS citems(" +
                    "id VARCHAR(255), " +
                    "material VARCHAR(255), " +
                    "display_name VARCHAR(255), " +
                    "lore TEXT, " +
                    "enchantments TEXT, " +
                    "damage INT, " +
                    "color VARCHAR(255)," +
                    "custom_model_data INT, " +
                    "flag_undroppable BOOLEAN, "+
                    "flag_unusable BOOLEAN, " +
                    "flag_placeable INT, "+
                    "flag_profile_bg TEXT, " +
                    "flag_profile_picture TEXT, " +
                    "action_left TEXT, " +
                    "action_right TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS placed_citems("+
                    "citem_id VARCHAR(255) NOT NULL, " +
                    "owner_uuid CHAR(36) NOT NULL, " +
                    "block_location VARCHAR(255) NOT NULL, " +
                    "display_location VARCHAR(255) NOT NULL," +
                    "PRIMARY KEY (citem_id))");
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

    public void saveCitem(String id, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String material = item.getType().toString();
        String display_name = meta.getDisplayName() != null ? meta.getDisplayName() : "";
        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        String loreString = lore != null ? lore.toString() : "";
        String enchants = meta.hasEnchants() ? meta.getEnchants().toString() : null;
        int damage = meta instanceof Damageable ? ((Damageable) meta).getDamage() : 0;
        Color color = meta instanceof LeatherArmorMeta ? ((LeatherArmorMeta) meta).getColor() : null;
        int custom_model_data = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        Boolean undroppable = data.has(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN)
                ? data.get(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN) : false;
        Boolean unusable = data.has(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN)
                ? data.get(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN) : false;

        String actionLeft = data.has(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING) : null;
        String actionRight = data.has(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING) : null;

        Integer placeable = data.has(plugin.getCitemManager().getPlaceableKey(), PersistentDataType.INTEGER)
                ? data.get(plugin.getCitemManager().getPlaceableKey(), PersistentDataType.INTEGER) : 0;

        String profilePicture = data.has(plugin.getCitemManager().getProfilePicKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getProfilePicKey(), PersistentDataType.STRING) : null;
        String profileBackground = data.has(plugin.getCitemManager().getProfileBgKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getProfileBgKey(), PersistentDataType.STRING) : null;

        String query = "INSERT INTO citems (id, material, display_name, lore, enchantments, damage, color, custom_model_data, flag_undroppable, flag_unusable, action_left, action_right, flag_placeable, flag_profile_bg, flag_profile_picture) VALUES (?, ?, ?,?,?,?,?,?,?,?,?,?,?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.setString(2, material);
            stmt.setString(3, display_name);
            stmt.setString(4, loreString);
            stmt.setString(5, enchants);
            stmt.setInt(6, damage);
            stmt.setString(7, color.toString());
            stmt.setInt(8, custom_model_data);
            stmt.setBoolean(9, undroppable);
            stmt.setBoolean(10, unusable);
            stmt.setString(11, actionLeft);
            stmt.setString(12, actionRight);
            stmt.setInt(13, placeable);
            stmt.setString(14, profileBackground);
            stmt.setString(15, profilePicture);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to save Citem: " + e);
        }
    }

    public void updateCitem(String id, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String material = item.getType().toString();
        String display_name = meta.getDisplayName() != null ? meta.getDisplayName() : "";
        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        String loreString = lore != null ? lore.toString() : "";
        String enchants = meta.hasEnchants() ? meta.getEnchants().toString() : null;
        int damage = meta instanceof Damageable ? ((Damageable) meta).getDamage() : 0;
        Color color = meta instanceof LeatherArmorMeta ? ((LeatherArmorMeta) meta).getColor() : null;
        int custom_model_data = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        Boolean undroppable = data.has(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN)
                ? data.get(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN) : false;
        Boolean unusable = data.has(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN)
                ? data.get(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN) : false;

        String actionLeft = data.has(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING) : null;
        String actionRight = data.has(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING) : null;

        Integer placeable = data.has(plugin.getCitemManager().getPlaceableKey(), PersistentDataType.INTEGER)
                ? data.get(plugin.getCitemManager().placeableKey, PersistentDataType.INTEGER) : null;

        String profilePicture = data.has(plugin.getCitemManager().getProfilePicKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getProfilePicKey(), PersistentDataType.STRING) : null;
        String profileBackground = data.has(plugin.getCitemManager().getProfileBgKey(), PersistentDataType.STRING)
                ? data.get(plugin.getCitemManager().getProfileBgKey(), PersistentDataType.STRING) : null;

        String query = "UPDATE citems SET material = ?, display_name = ?, lore = ?, enchantments = ?, damage = ?, color = ?, custom_model_data = ?, flag_undroppable = ?, flag_unusable = ?, action_left = ?, action_right = ?, flag_placeable = ?, flag_profile_bg = ?, flag_profile_picture = ? WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, material);
            stmt.setString(2, display_name);
            stmt.setString(3, loreString);
            stmt.setString(4, enchants);
            stmt.setInt(5, damage);
            stmt.setString(6, color.toString());
            stmt.setInt(6, custom_model_data);
            stmt.setBoolean(7, undroppable);
            stmt.setBoolean(8, unusable);
            stmt.setString(9, actionLeft);
            stmt.setString(10, actionRight);
            stmt.setInt(11, placeable);
            stmt.setString(12, profileBackground);
            stmt.setString(13, profilePicture);
            stmt.setString(14, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to update Citem: " + e);
        }
    }

    public boolean citemExists(String id) {
        try (Connection conn = db.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement("SELECT 1 FROM citems WHERE id = ?")) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to check existing Citem: " + e);
            return false;
        }
    }

    public List<String> getLore(String id) {
        String sql = "SELECT lore FROM citems WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            String loreString = resultSet.getString("lore");
            List<String> lore = (loreString != null && !loreString.isEmpty())
                    ? Arrays.asList(loreString.replace("[", "").replace("]", "").split(", "))
                    : null;
            return lore;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Citem Lore: " + e);
            return null;
        }
    }

    public String getDisplayName(String id) {
        String sql = "SELECT display_name FROM citems WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            String displayName = resultSet.getString("display_name");
            return displayName;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Citem Display Name: " + e);
            return null;
        }
    }

    public ItemStack getCitem(String id) {
        String query = "SELECT * FROM citems WHERE id = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Extract fields from the database
                String display_name = rs.getString("display_name");
                String material = rs.getString("material");
                String loreString = rs.getString("lore");
                String enchantsString = rs.getString("enchantments");
                int damage = rs.getInt("damage");
                Color color = Parsers.parseColorFromString(rs.getString("color"));
                int custom_model_data = rs.getInt("custom_model_data");
                boolean undroppable = rs.getBoolean("flag_undroppable");
                boolean unusable = rs.getBoolean("flag_unusable");
                String actionLeft = rs.getString("action_left");
                String actionRight = rs.getString("action_right");
                int placeable = rs.getInt("flag_placeable");
                String profileBackground = rs.getString("flag_profile_bg");
                String profilePicture = rs.getString("flag_profile_picture");

                // Convert lore from a string back to a list
                List<String> lore = (loreString != null && !loreString.isEmpty())
                        ? Arrays.asList(loreString.replace("[", "").replace("]", "").split(", "))
                        : null;

                // Convert enchantments from a string back to a map
                Map<Enchantment, Integer> enchants = new HashMap<>();
                if (enchantsString != null && !enchantsString.isEmpty()) {
                    String[] enchantPairs = enchantsString.replace("{", "").replace("}", "").split(", ");
                    for (String pair : enchantPairs) {
                        String[] parts = pair.split("=");
                        if (parts.length == 2) {
                            Enchantment enchantment = Enchantment.getByName(parts[0].trim());
                            int level = Integer.parseInt(parts[1].trim());
                            if (enchantment != null) {
                                enchants.put(enchantment, level);
                            }
                        }
                    }
                }

                // Create the new ItemStack
                ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(material))); // Default, change as needed
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    // Set display name
                    if (display_name != null && !display_name.isEmpty()) {
                        meta.setDisplayName(display_name);
                    }

                    // Set lore
                    if (lore != null) {
                        meta.setLore(lore);
                    }

                    // Set enchantments
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }

                    // Set damage if applicable
                    if (meta instanceof Damageable) {
                        ((Damageable) meta).setDamage(damage);
                    }

                    if (meta instanceof LeatherArmorMeta) {
                        ((LeatherArmorMeta) meta).setColor(color);
                    }

                    // Set custom model data
                    if (custom_model_data != 0) {
                        meta.setCustomModelData(custom_model_data);
                    }

                    // Set Persistent Data (flags & actions)
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    NamespacedKey idKey = plugin.getCitemManager().getIdKey();
                    NamespacedKey undroppableKey = plugin.getCitemManager().getUndroppableKey();
                    NamespacedKey unusableKey = plugin.getCitemManager().getUnusableKey();
                    NamespacedKey leftActionKey = plugin.getCitemManager().getLeftActionKey();
                    NamespacedKey rightActionKey = plugin.getCitemManager().getRightActionKey();
                    NamespacedKey placeableKey = plugin.getCitemManager().getPlaceableKey();
                    NamespacedKey profileBgKey = plugin.getCitemManager().getProfileBgKey();
                    NamespacedKey profilePicKey = plugin.getCitemManager().getProfilePicKey();

                    data.set(idKey, PersistentDataType.STRING, id);
                    data.set(undroppableKey, PersistentDataType.BOOLEAN, undroppable);
                    data.set(unusableKey, PersistentDataType.BOOLEAN, unusable);
                    data.set(placeableKey, PersistentDataType.INTEGER, placeable);
                    if (profileBackground != null) {
                        data.set(profileBgKey, PersistentDataType.STRING, profileBackground);
                    }
                    if (profilePicture != null) {
                        data.set(profilePicKey, PersistentDataType.STRING, profilePicture);
                    }
                    if (actionLeft != null) {
                        data.set(leftActionKey, PersistentDataType.STRING, actionLeft);
                    }
                    if (actionRight != null) {
                        data.set(rightActionKey, PersistentDataType.STRING, actionRight);
                    }

                    item.setItemMeta(meta);
                }

                return item;
            }

        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Citem: " + e);
        }

        return null; // Return null if no item was found
    }


    public void deleteCitem(String id) {
        String query = "DELETE FROM citems WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to delete Citem: " + e);
        }
    }
}
