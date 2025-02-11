package me.hektortm.woSSystems.systems.interactions;

import com.maximde.hologramlib.hologram.Hologram;
import com.maximde.hologramlib.hologram.ItemHologram;
import com.maximde.hologramlib.hologram.RenderMode;
import com.maximde.hologramlib.hologram.TextHologram;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
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
    private final ConditionHandler newConditionHandler = plugin.getConditionHandler();
    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)), WoSCore.getPlugin(WoSCore.class));
    private PlaceholderResolver resolver;
    private ConditionHandler conditionHandler;
    public final Map<String, InteractionData> interactionMap = new HashMap<>();
    private final Map<String, TextHologram> hologramMap = new HashMap<>();

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
        if (interactionFiles == null || interactionFiles.length == 0) {
            log.sendWarning("No valid interactions loaded. Check JSON structure in " + interactionFolder.getPath());
            return;
        }

        for (File file : interactionFiles) {
            String interactionId = file.getName().replace(".json", "");
            try (FileReader reader = new FileReader(file)) {
                JSONObject json = (JSONObject) new JSONParser().parse(reader);

                // Parse bound locations and NPCs
                JSONObject bound = (JSONObject) json.get("bound");
                JSONArray locationArray = (JSONArray) bound.get("block");
                JSONArray npcArray = (JSONArray) bound.get("npc");

                List<Location> locations = new ArrayList<>();
                if (locationArray != null) {
                    for (Object location : locationArray) {
                        locations.add(Parsers.parseLocation((String) location));
                    }
                }

                List<String> validNpcIds = new ArrayList<>();
                if (npcArray != null) {
                    for (Object npcId : npcArray) {
                        validNpcIds.add((String) npcId);
                    }
                }

                // Parse particles, npc, hologram, and actions
                Map<String, JSONObject> particles = parseSection(json, "particles");
                Map<String, JSONObject> npc = parseSection(json, "npc");
                Map<String, JSONObject> hologram = parseSection(json, "hologram");
                Map<String, JSONObject> actions = parseSection(json, "actions");

                interactionMap.put(interactionId, new InteractionData(interactionId, particles, npc, hologram, actions, locations, validNpcIds));

            } catch (Exception e) {
                Bukkit.getLogger().warning("Error loading interaction from file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Map<String, JSONObject> parseSection(JSONObject json, String sectionName) {
        Map<String, JSONObject> sectionMap = new HashMap<>();
        JSONObject section = (JSONObject) json.get(sectionName);
        if (section != null) {
            for (Object key : section.keySet()) {
                String keyStr = (String) key;
                sectionMap.put(keyStr, (JSONObject) section.get(keyStr));
            }
        }
        return sectionMap;
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

        if(inter.getActions() == null) return;
        List<String> actions;
        // proritize conditions
        ConditionHandler.ConditionOutcomes unMetOutcomes = newConditionHandler.getUnmetConditionOutcomes(p, inter.getConditions());
        if (unMetOutcomes != null) {
            actions = unMetOutcomes.actions;
        } else {
             actions = inter.getActions();
        }

        for (String action : actions) {
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
                                particleHandler.spawnParticlesForPlayer(player, inter, location, false);

                                //spawnTextDisplay(location, inter, null, false);
                                //updateTextDisplay(location, inter, null, false);
                            }
                        }
                    }
                    for (String id : inter.getNpcIDs()) {
                        if (id != null) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                NPC npc1 = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(id));
                                Location location = npc1.getEntity().getLocation().getBlock().getLocation();
                                particleHandler.spawnParticlesForPlayer(player, inter, npc1.getEntity().getLocation(), true);
                                //spawnTextDisplay(location, inter, id, true);
                                //updateTextDisplay(location, inter, id, true);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void removeTextDisplays() {
        for (Hologram holo : hologramMap.values()) {
            String id = holo.getId();
            plugin.getHologramManager().remove(id);
        }
    }

    public void spawnTextDisplay(Location location, InteractionData inter, String npcID, boolean npc) {

        Location newL = location.clone().add(0.5, 2, 0.5);

        String interId = inter.getId();
        String id;
        if (npc) {
            id = interId+":npc_"+npcID;
        } else {
            //placeholder for now
            id = interId+":loc_"+location.toString();
            newL = location.clone().add(0.5, 1.2, 0.5);
        }
        if (!hologramMap.containsKey(id)) {
            createDisplay(id, newL);
        } else {
            Location loc = hologramMap.get(id).getLocation();
            if (!Objects.equals(loc.toString(), newL.toString())) {
                plugin.getHologramManager().remove(id);
                hologramMap.remove(id);
                createDisplay(id, newL);
            }
        }
    }

    private void createDisplay(String id, Location loc) {
        TextHologram textHologram = new TextHologram(id, RenderMode.ALL, (player1, textDisplayMeta) ->{
            /*
            String[] parts = id.split(":");
            String interactionID = parts[0];
            InteractionData interaction = getInteractionByID(interactionID);
            ConditionHandler.ConditionOutcomes outcomes = conditionHandler.getUnmetConditionOutcomes(player1, interaction.getConditions());
            List<String> lines = interaction.getHologram();
            if (outcomes.holograms != null) {
                lines = outcomes.holograms;

            }
            String combinedText = String.join("\n", lines);
            Component component = Component.text(combinedText);
            textDisplayMeta.setText(component);

             */
            return textDisplayMeta;
        })
                .setBillboard(Display.Billboard.VERTICAL);


        plugin.getHologramManager().spawn(textHologram, loc);
        hologramMap.put(id, textHologram);
    }
    private void updateTextDisplay(Location location, InteractionData inter, String npcID, boolean npc) {

        String interId = inter.getId();
        String id;
        if (npc) {
            id = interId+":npc_"+npcID;
        } else {
            //placeholder for now
            id = interId+":loc_"+location.toString();
        }
        if (hologramMap.containsKey(id)) {
            TextHologram holo = hologramMap.get(id);
            holo.update();
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
                locationArray.add(Parsers.locationToString(loc)); // Convert each location to string
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

            // Hologram

            json.put("hologram", interaction.getHologram());


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
    private JSONArray listToJsonArray(List<String> lines) {
        JSONArray jsonArray = new JSONArray();
        if (lines != null) {
            for (String line : lines) {
                jsonArray.add(line); // Add each line to the JSONArray
            }
        }
        return jsonArray;
    }

}
