package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Represents a quest definition row in the {@code quests} table.
 *
 * <p>The {@code quests} table is authored by the admin portal; this model lets
 * {@link me.hektortm.woSSystems.database.SchemaManager} create the table if it
 * does not yet exist and add any missing columns on startup.</p>
 */
@Table("quests")
public class QuestDefinition extends BaseEntity {

    @Column
    private final String title;

    @Column(type = "TEXT")
    private final String description;

    @Column(type = "JSON")
    private final String nodes;

    @Column(type = "JSON")
    private final String edges;

    public QuestDefinition(String id, String title, String description,
                           String nodes, String edges) {
        super(id);
        this.title       = title;
        this.description = description;
        this.nodes       = nodes;
        this.edges       = edges;
    }

    public String getTitle()       { return title;       }
    public String getDescription() { return description; }
    public String getNodes()       { return nodes;       }
    public String getEdges()       { return edges;       }
}
