package me.hektortm.woSSystems.systems.quests.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable quest definition parsed from the {@code quests} database table.
 *
 * <p>Provides the flow-graph helpers needed by
 * {@link me.hektortm.woSSystems.systems.quests.QuestManager} to traverse the
 * ReactFlow flowchart at runtime.</p>
 */
public class Quest {

    private final String id;
    private final String title;
    private final String description;
    private final Map<String, QuestNode> nodes; // nodeId → node
    private final List<QuestEdge> edges;

    public Quest(String id, String title, String description,
                 List<QuestNode> nodes, List<QuestEdge> edges) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.edges       = edges;

        this.nodes = new LinkedHashMap<>();
        for (QuestNode n : nodes) this.nodes.put(n.getId(), n);
    }

    public String getId()          { return id;          }
    public String getTitle()       { return title;       }
    public String getDescription() { return description; }

    public QuestNode             getNode(String nodeId) { return nodes.get(nodeId); }
    public Collection<QuestNode> getAllNodes()          { return nodes.values();    }

    /** @return the first node with type {@code "start"}, or {@code null} */
    public QuestNode getStartNode() {
        for (QuestNode n : nodes.values()) {
            if ("start".equals(n.getType())) return n;
        }
        return null;
    }

    /** All outgoing edges from {@code nodeId}. */
    public List<QuestEdge> getEdgesFrom(String nodeId) {
        List<QuestEdge> result = new ArrayList<>();
        for (QuestEdge e : edges) {
            if (nodeId.equals(e.getSource())) result.add(e);
        }
        return result;
    }

    /**
     * Returns the target node IDs for edges leaving {@code nodeId} whose
     * {@code sourceHandle} matches {@code handle}.
     *
     * <ul>
     *   <li>{@code handle == null} — edges <em>without</em> a sourceHandle
     *       (normal sequential flow)</li>
     *   <li>{@code handle != null} — edges whose sourceHandle equals the value
     *       (condition YES/NO, check_variable TRUE/FALSE/ELSE, parallel branch_N)</li>
     * </ul>
     */
    public List<String> getNextNodeIds(String nodeId, String handle) {
        List<String> result = new ArrayList<>();
        for (QuestEdge e : edges) {
            if (!nodeId.equals(e.getSource())) continue;
            boolean matches = handle == null
                    ? e.getSourceHandle() == null
                    : handle.equals(e.getSourceHandle());
            if (matches) result.add(e.getTarget());
        }
        return result;
    }

    /**
     * Convenience overload — follows edges without a sourceHandle
     * (normal sequential flow).
     */
    public List<String> getNextNodeIds(String nodeId) {
        return getNextNodeIds(nodeId, null);
    }

    /**
     * Branch edges leaving {@code nodeId} (those with a non-null sourceHandle).
     * Used by {@code parallel} nodes to activate all branches simultaneously.
     */
    public List<QuestEdge> getBranchEdges(String nodeId) {
        List<QuestEdge> result = new ArrayList<>();
        for (QuestEdge e : edges) {
            if (nodeId.equals(e.getSource()) && e.isBranch()) result.add(e);
        }
        return result;
    }
}
