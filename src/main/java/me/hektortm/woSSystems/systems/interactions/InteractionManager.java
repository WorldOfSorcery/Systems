package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class InteractionManager {

    public final File interactionFolder;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)), WoSCore.getPlugin(WoSCore.class));
    private PlaceholderResolver resolver;
    private ConditionHandler conditionHandler;
    public final Map<String, InteractionData> interactionMap = new HashMap<>();

    public InteractionManager() {
        this.interactionFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "interactions");
        if(!interactionFolder.exists()) interactionFolder.mkdirs();
    }

    // TODO: Particle Conditions

    public void setConditionHandler(ConditionHandler conditionHandler) {
        if (conditionHandler == null) {
            throw new IllegalArgumentException("ConditionHandler cannot be null.");
        }
        this.conditionHandler = conditionHandler;
    }
    public void setPlaceholderResolver(PlaceholderResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("PlaceholderResolver cannot be null.");
        }
        this.resolver = resolver;
    }


    public void loadInteraction() {
        File[] interactionFiles = interactionFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if(interactionFiles == null || interactionFiles.length == 0) {
            log.sendWarning("No valid interactions loaded. Check JSON structure in " + interactionFolder.getPath());
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
                if (locationArray != null) {
                    for (Object location : locationArray) {
                        // Assuming there's a method to parse location strings into Location objects
                        locations.add(parseLocation((String) location));
                    }
                }

                List<String> npcs = new ArrayList<>();
                if (npcArray != null) {
                    for (Object npc : npcArray) {
                        npcs.add((String) npc);
                    }
                }

                JSONObject particlesJson = (JSONObject) json.get("particles");
                String particleType = particlesJson != null ? (String) particlesJson.get("type") : "";
                String particleColor = particlesJson != null ? (String) particlesJson.get("color") : "";

                JSONObject elseJson = (JSONObject) particlesJson.get("else");
                String elseColor = null;
                String elseType = null;
                if (elseJson != null) {
                    elseColor = (String) elseJson.get("color");
                    elseType = (String) elseJson.get("type");

                }


                // Load the "conditions" section
                JSONObject conditions = (JSONObject) json.get("conditions");

                // Load the "actions" section
                JSONArray actionsArray = (JSONArray) json.get("actions");
                List<String> actions = new ArrayList<>();
                if (actionsArray != null) {
                    for (Object action : actionsArray) {
                        actions.add((String) action);
                    }
                }

                interactionMap.put(interactionId, new InteractionData(conditions,actions,locations,npcs,particleType,particleColor, elseType, elseColor));

            } catch (Exception e) {
                Bukkit.getLogger().warning("Error loading interaction from file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static Location parseLocation(String locationString) {
        try {
            // Split the string by commas
            String[] parts = locationString.split(",");
            if (parts.length < 4) {
                throw new IllegalArgumentException("Location string must have at least 4 parts: world,x,y,z");
            }

            // Extract world name and coordinates
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            // Optional: Parse yaw and pitch if present
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;

            // Get the world from the server
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new IllegalArgumentException("World '" + worldName + "' not found");
            }

            // Return the location object
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            System.err.println("Failed to parse location string: " + locationString);
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }

    public void loadAllInteractions() {
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

        if (inter == null) {
            Bukkit.getLogger().warning(p.getName()+": Interaction \""+ id + "\" not found");
            return;
        }

        if(!conditionHandler.validateConditions(p, inter.getConditions())) return;

        if(inter.getActions() == null) return;

        for (String action : inter.getActions()) {
            action = action.replace("%player%", p.getName());

            if (action.startsWith("send_message")) {
                String message = action.replace("send_message", "");
                String finalMessage = resolver.resolvePlaceholders(message, p);
                p.sendMessage(finalMessage.replace("&", "§"));
            }
            if (action.startsWith("close_gui")) {
                p.closeInventory();
            }
            if (action.startsWith("playsound")) {
                String[] parts = action.split(" ");
                if (parts.length > 1) {
                    String sound = parts[1];
                    p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
                }
            }
            if (action.startsWith("player_cmd")) {
                String cmd = action.replace("player_cmd", "");
                plugin.getServer().dispatchCommand(p, cmd);
            } else {
                if (!action.startsWith("send_message") &&
                        !action.startsWith("close_gui") &&
                        !action.startsWith("playsound") &&
                        !action.startsWith("player_cmd")) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action);
                }
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
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                // Check if player meets conditions
                                    spawnParticlesForPlayer(player, inter, location);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }


    public void spawnParticlesForPlayer(Player player, InteractionData inter, Location location) {
        String particleType = inter.getParticleType();
        if (conditionHandler.validateConditionsNoActions(player, inter.getConditions())) {
            particleType = inter.getParticleType();
        } else {
            if (inter.getElseParticleType() != null) {
                particleType = inter.getElseParticleType();
            } else {
                return;
            }
        }


        switch (particleType.toLowerCase()) {
            case "redstone_dust":
                sendRedstoneParticleToPlayer(player,location, inter);
                break;
            case "redstone_dust_circle":
                spawnRedstoneParticleCircle(player, location, inter);
                break;
            case "portal":
                spawnSurroundingParticles(player, location, Particle.PORTAL);
                break;
            case "villager_happy":
                spawnVillagerHappyParticles(player, location);
                break;
            case "villager_happy_circle":
                spawnVillagerHappyCircleParticles(player, location);
                break;
            case "flame":
                spawnSurroundingParticles(player, location, Particle.SMALL_FLAME);
                break;
            case "totem":
                spawnSurroundingParticles(player, location, Particle.TOTEM_OF_UNDYING);
                break;
            case "smoke":
                spawnSurroundingParticles(player, location, Particle.SMOKE);
                break;
            case "explosion":
                spawnSurroundingParticles(player, location, Particle.EXPLOSION);
                break;
            case "mycelium":
                spawnSurroundingParticles(player, location, Particle.MYCELIUM);
                break;
            default:
                // Default behavior if the particle type is unknown
                break;
        }
    }


    private void spawnSurroundingParticles(Player player, Location location, Particle particle) {
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
                        player.spawnParticle(particle,
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

    public void unbindLocation(Player p, String id, Location loc) {
        if (!interExists(p,id)) return;
        if (!isBound(loc)) {
            p.sendMessage("Theres no interaction bound on this block.");
        }
        InteractionData inter = getInteractionByID(id);
        inter.removeLocation(loc);
        interactionMap.put(id, inter);
        saveInteractionToFile(id, inter);
    }

    public void bindLocation(Player p, String id, Location loc) {
        if (!interExists(p,id)) return;
        if (isBound(loc)) {
            p.sendMessage("Theres already an Interaction bound on this block.");
            return;
        }
        InteractionData inter = getInteractionByID(id);
        inter.addLocation(loc);
        interactionMap.put(id, inter);
        saveInteractionToFile(id, inter);
    }

    public boolean isBound(Location loc) {
        for (InteractionData inter : interactionMap.values()) {
            if(inter.getLocations().contains(loc)) return true;
        }
        return false;
    }

    public boolean interExists(CommandSender c, String id) {
        for (File file : interactionFolder.listFiles()) {
            if (file.getName().equals(id+".json")) return true;
        }
        c.sendMessage("Interaction \""+id+"\" not found.");
        return false;
    }

    public InteractionData getInteractionByID(String id) {
        return interactionMap.get(id);
    }


    public void spawnVillagerHappyParticles(Player player, Location location) {
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
                        player.spawnParticle(Particle.HAPPY_VILLAGER,
                                location.getX() + randomX,
                                location.getY() + randomY,
                                location.getZ() + randomZ,
                                1);
                    }
                }
            }
        }
    }

    public void spawnVillagerHappyCircleParticles(Player player, Location location) {
        int count = 15; // Total number of particles
        double radius = 0.5; // Distance from the center of the block to spawn particles

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2; // Random angle for circular distribution
            double yOffset = Math.random() * 0.5; // Randomly offset particles a little above and below the center
            double xOffset = radius * Math.cos(angle)+0.5; // Calculate x offset
            double zOffset = radius * Math.sin(angle)+0.5; // Calculate z offset

            // Spawn the particle around the block, using the center of the block
            player.spawnParticle(Particle.HAPPY_VILLAGER, location.getX() + xOffset, location.getY() + yOffset, location.getZ() + zOffset, 1);
        }
    }

    public void sendRedstoneParticleToPlayer(Player player, Location location, InteractionData interactionData) {
        // Ensure the player meets the conditions for the interaction
        String colorHex = interactionData.getParticleColor();
        if (conditionHandler.validateConditionsNoActions(player, interactionData.getConditions())) {
            colorHex = interactionData.getParticleColor();

        } else {
            if (interactionData.getElseParticleColor() != null) {
                colorHex = interactionData.getElseParticleColor();
            } else {
                return;
            }
        }

        // Get the color from the interaction data (assumed to be in hex format)

        Color color = Color.fromRGB(
                Integer.valueOf(colorHex.substring(1, 3), 16),
                Integer.valueOf(colorHex.substring(3, 5), 16),
                Integer.valueOf(colorHex.substring(5, 7), 16)
        );

        // Create the DustOptions for the redstone particle
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);

        // Spawn the redstone particles around the location for the player
        double radius = 2.5; // The radius in which particles will spawn around the target location
        int countPerLayer = 1; // Number of particles to spawn per layer
        double offset = 0.28; // The offset for randomness (to make the particles more spread out)

        // Loop to create a cuboid shape around the block
        for (int x = -1; x <= 1; x++) { // X dimension
            for (int z = -1; z <= 1; z++) { // Z dimension
                for (int y = 0; y <= 1; y++) { // Y dimension, only the top and middle
                    for (int i = 0; i < countPerLayer; i++) {
                        // Randomize the position a bit around the location to create a spread
                        double randomX = radius * (Math.random() * offset * 2) - offset + 0.1; // Random x offset within the range
                        double randomZ = radius * (Math.random() * offset * 1.9) - offset + 0.1; // Random z offset within the range
                        double randomY = radius * Math.random() * 0.4; // Random y offset to create vertical variation

                        // Spawn the particle at the randomized position relative to the location
                        player.spawnParticle(Particle.DUST,
                                location.getX() + randomX,
                                location.getY() + randomY,
                                location.getZ() + randomZ,
                                1, dustOptions);
                    }
                }
            }
        }
    }

    public void spawnRedstoneParticleCircle(Player player, Location location, InteractionData inter) {

        String colorHex = inter.getParticleColor();
        if (conditionHandler.validateConditionsNoActions(player, inter.getConditions())) {
            colorHex = inter.getParticleColor();

        } else {
            if (inter.getElseParticleColor() != null) {
                colorHex = inter.getElseParticleColor();
            } else {
                return;
            }
        }

        int count = 15; // Total number of particles
        double radius = 0.5; // Distance from the center of the block to spawn particles

        // Parse the color from the hex string
        Color color = Color.fromRGB(
                Integer.valueOf(colorHex.substring(1, 3), 16),
                Integer.valueOf(colorHex.substring(3, 5), 16),
                Integer.valueOf(colorHex.substring(5, 7), 16)
        );

        // Create the DustOptions for the redstone particle
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2; // Random angle for circular distribution
            double yOffset = Math.random() * 0.5; // Randomly offset particles a little above and below the center
            double xOffset = radius * Math.cos(angle)+0.5; // Calculate x offset
            double zOffset = radius * Math.sin(angle)+0.5; // Calculate z offset

            // Spawn the particle around the block, using the center of the block
            player.spawnParticle(Particle.DUST,
                    location.getX() + xOffset,
                    location.getY() + yOffset,
                    location.getZ() + zOffset,
                    1, dustOptions);
        }
    }


    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }


    public void saveInteractionToFile(String id, InteractionData interaction) {
        try {
            File interactionFile = new File(interactionFolder, id + ".json");
            if (!interactionFile.exists()) {
                interactionFile.createNewFile();
            }

            JSONObject json = new JSONObject();



            // Bound locations
            JSONObject bound = new JSONObject();
            JSONArray locationArray = new JSONArray();
            for (Location loc : interaction.getLocations()) {
                locationArray.add(locationToString(loc)); // Convert each location to string
            }
            bound.put("location", locationArray);

            JSONArray npcArray = new JSONArray();
            for (String npc : interaction.getNpcIDs()) {
                npcArray.add(npc);
            }
            bound.put("npc", npcArray);

            json.put("bound", bound);

            // Particles
            JSONObject particlesJson = new JSONObject();
            particlesJson.put("type", interaction.getParticleType());
            particlesJson.put("color", interaction.getParticleColor());
            json.put("particles", particlesJson);

            // Conditions
            json.put("conditions", interaction.getConditions());

            // Actions
            JSONArray actionsArray = new JSONArray();
            actionsArray.addAll(interaction.getActions());
            json.put("actions", actionsArray);

            // Write to the file
            try (FileWriter writer = new FileWriter(interactionFile)) {
                writer.write(json.toJSONString());
            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to save interaction to file: " + id);
            e.printStackTrace();
        }
    }
}
