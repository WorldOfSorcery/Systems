package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("unlockables")
public class Unlockable extends BaseEntity {

    @Column(defaultValue = "FALSE")
    private final boolean temp;

    public Unlockable(String id, boolean temp) {
        super(id);
        this.temp = temp;
    }

    /** Non-temp unlockable shorthand. */
    public Unlockable(String id) {
        this(id, false);
    }

    public boolean isTemp() { return temp; }
}
