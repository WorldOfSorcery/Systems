package me.hektortm.woSSystems.systems.interactions.commands.subcommands;


import me.hektortm.woSSystems.systems.interactions.commands.InteractionCommand;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubCommand {

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
