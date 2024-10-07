package me.hektortm.woSSystems.stats.commands.subcommands;

import me.hektortm.woSSystems.stats.StatsManager;
import me.hektortm.woSSystems.stats.commands.StatsSubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends StatsSubCommand {

    private final StatsManager manager;

    public ReloadCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!sender.hasPermission("stats.reload.single")) {
                Utils.error(sender, "general", "error.perms");
                return;
            }

            String id = args[0].toLowerCase();
            manager.reloadStat(id);
            Utils.successMsg1Value(sender, "stats", "reload.single", "%id%", id);
        } else {
            if (!sender.hasPermission("stats.reload.all")) {
                Utils.error(sender, "general", "error.perms");
                return;
            }

            manager.reloadAllStats();
            Utils.successMsg(sender, "stats", "reload.all");
        }
    }
}
