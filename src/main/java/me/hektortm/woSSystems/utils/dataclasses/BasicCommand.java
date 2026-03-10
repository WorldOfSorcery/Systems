package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("commands")
public class BasicCommand {

    @Column(name = "command", primaryKey = true, notNull = true)
    private final String command;

    @Column
    private final String permission;

    @Column(notNull = true)
    private final String interaction;

    public BasicCommand(String command, String interaction, String permission) {
        this.command     = command;
        this.permission  = permission;
        this.interaction = interaction;
    }

    public String getCommand()     { return command;     }
    public String getInteraction() { return interaction; }
    public String getPermission()  { return permission;  }
}
