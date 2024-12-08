package me.hektortm.woSSystems.systems.interactions_rework;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class InteractionManager {

    public final File interactionFolder;
    private final WoSSystems plugin = new WoSSystems();
    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)), WoSCore.getPlugin(WoSCore.class));
    private final PlaceholderResolver resolver = plugin.getPlaceholderResolver();
    private final ConditionHandler conditionHandler = plugin.getConditionHandler();
    private Map<String, InteractionData> interactionMap = new HashMap<>();

    public InteractionManager() {
        this.interactionFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "interactions");
        if(!interactionFolder.exists()) interactionFolder.mkdirs();
    }

    public void loadInteraction() {
        File[] interactionFiles = interactionFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if(interactionFiles == null || interactionFiles.length == 0) {
            log.sendWarning("No Interactions found in " + interactionFolder.getPath());
            return;
        }

        for (File file : interactionFiles) {
            String interactionId =  file.getName().replace(".json", "");
            try (FileReader reader = new FileReader(file)) {
                JSONObject json = (JSONObject) new JSONParser().parse(reader);

                JSONObject bound = (JSONObject) json.get("bound");
                JSONArray locationArray = (JSONArray) bound.get("location");
                JSONArray npcArray = (JSONArray) bound.get("npc");

                List<Location> locations = new ArrayList<>();
                for (Object location : locationArray) {
                    locations.add((Location) location);
                }

                List<String> npcs = new ArrayList<>();
                for (Object npc : npcArray) {
                    npcs.add((String) npc);
                }

                JSONObject particlesJson = (JSONObject) json.get("particles");
                String particleType = (String) particlesJson.get("type");
                String particleColor = (String) particlesJson.get("color");

                // Load the "conditions" section
                JSONObject conditions = (JSONObject) json.get("conditions");

                // Load the "actions" section
                JSONArray actionsArray = (JSONArray) json.get("actions");
                List<String> actions = new ArrayList<>();
                for (Object action : actionsArray) {
                    actions.add((String) action);
                }

                interactionMap.put(interactionId, new InteractionData(conditions,actions,locations,npcs,particleType,particleColor));

            } catch (Exception e) {
                //
            }
        }
    }

    public void loadAllInteractions() {
        interactionMap.clear();
        if (interactionFolder.listFiles() == null) {
            plugin.getLogger().info("No Interactions found in " + interactionFolder.getPath());
            return;
        }
        for (File file : Objects.requireNonNull(interactionFolder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                String id = file.getName().replace(".json", "");
                InteractionData inter = interactionMap.get(id);
                if (inter != null) {
                    interactionMap.put(id, inter);
                }
            }
        }
    }

    public void reloadInter(String id) {
        if(interactionMap.containsKey(id)) {
            interactionMap.put(id, interactionMap.get(id));
        } else {
            plugin.getLogger().warning("Failed to reload interaction: " + id);
        }
    }


    public void triggerInteraction(Player p, String id) {
        InteractionData inter = getInteractionByID(id);

        if(!conditionHandler.validateConditions(p, inter.getConditions())) return;

        if(inter.getActions() == null) return;

        for (String action : inter.getActions()) {
            action = action.replace("%player%", p.getName());

            if(action.startsWith("send_message")) {
                String message = action.replace("%message%", "");
                String finalMessage = resolver.resolvePlaceholders(message, p);
                p.sendMessage(finalMessage.replace("&","§"));
            }
            if (action.startsWith("close_gui")) {
                p.closeInventory();
            }
            if (action.startsWith("playsound")) {
                String[] parts = action.split(" ");
                if (parts.length > 1) {
                    String sound = parts[1];
                    p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
                    return;
                }
            }
            else {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action);
            }
        }
    }

    public void particleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (InteractionData inter : interactionMap.values()) {
                    for (Location location : inter.getLocations()) {
                        if (location != null) {
                            spawnParticles(inter, location);
                        }
                    }
                }
            }

        }.runTaskTimer(plugin,0L,10L);
    }

    public void spawnParticles(InteractionData inter, Location location) {
        String particleType = inter.getParticleType();

        switch (particleType.toLowerCase()) {
            case "redstone_dust":
                spawnRedstoneParticle(inter, location);
                break;
            case "portal":
                spawnSurroundingParticles(location, Particle.PORTAL);
                break;
            case "villager_happy":
                spawnVillagerHappyParticles(location);
                break;
            case "villager_happy_circle":
                spawnVillagerHappyCircleParticles(location);
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

    public Location getTargetBlock(Player p) {
        double distance = 5.0;

        Block target = p.getTargetBlock(null, (int)distance);

        if (target == null) return null;
        return target.getLocation();
    }

    public void unbindLocation(String id, Location loc) {
        InteractionData inter = getInteractionByID(id);
        inter.removeLocation(loc);
    }

    public void bindLocation(String id, Location loc) {
        InteractionData inter = getInteractionByID(id);
        inter.addLocation(loc);
    }

    public InteractionData getInteractionByID(String id) {
        return interactionMap.get(id);
    }


    public void spawnVillagerHappyParticles(Location location) {
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
                        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                                location.getX() + randomX,
                                location.getY() + randomY,
                                location.getZ() + randomZ,
                                1);
                    }
                }
            }
        }
    }

    public void spawnVillagerHappyCircleParticles(Location location) {
        int count = 15; // Total number of particles
        double radius = 0.5; // Distance from the center of the block to spawn particles

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2; // Random angle for circular distribution
            double yOffset = Math.random() * 0.5; // Randomly offset particles a little above and below the center
            double xOffset = radius * Math.cos(angle)+0.5; // Calculate x offset
            double zOffset = radius * Math.sin(angle)+0.5; // Calculate z offset

            // Spawn the particle around the block, using the center of the block
            location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.getX() + xOffset, location.getY() + yOffset, location.getZ() + zOffset, 1);
        }
    }

    public void spawnRedstoneParticle(InteractionData interaction, Location location) {
        String colorHex = interaction.getParticleColor();
        Color color = Color.fromRGB(
                Integer.valueOf(colorHex.substring(1, 3), 16),
                Integer.valueOf(colorHex.substring(3, 5), 16),
                Integer.valueOf(colorHex.substring(5, 7), 16)
        );
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);

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
                        location.getWorld().spawnParticle(Particle.DUST, location.getX() + randomX, location.getY() + randomY, location.getZ() + randomZ, 1, dustOptions);
                    }
                }
            }
        }

    }

}
