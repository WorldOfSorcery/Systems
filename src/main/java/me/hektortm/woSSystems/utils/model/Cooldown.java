package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable definition of a cooldown type managed by the cooldown system.
 *
 * <p>Maps to the {@code cooldowns} database table.  Each cooldown definition
 * specifies a duration and optional interaction IDs that are triggered when the
 * cooldown is started ({@link #start_interaction}) or expires
 * ({@link #end_interaction}).  Player-specific cooldown state (expiry
 * timestamps) is stored in a separate player-data table.</p>
 */
@Table("cooldowns")
public class Cooldown extends BaseEntity {

    @Column
    private final long duration;

    @Column(name = "start_interaction")
    private final String start_interaction;

    @Column(name = "end_interaction")
    private final String end_interaction;

    /**
     * @param id                the unique cooldown definition ID
     * @param duration          the cooldown length in seconds
     * @param start_interaction the interaction ID triggered when the cooldown begins;
     *                          may be {@code null}
     * @param end_interaction   the interaction ID triggered when the cooldown expires;
     *                          may be {@code null}
     */
    public Cooldown(String id, long duration, String start_interaction, String end_interaction) {
        super(id);
        this.duration          = duration;
        this.start_interaction = start_interaction;
        this.end_interaction   = end_interaction;
    }

    public long   getDuration()          { return duration;          }
    public String getStart_interaction() { return start_interaction; }
    public String getEnd_interaction()   { return end_interaction;   }
}
