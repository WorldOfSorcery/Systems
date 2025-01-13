package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
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

                List<String> validNpcIds = new ArrayList<>();
                if (npcArray != null) {
                    for (Object npcId : npcArray) {
                        if (npcId instanceof String) {
                            int id = Integer.parseInt((String) npcId);
                            if (CitizensAPI.getNPCRegistry().getById(id) != null) { // Check if NPC exists
                                validNpcIds.add((String) npcId);
                            } else {
                                Bukkit.getLogger().info("Removed invalid NPC ID: " + npcId + " from interaction " + interactionId);
                            }
                        }
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

                interactionMap.put(interactionId, new InteractionData(conditions,actions,locations,validNpcIds,particleType,particleColor, elseType, elseColor));

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
        ParticleHandler particleHandler = new ParticleHandler();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (InteractionData inter : interactionMap.values()) {
                    for (Location location : inter.getLocations()) {
                        if (location != null) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                // Check if player meets conditions
                                    particleHandler.spawnParticlesForPlayer(player, inter, location);
                            }
                        }
                    }
                    for (String id : inter.getNpcIDs()) {
                        if (id != null) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                NPC npc1 = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(id));
                                Location location = npc1.getEntity().getLocation();
                                particleHandler.spawnParticlesForPlayer(player, inter, location);
                            }
                        }
                    }

                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
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
        p.sendMessage("Unbound interaction '"+id);
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
        p.sendMessage("Bound interaction '"+id);
    }

    public void bindNPC(Player p, String id, int npcID) {
        String npcIDString = String.valueOf(npcID);
        if(!interExists(p,id)) return;
        if (isNPCBound(npcID)) {
            p.sendMessage("error isBound");
            return;
        }
        InteractionData inter = getInteractionByID(id);
        inter.addNpcID(npcIDString);
        interactionMap.put(id, inter);
        saveInteractionToFile(id, inter);
        p.sendMessage("Bound interaction '"+id+"' to NPC '"+npcID+"'");
    }
    public void unbindNPC(Player p, String id, int npcID) {
        String npcIDString = String.valueOf(npcID);
        if (!interExists(p,id)) return;
        if (!isNPCBound(npcID)) {
            p.sendMessage("Theres no interaction bound on this NPC.");
        }
        InteractionData inter = getInteractionByID(id);
        inter.removeNpcID(npcIDString);
        interactionMap.put(id, inter);
        saveInteractionToFile(id, inter);
        p.sendMessage("Unbound interaction '"+id+"' from NPC '"+npcID+"'");
    }

    public boolean isNPCBound(int id) {
        for (InteractionData inter : interactionMap.values()) {
            if(inter.getNpcIDs().contains(id)) return true;
        }
        return false;
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
