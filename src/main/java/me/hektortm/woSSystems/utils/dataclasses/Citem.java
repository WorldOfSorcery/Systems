package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("citems")
public class Citem extends BaseEntity {

    @Column(name = "data", type = "JSON")
    private final String data;

    protected Citem(String id, String data) {
        super(id);
        this.data = data;
    }
}
