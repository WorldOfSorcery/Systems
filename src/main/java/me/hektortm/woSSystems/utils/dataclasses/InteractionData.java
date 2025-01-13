package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Location;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InteractionData {
    private final String id;
    private final JSONObject conditions;
    private final List<String> actions;
    private final List<Location> locations;
    private final List<String> npcIDs;

    private final String particleType;
    private final String particleColor;
    private final String elseParticleType;
    private final String elseParticleColor;
    private final List<String> hologramDefault;
    private final List<String> hologramElse;

    public InteractionData(String id,JSONObject conditions, List<String> actions, List<Location> locations, List<String> npcIDs, String particleType, String particleColor, String elseParticleType, String elseParticleColor, List<String> hologramDefault, List<String> hologramElse) {

        this.conditions = conditions;
        this.actions = actions;
        this.locations = locations;
        this.npcIDs = npcIDs;
        this.id = id;
        this.particleType = particleType;
        this.particleColor = particleColor;
        this.elseParticleType = elseParticleType;
        this.elseParticleColor = elseParticleColor;
        this.hologramDefault = hologramDefault;
        this.hologramElse = hologramElse;
    }
    public String getId() {
        return id;
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

    public List<String> getNpcIDs() {
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
        if (!npcIDs.contains(npcID)) {
            npcIDs.add(npcID);
        }
    }

    public void removeNpcID(String npcID) {
        npcIDs.remove(npcID);
    }

    public List<String> getHologramDefault() {
        return hologramDefault != null ? hologramDefault : Collections.emptyList();
    }

    public List<String> getHologramElse() {
        return hologramElse != null ? hologramElse : Collections.emptyList();
    }

}
