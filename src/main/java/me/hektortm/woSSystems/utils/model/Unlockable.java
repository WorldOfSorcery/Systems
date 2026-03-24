package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable definition of a player unlockable flag.
 *
 * <p>Maps to the {@code unlockables} database table.  An unlockable is a
 * boolean flag that can be permanently or temporarily granted to a player and
 * is checked by the condition system.  Temporary unlockables ({@link #temp}
 * {@code == true}) are stored in a separate player-data table.</p>
 */
@Table("unlockables")
public class Unlockable extends BaseEntity {

    @Column(defaultValue = "FALSE")
    private final boolean temp;

    /**
     * @param id   the unique unlockable ID
     * @param temp {@code true} if this is a temporary unlockable
     */
    public Unlockable(String id, boolean temp) {
        super(id);
        this.temp = temp;
    }

    /**
     * Non-temp unlockable shorthand.
     *
     * @param id the unique unlockable ID
     */
    public Unlockable(String id) {
        this(id, false);
    }

    public boolean isTemp() { return temp; }
}
