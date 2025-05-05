package me.hektortm.woSSystems.systems.stats.commands.subcmd_globalstats;

import me.hektortm.woSSystems.systems.stats.commands.GlobalStatCommand;
import me.hektortm.woSSystems.systems.stats.commands.StatsCommand;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class GlobalHelpCommand extends SubCommand {
    private final GlobalStatCommand cmd;

    public GlobalHelpCommand(GlobalStatCommand cmd) {
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
        cmd.globalStatsHelp(sender);
    }
}
