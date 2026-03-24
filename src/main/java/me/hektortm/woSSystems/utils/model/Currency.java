package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable definition of an in-game currency managed by the economy system.
 *
 * <p>Maps to the {@code currencies} database table.  Player balances are stored
 * in a separate player-data table and accessed via
 * {@link me.hektortm.woSSystems.database.dao.EconomyDAO}.</p>
 */
@Table("currencies")
public class Currency extends BaseEntity {

    @Column(notNull = true)
    private final String name;

    @Column(name = "short_name", notNull = true)
    private final String shortName;

    @Column
    private final String icon;

    @Column
    private final String color;

    @Column(name = "hidden_if_zero", defaultValue = "FALSE")
    private final boolean hiddenIfZero;

    /**
     * @param id           the unique currency ID
     * @param name         the display name
     * @param shortName    the abbreviated display name
     * @param icon         an optional icon string (e.g. a Unicode symbol or item key)
     * @param color        an optional colour code for rendering the currency
     * @param hiddenIfZero when {@code true}, the currency is omitted from UI if the balance is zero
     */
    public Currency(String id, String name, String shortName, String icon, String color, boolean hiddenIfZero) {
        super(id);
        this.name         = name;
        this.shortName    = shortName;
        this.icon         = icon;
        this.color        = color;
        this.hiddenIfZero = hiddenIfZero;
    }

    public String  getName()        { return name;         }
    public String  getShortName()   { return shortName;    }
    public String  getIcon()        { return icon;         }
    public String  getColor()       { return color;        }
    public boolean isHiddenIfZero() { return hiddenIfZero; }
}
