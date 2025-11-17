package me.hektortm.woSSystems.utils.dataclasses;

public class BasicCommand {
    private final String command;
    private final String permission;
    private final String interaction;
    public BasicCommand(String command, String interaction, String permission) {
        this.command = command;
        this.permission = permission;
        this.interaction = interaction;
    }

    public String getCommand() {
        return command;
    }

    public String getInteraction() {
        return interaction;
    }
}
