package me.hektortm.woSSystems.systems.stats.commands.subcommands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    private final StatsManager manager;

    public ReloadCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if(!PermissionUtil.hasPermission(sender, Permissions.STATS_RELOAD_SINGLE)) return;

            String id = args[0].toLowerCase();
            manager.reloadStat(id);
            Utils.successMsg1Value(sender, "stats", "reload.single", "%id%", id);
        } else {
            if (!PermissionUtil.hasPermission(sender, Permissions.STATS_RELOAD_ALL)) return;

            manager.reloadAllStats();
            Utils.successMsg(sender, "stats", "reload.all");
        }
    }
}
