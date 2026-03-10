package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

@Table("inter_holograms")
public class InteractionHologram {

    /** FK → interactions.id */
    @Column(name = "interaction_id", primaryKey = true, notNull = true)
    private final String interactionId;

    @Column(name = "hologram_id")
    private final int hologramID;

    @Column(notNull = true)
    private final String behaviour; // "break" or "continue"

    @Column(name = "matchtype", notNull = true)
    private final String matchType; // "all" or "one"

    @Column(type = "TEXT", notNull = true)
    private final List<String> hologram;

    public InteractionHologram(String interactionId, int hologramID, String behaviour, String matchType, List<String> hologram) {
        this.interactionId = interactionId;
        this.hologramID    = hologramID;
        this.behaviour     = behaviour;
        this.matchType     = matchType;
        this.hologram      = hologram;
    }

    public String getInteractionId()   { return interactionId; }
    public int getHologramID()         { return hologramID;    }
    public String getBehaviour()       { return behaviour;     }
    public String getMatchType()       { return matchType;     }
    public List<String> getHologram()  { return hologram;      }
}
