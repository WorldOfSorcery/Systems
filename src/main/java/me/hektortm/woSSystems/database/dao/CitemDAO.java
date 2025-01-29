package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DatabaseManager;
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

public class CitemDAO {

    private final Connection conn;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public CitemDAO(DatabaseManager db) {
        this.conn = db.getConnection();
        createTable();
    }

    private void createTable() {
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
        } catch (SQLException e) {
            plugin.writeLog("CitemDAO", Level.SEVERE, "Error creating Tables: " + e.getMessage());
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



        String query = "INSERT INTO citems (id, material, display_name, lore, enchantments, damage, custom_model_data, flag_undroppable, flag_unusable, action_left, action_right) VALUES (?, ?, ?,?,?,?,?,?,?,?,?)";
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



        String query = "UPDATE citems SET material = ?, display_name = ?, lore = ?, enchantments = ?, damage = ?, custom_model_data = ?, flag_undroppable = ?, flag_unusable = ?, action_left = ?, action_right = ? WHERE id = ?";
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
            stmt.setString(11, id);
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

                    data.set(idKey, PersistentDataType.STRING, id);
                    data.set(undroppableKey, PersistentDataType.BOOLEAN, undroppable);
                    data.set(unusableKey, PersistentDataType.BOOLEAN, unusable);
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
