package me.hektortm.woSSystems.systems.stats.commands.subcmd_stats;

import me.hektortm.woSSystems.systems.stats.commands.StatsCommand;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubCommand {
    private final StatsCommand cmd;

    public HelpCommand(StatsCommand cmd) {
        this.cmd = cmd;
    }


    @Override
    public String getName() {
        return "help";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        cmd.statsHelp(sender);
    }
}
