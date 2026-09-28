package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable definition of a database-driven command registered at runtime.
 *
 * <p>Maps to the {@code commands} database table.  Each record binds a
 * command label to an interaction ID so that when the command is executed the
 * corresponding interaction is triggered via
 * {@link me.hektortm.woSSystems.systems.interactions.InteractionManager}.
 * An optional permission node gates access to the command.</p>
 */
@Table("commands")
public class BasicCommand {

    @Column(name = "command", primaryKey = true, notNull = true)
    private final String command;

    @Column
    private final String permission;

    @Column(notNull = true)
    private final String interaction;

    /**
     * @param command     the command label (without the leading {@code /})
     * @param interaction the interaction ID to trigger when the command is run
     * @param permission  the optional permission node required to use the command;
     *                    may be {@code null} for public commands
     */
    public BasicCommand(String command, String interaction, String permission) {
        this.command     = command;
        this.permission  = permission;
        this.interaction = interaction;
    }

    public String getCommand()     { return command;     }
    public String getInteraction() { return interaction; }
    public String getPermission()  { return permission;  }
}
