package me.hektortm.woSSystems.utils.dataclasses;

import java.util.List;

public class InteractionAction {
    private final String interactionId;
    private final String behaviour; // "break" or "continue"
    private final String matchType;
    private final int actionId;
    private final List<String> actions;

    public InteractionAction(String interactionId, String behaviour, String matchType, int actionId, List<String> actions) {
        this.interactionId = interactionId;
        this.behaviour = behaviour;
        this.matchType = matchType;
        this.actionId = actionId;
        this.actions = actions;
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
    public int getActionId() {
        return actionId;
    }
    public List<String> getActions() {
        return actions;
    }
}