package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Table;
import org.bukkit.Location;

import java.util.List;

/**
 * The interactions table only stores the id.
 * Actions, holograms, particles, bound blocks and NPCs are loaded
 * from their own child tables via joins.
 */
@Table("interactions")
public class Interaction extends BaseEntity {

    // These are loaded via child-table joins, not stored in the interactions row.
    private final List<InteractionAction>   actions;
    private final List<InteractionHologram> holograms;
    private final List<InteractionParticles> particles;
    private final List<Location>            blockLocations;
    private final List<Integer>             npcIDs;

    public Interaction(String interactionId, List<InteractionAction> actions, List<InteractionHologram> holograms, List<InteractionParticles> particles, List<Location> blockLocations, List<Integer> npcIDs) {
        super(interactionId);
        this.actions        = actions;
        this.holograms      = holograms;
        this.particles      = particles;
        this.blockLocations = blockLocations;
        this.npcIDs         = npcIDs;
    }

    public String getInteractionId()             { return getId();         }
    public List<InteractionAction> getActions()   { return actions;         }
    public List<InteractionHologram> getHolograms(){ return holograms;     }
    public List<InteractionParticles> getParticles(){ return particles;    }
    public List<Location> getBlockLocations()    { return blockLocations;  }
    public List<Integer> getNpcIDs()             { return npcIDs;          }
}
