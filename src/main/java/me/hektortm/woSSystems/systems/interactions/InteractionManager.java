package me.hektortm.woSSystems.systems.interactions;



import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.systems.interactions.config.InteractionConfig;
import me.hektortm.woSSystems.systems.interactions.config.YAMLLoader;
import me.hektortm.woSSystems.systems.interactions.actions.ActionHandler;
import me.hektortm.woSSystems.systems.interactions.particles.ParticleHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class InteractionManager {

    private Map<String, InteractionConfig> interactions = new HashMap<>();
    private YAMLLoader yamlLoader;
    private ActionHandler actionHandler;
    private final GUIManager guiManager;
    private final WoSSystems plugin;
    private final ParticleHandler particleHandler;
    private final PlaceholderResolver resolver;

    public InteractionManager(YAMLLoader yamlLoader, WoSSystems plugin, GUIManager guiManager, ParticleHandler particleHandler, PlaceholderResolver resolver) {
        this.yamlLoader = yamlLoader;
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.particleHandler = particleHandler;
        this.resolver = resolver;
        this.actionHandler = new ActionHandler(plugin, resolver);
    }



    public void loadAllInteractions() {
        interactions.clear();
        interactions.putAll(yamlLoader.loadInteractions());

        // Load bound locations separately from the bound folder
        for (String interactionId : interactions.keySet()) {
            loadBoundLocations(interactionId);  // Load bound blocks for each interaction
        }
    }

    // Reload a specific interaction
    public void reloadInteraction(String id) {
        // Create a reference to the interaction file using the ID
        File interactionFile = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "interactions/" + id + ".yml");

        // Check if the file exists
        if (!interactionFile.exists()) {
            plugin.getLogger().warning("Interaction file " + id + ".yml not found!");
            return;
        }

        // Load the interaction from the file
        InteractionConfig config = yamlLoader.loadInteraction(interactionFile);

        if (config != null) {
            // Replace or add the new interaction config to the map
            interactions.put(id, config);
        } else {
            plugin.getLogger().warning("Failed to load interaction: " + id);
        }
    }


    // Reload all interactions
    public void reloadAllInteractions() {
        loadAllInteractions();
    }

    public void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (InteractionConfig interaction : interactions.values()) {
                    if (interaction.hasParticle()) {
                        for (Location location : interaction.getLocations()) {
                            if (location != null) {
                                spawnParticles(interaction, location);
                            } else {
                                plugin.getLogger().warning("Location is null for interaction.");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }


    private void spawnParticles(InteractionConfig interaction, Location location) {
        String particleType = interaction.getParticleType();
        double radius = 1.0;

        switch (particleType.toLowerCase()) {
            case "redstone_dust":
                particleHandler.spawnRedstoneParticle(interaction, location);
                break;
            case "portal":
                spawnSurroundingParticles(location, Particle.PORTAL);
                break;
            case "villager_happy":
                particleHandler.spawnVillagerHappyParticles(location);
                break;
            case "villager_happy_circle":
                particleHandler.spawnVillagerHappyCircleParticles(location);
                break;
            case "flame":
                spawnSurroundingParticles(location, Particle.SMALL_FLAME);
                break;
            case "totem":
                spawnSurroundingParticles(location, Particle.TOTEM_OF_UNDYING);
                break;
            case "smoke":
                spawnSurroundingParticles(location, Particle.SMOKE);
                break;
            case "explosion":
                spawnSurroundingParticles(location, Particle.EXPLOSION);
                break;
            default:
                // Default behavior if the particle type is unknown
                break;
        }
    }

    // TODO: Shapes
    // Circle:
    //      double angle = Math.random() * Math.PI * 2; // Random angle for circular distribution
    //      double xOffset = radius * Math.cos(angle)+0.5; // Calculate x offset
    //      double zOffset = radius * Math.sin(angle)+0.5; // Calculate z offset


    private void spawnSurroundingParticles(Location location, Particle particle) {
        double radius = 2.5;
        int countPerLayer = 1; // Number of particles to spawn per layer
        double offset = 0.28; // Half of the block size for offsetting (smaller value for tighter surrounding)

        // Loop to create a cuboid shape around the block
        for (int x = -1; x <= 1; x++) { // X dimension
            for (int z = -1; z <= 1; z++) { // Z dimension
                for (int y = 0; y <= 1; y++) { // Y dimension, only the top and middle
                    for (int i = 0; i < countPerLayer; i++) {
                        double randomX = radius * (Math.random() * offset * 2) - offset+0.1; // Random x offset within the range
                        double randomZ = radius * (Math.random() * offset * 1.9) - offset+0.1; // Random z offset within the range
                        double randomY = radius * Math.random() * 0.4; // Random y offset to create vertical variation

                        // Spawn particles at the calculated position relative to the block
                        location.getWorld().spawnParticle(particle,
                                location.getX() + randomX,
                                location.getY() + randomY,
                                location.getZ() + randomZ,
                                1);
                    }
                }
            }
        }
    }


    public void loadBoundLocations(String interactionId) {
        File boundFile = new File(plugin.getDataFolder(), "bound/" + interactionId + ".yml");

        if (!boundFile.exists()) {
            plugin.getLogger().warning("Bound file for interaction " + interactionId + " not found.");
            return;
        }

        // Use YamlConfiguration to load the bound file
        FileConfiguration boundConfig = YamlConfiguration.loadConfiguration(boundFile);
        InteractionConfig interaction = interactions.get(interactionId);

        if (interaction != null) {
            interaction.loadLocations(boundConfig);  // Load locations from the bound file
        } else {
            plugin.getLogger().warning("Interaction config for " + interactionId + " is null.");
        }
    }





    // Get an interaction by its ID
    public InteractionConfig getInteractionById(String id) {
        return interactions.get(id);
    }

    // Get all loaded interaction IDs
    public String getAllInteractionIds() {
        return String.join(", ", interactions.keySet());
    }

    // Trigger an interaction for a player
    public void triggerInteraction(InteractionConfig interaction, Player player) {
        if (interaction == null) {
            player.sendMessage(ChatColor.RED + "Interaction not found.");
            return;
        }
        actionHandler.triggerActions(interaction.getActions(), player);
    }

    public InteractionConfig getInteractionConfig(String id) {
        return interactions.get(id);
    }

}