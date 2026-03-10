package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("cooldowns")
public class Cooldown extends BaseEntity {

    @Column
    private final long duration;

    @Column(name = "start_interaction")
    private final String start_interaction;

    @Column(name = "end_interaction")
    private final String end_interaction;

    public Cooldown(String id, long duration, String start_interaction, String end_interaction) {
        super(id);
        this.duration          = duration;
        this.start_interaction = start_interaction;
        this.end_interaction   = end_interaction;
    }

    public long   getDuration()          { return duration;          }
    public String getStart_interaction() { return start_interaction; }
    public String getEnd_interaction()   { return end_interaction;   }
}
