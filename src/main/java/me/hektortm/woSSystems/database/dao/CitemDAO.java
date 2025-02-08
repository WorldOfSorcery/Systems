package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;

import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class CitemDAO implements IDAO {
    private final Connection conn;
    private final me.hektortm.wosCore.database.DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public CitemDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
        this.conn = db.getConnection();
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS citems(" +
                    "id TEXT, " +
                    "material TEXT, " +
                    "display_name TEXT, " +
                    "lore TEXT, " +
                    "enchantments TEXT, " +
                    "damage INT, " +
                    "custom_model_data INT, " +
                    "flag_undroppable BOOLEAN, "+
                    "flag_unusable BOOLEAN, "+
                    "action_left TEXT, " +
                    "action_right TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS placed_citems("+
                    "citem_id TEXT PRIMARY_KEY NOT NULL, " +
                    "owner_uuid TEXT NOT NULL, " +
                    "location TEXT NOT NULL)");
            stmt.execute("UPDATE TABLE IF EXISTS citems("+
                    "id TEXT, " +
                    "material TEXT, " +
                    "display_name TEXT, " +
                    "lore TEXT, " +
                    "enchantments TEXT, " +
                    "damage INT, " +
                    "custom_model_data INT, " +
                    "flag_undroppable BOOLEAN, "+
                    "flag_unusable BOOLEAN, "+
                    "action_left TEXT, " +
                    "action_right TEXT, "+
                    "flag_placeable BOOLEAN)");
        }
    }

    public void createItemDisplay(String id, UUID ownerUUID, Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "INSERT INTO placed_citems (citem_id, owner_uuid, location) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, ownerUUID.toString());
            pstmt.setString(3, loc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeItemDisplay(UUID ownerUUID, Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "DELETE FROM placed_items WHERE owner_uuid = ? AND location = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ownerUUID.toString());
            pstmt.setString(2, loc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UUID getUUID(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT owner_uuid FROM placed_citems WHERE location = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            return UUID.fromString(rs.getString("owner_uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getItemDisplayID(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT citem_id FROM placed_citems WHERE location = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("citem_id");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isItemDisplay(Location location) {
        String loc = Parsers.locationToString(location);
        String sql = "SELECT  1 FROM placed_citems WHERE location = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loc);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isItemDisplayOwner(Location location, UUID uuid) {
        String sql = "SELECT 1 FROM placed_citems WHERE location = ? AND owner_uuid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, location.toString());
            pstmt.setString(2, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void saveCitem(String id, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String material = item.getType().toString();
        String display_name = meta.getDisplayName() != null ? meta.getDisplayName() : "";
        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        String loreString = lore != null ? lore.toString() : "";
        String enchants = meta.hasEnchants() ? meta.getEnchants().toString() : null;
        int damage = meta instanceof Damageable ? ((Damageable) meta).getDamage() : 0;
        int custom_model_data = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        Boolean undroppable = data.has(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN) ? data.get(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN) : false;
        Boolean unusable = data.has(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN) ? data.get(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN) : false;

        String actionLeft = data.has(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING) ? data.get(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING) : null;
        String actionRight = data.has(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING) ? data.get(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING) : null;

        Boolean placeable = data.has(plugin.getCitemManager().getPlaceableKey(), PersistentDataType.BOOLEAN) ? data.get(plugin.getCitemManager().getPlaceableKey(), PersistentDataType.BOOLEAN) : false;

        String query = "INSERT INTO citems (id, material, display_name, lore, enchantments, damage, custom_model_data, flag_undroppable, flag_unusable, action_left, action_right, flag_pleaceable) VALUES (?, ?, ?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.setString(2, material);
            stmt.setString(3, display_name);
            stmt.setString(4, loreString);
            stmt.setString(5, enchants);
            stmt.setInt(6, damage);
            stmt.setInt(7, custom_model_data);
            stmt.setBoolean(8, undroppable);
            stmt.setBoolean(9, unusable);
            stmt.setString(10, actionLeft);
            stmt.setString(11, actionRight);
            stmt.setBoolean(12, placeable);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        int custom_model_data = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        Boolean undroppable = data.has(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN) ? data.get(plugin.getCitemManager().getUndroppableKey(), PersistentDataType.BOOLEAN) : false;
        Boolean unusable = data.has(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN) ? data.get(plugin.getCitemManager().getUnusableKey(), PersistentDataType.BOOLEAN) : false;

        String actionLeft = data.has(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING) ? data.get(plugin.getCitemManager().getLeftActionKey(), PersistentDataType.STRING) : null;
        String actionRight = data.has(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING) ? data.get(plugin.getCitemManager().getRightActionKey(), PersistentDataType.STRING) : null;

        Boolean placeable = data.has(plugin.getCitemManager().getPlaceableKey(), PersistentDataType.BOOLEAN) ? data.get(plugin.getCitemManager().placeableKey, PersistentDataType.BOOLEAN) : false;


        String query = "UPDATE citems SET material = ?, display_name = ?, lore = ?, enchantments = ?, damage = ?, custom_model_data = ?, flag_undroppable = ?, flag_unusable = ?, action_left = ?, action_right = ?, flag_placeable = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, material);
            stmt.setString(2, display_name);
            stmt.setString(3, loreString);
            stmt.setString(4, enchants);
            stmt.setInt(5, damage);
            stmt.setInt(6, custom_model_data);
            stmt.setBoolean(7, undroppable);
            stmt.setBoolean(8, unusable);
            stmt.setString(9, actionLeft);
            stmt.setString(10, actionRight);
            stmt.setBoolean(11, placeable);
            stmt.setString(12, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog("CitemDAO", Level.SEVERE, "Error updating Citem: " + e.getMessage());
        }
    }

    public boolean citemExists(String id) {
        try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT 1 FROM citems WHERE id = ?")) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.writeLog("CitemDAO", Level.SEVERE, "Error retrieving data: " + e.getMessage());
            return false;
        }
    }

    public List<String> getLore(String id) {
        String sql = "SELECT lore FROM citems WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            String loreString = resultSet.getString("lore");
            List<String> lore = (loreString != null && !loreString.isEmpty())
                    ? Arrays.asList(loreString.replace("[", "").replace("]", "").split(", "))
                    : null;
            return lore;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDisplayName(String id) {
        String sql = "SELECT display_name FROM citems WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            String displayName = resultSet.getString("display_name");
            return displayName;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ItemStack getCitem(String id) {
        String query = "SELECT * FROM citems WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Extract fields from the database
                String display_name = rs.getString("display_name");
                String material = rs.getString("material");
                String loreString = rs.getString("lore");
                String enchantsString = rs.getString("enchantments");
                int damage = rs.getInt("damage");
                int custom_model_data = rs.getInt("custom_model_data");
                boolean undroppable = rs.getBoolean("flag_undroppable");
                boolean unusable = rs.getBoolean("flag_unusable");
                String actionLeft = rs.getString("action_left");
                String actionRight = rs.getString("action_right");
                boolean placeable = rs.getBoolean("flag_placeable");

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

                    data.set(idKey, PersistentDataType.STRING, id);
                    data.set(undroppableKey, PersistentDataType.BOOLEAN, undroppable);
                    data.set(unusableKey, PersistentDataType.BOOLEAN, unusable);
                    data.set(placeableKey, PersistentDataType.BOOLEAN, placeable);
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
            plugin.writeLog("CitemDAO", Level.SEVERE, "Error getting citem: " + e.getMessage());
        }

        return null; // Return null if no item was found
    }


    public void deleteCitem(String id) {
        String query = "DELETE FROM citems WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
