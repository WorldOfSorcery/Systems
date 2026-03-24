package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable definition of a player stat tracked by the stats system.
 *
 * <p>Maps to the {@code stats} database table via the {@link Table} annotation.
 * When {@link #capped} is {@code true} the stat value is constrained to
 * {@link #max} by the DAO layer.</p>
 */
@Table("stats")
public class Stat extends BaseEntity {

    @Column(defaultValue = "0")
    private final long max;

    @Column(defaultValue = "FALSE")
    private final boolean capped;

    /**
     * @param id     the unique stat ID
     * @param max    the maximum allowed value (relevant only when {@code capped} is {@code true})
     * @param capped whether the stat value is capped at {@code max}
     */
    public Stat(String id, long max, boolean capped) {
        super(id);
        this.max    = max;
        this.capped = capped;
    }

    public boolean getCapped() { return capped; }
    public long    getMax()    { return max; }
}
