package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

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
