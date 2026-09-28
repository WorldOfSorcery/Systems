package me.hektortm.woSSystems.systems.quests.model;

/**
 * Represents a directed edge between two nodes in a quest flowchart.
 *
 * <p>{@link #sourceHandle} is non-null for parallel branch edges
 * (e.g. {@code "branch_0"}, {@code "branch_1"}).  Edges without a
 * sourceHandle connect sequential nodes.</p>
 */
public class QuestEdge {

    private final String id;
    private final String source;
    private final String target;
    private final String sourceHandle; // null for sequential edges

    public QuestEdge(String id, String source, String target, String sourceHandle) {
        this.id           = id;
        this.source       = source;
        this.target       = target;
        this.sourceHandle = sourceHandle;
    }

    public String getId()           { return id;           }
    public String getSource()       { return source;       }
    public String getTarget()       { return target;       }
    public String getSourceHandle() { return sourceHandle; }

    /** @return {@code true} if this edge is a parallel branch edge */
    public boolean isBranch() { return sourceHandle != null; }
}
