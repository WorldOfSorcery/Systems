package me.hektortm.woSSystems.utils.dataclasses;

import java.util.List;

public class InteractionHologram {

    private final String interactionId;
    private final int hologramID;
    private final String behaviour; // "break" or "continue"
    private final String matchType; // "all" or "one"
    private final List<String> hologram;

    public InteractionHologram(String interactionId, int hologramID, String behaviour, String matchType, List<String> hologram) {
        this.interactionId = interactionId;
        this.hologramID = hologramID;
        this.behaviour = behaviour;
        this.matchType = matchType;
        this.hologram = hologram;
    }

    public String getInteractionId() {
        return interactionId;
    }
    public int getHologramID() {
        return hologramID;
    }
    public String getBehaviour() {
        return behaviour;
    }
    public String getMatchType() {
        return matchType;
    }
    public List<String> getHologram() {
        return hologram;
    }
}
