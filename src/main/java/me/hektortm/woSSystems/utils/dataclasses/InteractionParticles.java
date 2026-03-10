package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("inter_particles")
public class InteractionParticles {

    /** FK → interactions.id */
    @Column(name = "id", primaryKey = true, notNull = true)
    private final String interactionId;

    @Column(notNull = true)
    private final String behaviour; // "break" or "continue"

    @Column(name = "matchtype", notNull = true)
    private final String matchType; // "all" or "one"

    @Column(name = "particle_id", notNull = true)
    private final int particleId;

    @Column(notNull = true)
    private final String particle;

    @Column(name = "particle_color")
    private final String particleColor;

    public InteractionParticles(String interactionId, String behaviour, String matchType, int particleId, String particle, String particleColor) {
        this.interactionId = interactionId;
        this.behaviour     = behaviour;
        this.matchType     = matchType;
        this.particleId    = particleId;
        this.particle      = particle;
        this.particleColor = particleColor;
    }

    public String getInteractionId() { return interactionId; }
    public String getBehaviour()     { return behaviour;     }
    public String getMatchType()     { return matchType;     }
    public int getParticleId()       { return particleId;    }
    public String getParticle()      { return particle;      }
    public String getParticleColor() { return particleColor; }
}
