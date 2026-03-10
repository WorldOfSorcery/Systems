package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("stats")
public class Stat extends BaseEntity {

    @Column(defaultValue = "0")
    private final long max;

    @Column(defaultValue = "FALSE")
    private final boolean capped;

    public Stat(String id, long max, boolean capped) {
        super(id);
        this.max    = max;
        this.capped = capped;
    }

    public boolean getCapped() { return capped; }
    public long    getMax()    { return max; }
}
