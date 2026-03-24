package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable definition and current value of a server-wide global stat.
 *
 * <p>Maps to the {@code global_stats} database table.  Unlike per-player
 * stats, the accumulated {@link #value} is stored directly on the definition
 * row.  When {@link #capped} is {@code true} the value is constrained to
 * {@link #max} by the DAO layer.</p>
 */
@Table("global_stats")
public class GlobalStat extends BaseEntity {

    /** Live accumulated value — managed by StatsDAO; schema column must exist. */
    @Column(defaultValue = "0")
    private final long value;

    @Column(defaultValue = "0")
    private final long max;

    @Column(defaultValue = "FALSE")
    private final boolean capped;

    /**
     * Full constructor (used when loading value + definition together).
     *
     * @param id     the unique global stat ID
     * @param value  the current accumulated value
     * @param max    the maximum allowed value (relevant only when {@code capped} is {@code true})
     * @param capped whether the stat value is capped at {@code max}
     */
    public GlobalStat(String id, long value, long max, boolean capped) {
        super(id);
        this.value  = value;
        this.max    = max;
        this.capped = capped;
    }

    /**
     * Definition-only constructor — value defaults to 0.
     *
     * @param id     the unique global stat ID
     * @param max    the maximum allowed value
     * @param capped whether the stat value is capped at {@code max}
     */
    public GlobalStat(String id, long max, boolean capped) {
        this(id, 0L, max, capped);
    }

    public long    getValue()   { return value;  }
    public long    getMax()     { return max;    }
    public boolean getCapped()  { return capped; }
}
