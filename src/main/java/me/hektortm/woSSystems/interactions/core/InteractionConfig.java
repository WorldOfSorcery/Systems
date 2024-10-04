package me.hektortm.woSSystems.interactions.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractionConfig {

    private List<String> npcs;
    private List<Location> locations;
    private List<String> actions;
    private String inventoryTitle;
    private int inventoryRows;
    private Map<Integer, Map<String, Object>> slots = new HashMap<>();
    private Map<Integer, Boolean> slotClickable = new HashMap<>();
    private Map<Integer, Map<String, String>> slotActions = new HashMap<>();

    // New fields for particle settings
    private String particleType;
    private String particleColor;

    public InteractionConfig(FileConfiguration config) {
        // Load NPCs list
        npcs = config.getStringList("npc");

        particleType = config.getString("particle.type");
        particleColor = config.getString("particle.color");

        // Initialize locations list (assumed to be strings in YAML)
        locations = new ArrayList<>();
        List<String> locationStrings = config.getStringList("block");  // Assuming the YAML key is "block"

        for (String locStr : locationStrings) {
            String[] parts = locStr.split(",");  // Split the string by commas
            if (parts.length == 4) {  // Ensure there are 4 parts (world, x, y, z)
                World world = Bukkit.getWorld(parts[0]);  // Get the world by name
                if (world != null) {
                    try {
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);
                        // Add the location to the list
                        locations.add(new Location(world, x, y, z));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid coordinates in block location: " + locStr);
                    }
                } else {
                    System.err.println("Invalid world name: " + parts[0]);
                }
            } else {
                System.err.println("Invalid block location format: " + locStr);
            }
        }

        // Load actions (as a list of strings)
        actions = config.getStringList("action");

        // Load inventory properties (with default values)
        inventoryTitle = config.getString("open-inv.title", "Default Inventory");
        inventoryRows = config.getInt("open-inv.rows", 3);

        // Load slot information (if it exists)
        if (config.contains("open-inv.slots")) {
            ConfigurationSection slotsSection = config.getConfigurationSection("open-inv.slots");
            if (slotsSection != null) {
                for (String key : slotsSection.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);  // Convert the slot key to an integer
                        ConfigurationSection slotConfig = slotsSection.getConfigurationSection(key);

                        if (slotConfig != null) {
                            // Store the slot configuration
                            slots.put(slot, slotConfig.getValues(true));

                            // Load the clickable flag (default to true if not present)
                            boolean clickable = slotConfig.getBoolean("clickable", true);
                            slotClickable.put(slot, clickable);

                            // Load actions for different click types (left, right, shift)
                            Map<String, String> actions = new HashMap<>();
                            ConfigurationSection actionsSection = slotConfig.getConfigurationSection("actions");
                            if (actionsSection != null) {
                                actions.put("left-click", actionsSection.getString("left-click"));
                                actions.put("right-click", actionsSection.getString("right-click"));
                                actions.put("shift-click", actionsSection.getString("shift-click"));
                            }
                            slotActions.put(slot, actions);
                        }
                    } catch (NumberFormatException e) {
                        // Handle case where slot key is not a number
                        System.err.println("Invalid slot key: " + key);
                    }
                }
            }
        }
    }

    public void loadLocations(FileConfiguration boundConfig) {
        List<String> locationStrings = boundConfig.getStringList("block");  // Assuming the YAML key is "block"

        for (String locStr : locationStrings) {
            String[] parts = locStr.split(",");
            if (parts.length == 4) {
                World world = Bukkit.getWorld(parts[0]);  // Get world by name
                if (world != null) {
                    try {
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);
                        // Add Location object to the list
                        locations.add(new Location(world, x, y, z));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid coordinates in block location: " + locStr);
                    }
                } else {
                    System.err.println("Invalid world name: " + parts[0]);
                }
            } else {
                System.err.println("Invalid block location format: " + locStr);
            }
        }
    }


    public String getParticleType() {
        return particleType;
    }

    public void setParticleType(String particleType) {
        this.particleType = particleType;
    }

    public String getParticleColor() {
        return particleColor;
    }

    public void setParticleColor(String particleColor) {
        this.particleColor = particleColor;
    }

    // Add particle type and color parsing in your config loader
    public boolean hasParticle() {
        return particleType != null && !particleType.isEmpty();
    }

    public List<String> getActions() {
        return actions;
    }

    public List<String> getNpcs() {
        return npcs;
    }

    public List<Location> getLocations() {
        return locations;
    }


    public Map<Integer, Map<String, Object>> getSlots() {
        return slots;
    }

    public boolean hasInventory() {
        return inventoryTitle != null && inventoryRows > 0;
    }


    public String getInventoryTitle() {
        return inventoryTitle;
    }

    public int getInventoryRows() {
        return inventoryRows;
    }


    public boolean isSlotClickable(int slot) {
        return slotClickable.getOrDefault(slot, true);
    }

    public String getSlotAction(int slot, String clickType) {
        if (slotActions.containsKey(slot)) {
            return slotActions.get(slot).get(clickType);
        }
        return null;
    }

}