package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("global_stats")
public class GlobalStat extends BaseEntity {

    /** Live accumulated value — managed by StatsDAO; schema column must exist. */
    @Column(defaultValue = "0")
    private final long value;

    @Column(defaultValue = "0")
    private final long max;

    @Column(defaultValue = "FALSE")
    private final boolean capped;

    /** Full constructor (used when loading value + definition together). */
    public GlobalStat(String id, long value, long max, boolean capped) {
        super(id);
        this.value  = value;
        this.max    = max;
        this.capped = capped;
    }

    /** Definition-only constructor — value defaults to 0. */
    public GlobalStat(String id, long max, boolean capped) {
        this(id, 0L, max, capped);
    }

    public long    getValue()   { return value;  }
    public long    getMax()     { return max;    }
    public boolean getCapped()  { return capped; }
}
