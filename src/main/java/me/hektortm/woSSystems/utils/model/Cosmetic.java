package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Definition of a cosmetic item.
 * The composite primary key is (id, type).
 */
@Table("cosmetics")
public class Cosmetic extends BaseEntity {

    /** Second part of the composite PK alongside {@code id}. */
    @Column(primaryKey = true, notNull = true)
    private final String type;

    @Column(notNull = true)
    private final String display;

    @Column
    private final String description;

    @Column
    private final String permission;

    public Cosmetic(String id, String type, String display, String description, String permission) {
        super(id);
        this.type        = type;
        this.display     = display;
        this.description = description;
        this.permission  = permission;
    }

    public String getType()        { return type;        }
    public String getDisplay()     { return display;     }
    public String getDescription() { return description; }
    public String getPermission()  { return permission;  }
}
