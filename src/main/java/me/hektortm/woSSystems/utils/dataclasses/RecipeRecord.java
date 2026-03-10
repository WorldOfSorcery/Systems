package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * DB-layer representation of a crafting recipe row.
 * The richer in-memory object is {@link CRecipe}.
 */
@Table("recipes")
public class RecipeRecord extends BaseEntity {

    @Column
    private final String type;

    @Column(type = "TEXT")
    private final String slots;

    @Column
    private final String output;

    @Column
    private final String success;

    public RecipeRecord(String id, String type, String slots, String output, String success) {
        super(id);
        this.type    = type;
        this.slots   = slots;
        this.output  = output;
        this.success = success;
    }

    public String getType()    { return type;    }
    public String getSlots()   { return slots;   }
    public String getOutput()  { return output;  }
    public String getSuccess() { return success; }
}
