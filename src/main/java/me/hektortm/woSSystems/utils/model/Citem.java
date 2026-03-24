package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable record of a custom item (citem) stored in the database.
 *
 * <p>Maps to the {@code citems} database table.  The full
 * {@link org.bukkit.inventory.ItemStack} is serialised to JSON and persisted in
 * the {@link #data} column; deserialisation is handled by
 * {@link me.hektortm.woSSystems.database.dao.CitemDAO}.</p>
 */
@Table("citems")
public class Citem extends BaseEntity {

    @Column(name = "data", type = "JSON")
    private final String data;

    /**
     * @param id   the unique citem ID
     * @param data the JSON-serialised {@link org.bukkit.inventory.ItemStack}
     */
    protected Citem(String id, String data) {
        super(id);
        this.data = data;
    }
}
