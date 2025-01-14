package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;

public class InteractionData {
    private final String id;
    private final JSONArray conditions;
    private final List<String> actions;
    private final List<Location> locations;
    private final List<String> npcIDs;

    private final String particleType;
    private final String particleColor;
    private final List<String> hologram;

    public InteractionData(String id,JSONArray conditions, List<String> actions, List<Location> locations, List<String> npcIDs, String particleType, String particleColor, List<String> hologram) {
        this.id = id;
        this.conditions = conditions;
        this.actions = actions;
        this.locations = locations;
        this.npcIDs = npcIDs;
        this.particleType = particleType;
        this.particleColor = particleColor;
        this.hologram = hologram;
    }
    public String getId() {
        return id;
    }

    public List<String> getActions() {
        return actions;
    }

    public JSONArray getConditions() {
        return conditions;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<String> getNpcIDs() {
        return npcIDs;
    }

    public String getParticleType() {
        return particleType;
    }

    public String getParticleColor() {
        return particleColor;
    }

    // Methods to add locations and NPC IDs
    public void addLocation(Location location) {
        if (location != null && !locations.contains(location)) {
            locations.add(location);
        }
    }

    public void removeLocation(Location location) {
        locations.remove(location);
    }

    public void addNpcID(String npcID) {
        if (!npcIDs.contains(npcID)) {
            npcIDs.add(npcID);
        }
    }

    public void removeNpcID(String npcID) {
        npcIDs.remove(npcID);
    }

    public List<String> getHologram() {
        return hologram != null ? hologram : Collections.emptyList();
    }

}
