package me.hektortm.woSSystems.systems.interactions.config;

import me.hektortm.woSSystems.utils.dataclasses.BoundData;
import me.hektortm.woSSystems.utils.dataclasses.ParticleData;
import java.util.List;

public class InteractionConfig {

    // Represents the "bound" section of the JSON
    private BoundData bound;

    // Particle configuration from the "particles" section
    private ParticleData particles;

    // List of actions from the "actions" section
    private List<String> actions;

    // Default constructor (required for deserialization)
    public InteractionConfig() {}

    // Constructor for manual creation
    public InteractionConfig(BoundData bound, ParticleData particles, List<String> actions) {
        this.bound = bound;
        this.particles = particles;
        this.actions = actions;
    }

    // Getters and setters
    public BoundData getBound() {
        return bound;
    }

    public void setBound(BoundData bound) {
        this.bound = bound;
    }

    public ParticleData getParticles() {
        return particles;
    }

    public void setParticles(ParticleData particles) {
        this.particles = particles;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    // Utility methods

    /**
     * Retrieves the list of bound block locations.
     */
    public List<String> getBoundBlocks() {
        return bound != null ? bound.getLocation() : null;
    }

    /**
     * Retrieves the list of bound NPC IDs.
     */
    public List<String> getBoundNpcs() {
        return bound != null ? bound.getNpc() : null;
    }

    /**
     * Checks if this interaction has particles enabled.
     */
    public boolean hasParticle() {
        return particles != null && particles.getType() != null;
    }

    /**
     * Retrieves the type of particles for this interaction.
     */
    public String getParticleType() {
        return particles != null ? particles.getType() : null;
    }

    /**
     * Retrieves the color of the particles, if applicable.
     */
    public String getParticleColor() {
        return particles != null ? particles.getColor() : null;
    }
}