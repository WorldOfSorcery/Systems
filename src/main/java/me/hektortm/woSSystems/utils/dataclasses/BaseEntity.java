package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;

/**
 * Base class for all database-backed entities.
 *
 * Every entity automatically has:
 * <ul>
 *   <li>{@code id}  — primary key (VARCHAR 255)</li>
 *   <li>{@code tag} — nullable, reserved for a future tagging system</li>
 * </ul>
 *
 * Pair with {@link me.hektortm.woSSystems.database.annotation.Table} on the
 * subclass and {@link Column} on each additional field to get automatic
 * table creation and migration via
 * {@link me.hektortm.woSSystems.database.SchemaManager#syncTable}.
 */
public abstract class BaseEntity {

    @Column(primaryKey = true, notNull = true)
    protected final String id;

    @Column
    protected final String tag;

    protected BaseEntity(String id) {
        this.id  = id;
        this.tag = null;
    }

    protected BaseEntity(String id, String tag) {
        this.id  = id;
        this.tag = tag;
    }

    public String getId()  { return id;  }
    public String getTag() { return tag; }
}
