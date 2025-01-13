package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Location;
import org.json.simple.JSONObject;

import java.util.List;

public class InteractionData {
    private final JSONObject conditions;
    private final List<String> actions;
    private final List<Location> locations;
    private final List<Integer> npcIDs;
    private final String particleType;
    private final String particleColor;
    private final String elseParticleType;
    private final String elseParticleColor;

    public InteractionData(JSONObject conditions, List<String> actions, List<Location> locations, List<Integer> npcIDs, String particleType, String particleColor, String elseParticleType, String elseParticleColor) {

        this.conditions = conditions;
        this.actions = actions;
        this.locations = locations;
        this.npcIDs = npcIDs;
        this.particleType = particleType;
        this.particleColor = particleColor;
        this.elseParticleType = elseParticleType;
        this.elseParticleColor = elseParticleColor;
    }


    public List<String> getActions() {
        return actions;
    }

    public JSONObject getConditions() {
        return conditions;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<Integer> getNpcIDs() {
        return npcIDs;
    }

    public String getParticleType() {
        return particleType;
    }

    public String getParticleColor() {
        return particleColor;
    }

    public String getElseParticleType() {
        return elseParticleType;
    }
    public String getElseParticleColor() {
        return elseParticleColor;
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
        if (npcID != null && !npcIDs.contains(npcID)) {
            npcIDs.add(npcID);
        }
    }

    public void removeNpcID(String npcID) {
        npcIDs.remove(npcID);
    }
}
