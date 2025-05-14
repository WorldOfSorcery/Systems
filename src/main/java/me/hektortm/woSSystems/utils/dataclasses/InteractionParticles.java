package me.hektortm.woSSystems.utils.dataclasses;

import java.util.List;

public class InteractionParticles {
    private final String interactionId;
    private final String behaviour; // "break" or "continue"
    private final String matchType; // "all" or "one"
    private final int particleId;
    private final String particle;

    public InteractionParticles(String interactionId, String behaviour, String matchType, int particleId, String particle) {
        this.interactionId = interactionId;
        this.behaviour = behaviour;
        this.matchType = matchType;
        this.particleId = particleId;
        this.particle = particle;
    }

    public String getInteractionId() {
        return interactionId;
    }
    public String getBehaviour() {
        return behaviour;
    }
    public String getMatchType() {
        return matchType;
    }
    public int getParticleId() {
        return particleId;
    }
    public String getParticle() {
        return particle;
    }

}
