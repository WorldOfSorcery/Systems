package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

@Table("inter_actions")
public class InteractionAction {

    /** FK → interactions.id */
    @Column(name = "id", primaryKey = true, notNull = true)
    private final String interactionId;

    @Column(notNull = true)
    private final String behaviour; // "break" or "continue"

    @Column(name = "matchtype", notNull = true)
    private final String matchType;

    @Column(name = "action_id", primaryKey = true)
    private final int actionId;

    @Column(type = "TEXT", notNull = true)
    private final List<String> actions;

    public InteractionAction(String interactionId, String behaviour, String matchType, int actionId, List<String> actions) {
        this.interactionId = interactionId;
        this.behaviour     = behaviour;
        this.matchType     = matchType;
        this.actionId      = actionId;
        this.actions       = actions;
    }

    public String getInteractionId() { return interactionId; }
    public String getBehaviour()     { return behaviour;     }
    public String getMatchType()     { return matchType;     }
    public int getActionId()         { return actionId;      }
    public List<String> getActions() { return actions;       }
}
