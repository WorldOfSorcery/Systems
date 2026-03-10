package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("constants")
public class Constant extends BaseEntity {

    @Column(notNull = true)
    private final String value;

    public Constant(String id, String value) {
        super(id);
        this.value = value;
    }

    public String getValue() { return value; }
}
