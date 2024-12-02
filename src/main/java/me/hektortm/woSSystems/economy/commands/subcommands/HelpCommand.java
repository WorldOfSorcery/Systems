package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.economy.commands.EcoCommand;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubCommand {

    private final EcoCommand cmd;
    public HelpCommand(EcoCommand cmd) {
        this.cmd = cmd;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        cmd.ecoHelp(sender);
    }
}
