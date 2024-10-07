package me.hektortm.woSSystems.interactions.commands.subcommands;


import me.hektortm.woSSystems.interactions.commands.InteractionCommand;
import me.hektortm.woSSystems.interactions.commands.InterSubCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand extends InterSubCommand {

    private final InteractionCommand command;

    public HelpCommand(InteractionCommand command) {
        this.command = command;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        command.sendHelp(sender);
    }
}
