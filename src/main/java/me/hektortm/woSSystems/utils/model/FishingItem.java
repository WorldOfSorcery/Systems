package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

@Table("fishing")
public class FishingItem extends BaseEntity {

    /** Stored as 'citem_id' in the DB to match the existing column name. */
    @Column(name = "citem_id", notNull = true)
    private final String citem;

    /** Stored as 'catch_interaction' in the DB. */
    @Column(name = "catch_interaction")
    private final String interaction;

    @Column(notNull = true)
    private final String rarity;

    /** Comma-separated region list stored as TEXT. */
    @Column(type = "TEXT")
    private final List<String> regions;

    public FishingItem(String id, String citem, String interaction, List<String> regions, String rarity) {
        super(id);
        this.citem       = citem;
        this.interaction = interaction;
        this.regions     = regions;
        this.rarity      = rarity;
    }

    public String       getCitem()       { return citem;       }
    public String       getInteraction() { return interaction; }
    public String       getRarity()      { return rarity;      }
    public List<String> getRegions()     { return regions;     }
}
