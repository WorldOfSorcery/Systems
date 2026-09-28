package me.hektortm.woSSystems.systems.quests.model;

import java.util.*;

/**
 * Mutable per-player quest session state.
 *
 * <p>Lives in the {@link me.hektortm.woSSystems.systems.quests.QuestManager} cache
 * and is persisted through wos-api ({@code PUT /v1/players/{uuid}/quests/{id}}).</p>
 */
public class PlayerQuestState {

    private final UUID   uuid;
    private final String questId;
    private String status; // "active" | "completed" | "failed"

    /** Nodes the player is currently "at" (waiting to be completed). */
    private final Set<String> activeNodes;

    /** Nodes the player has already finished. */
    private final Set<String> completedNodes;

    /** Objective progress: nodeId → current count toward the required quantity. */
    private final Map<String, Integer> objectiveProgress;

    /** Quest-scoped numeric variables set by {@code variable} nodes. */
    private final Map<String, Double> variables;

    /**
     * Merge node progress: nodeId → number of branches that have already signalled.
     * When this count reaches the node's {@code branch_count}, the merge fires.
     */
    private final Map<String, Integer> mergeProgress;

    // ---- fresh state ----

    public PlayerQuestState(UUID uuid, String questId) {
        this.uuid              = uuid;
        this.questId           = questId;
        this.status            = "active";
        this.activeNodes       = new LinkedHashSet<>();
        this.completedNodes    = new LinkedHashSet<>();
        this.objectiveProgress = new HashMap<>();
        this.variables         = new HashMap<>();
        this.mergeProgress     = new HashMap<>();
    }

    // ---- reconstructed from DB ----

    public PlayerQuestState(UUID uuid, String questId, String status,
                            Set<String> activeNodes, Set<String> completedNodes,
                            Map<String, Integer> objectiveProgress,
                            Map<String, Double> variables,
                            Map<String, Integer> mergeProgress) {
        this.uuid              = uuid;
        this.questId           = questId;
        this.status            = status;
        this.activeNodes       = activeNodes;
        this.completedNodes    = completedNodes;
        this.objectiveProgress = objectiveProgress;
        this.variables         = variables;
        this.mergeProgress     = mergeProgress;
    }

    // ---- accessors ----

    public UUID   getUuid()    { return uuid;    }
    public String getQuestId() { return questId; }
    public String getStatus()  { return status;  }
    public void   setStatus(String status) { this.status = status; }

    public Set<String>          getActiveNodes()       { return activeNodes;       }
    public Set<String>          getCompletedNodes()    { return completedNodes;    }
    public Map<String, Integer> getObjectiveProgress() { return objectiveProgress; }
    public Map<String, Double>  getVariables()         { return variables;         }
    public Map<String, Integer> getMergeProgress()     { return mergeProgress;     }

    public boolean isActive()    { return "active".equals(status);    }
    public boolean isCompleted() { return "completed".equals(status); }
}
