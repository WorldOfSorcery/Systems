package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Location;

import java.util.List;

public class Interaction {
    private final String interactionId;
    private final List<InteractionAction> actions;
    private final List<InteractionHologram> holograms;
    private final List<Location> blockLocations;
    private final List<Integer> npcIDs;

    public Interaction(String interactionId, List<InteractionAction> actions, List<InteractionHologram> holograms, List<Location> blockLocations, List<Integer> npcIDs) {
        this.interactionId = interactionId;
        this.actions = actions;
        this.holograms = holograms;
        this.blockLocations = blockLocations;
        this.npcIDs = npcIDs;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public List<InteractionAction> getActions() {
        return actions;
    }

    public List<InteractionHologram> getHolograms() {
        return holograms;
    }

    public List<Location> getBlockLocations() {
        return blockLocations;
    }

    public List<Integer> getNpcIDs() {
        return npcIDs;
    }
}