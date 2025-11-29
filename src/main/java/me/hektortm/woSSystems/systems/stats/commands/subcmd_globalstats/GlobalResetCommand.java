package me.hektortm.woSSystems.systems.stats.commands.subcmd_globalstats;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.hektortm.woSSystems.utils.Operations.RESET;


public class GlobalResetCommand extends SubCommand {

    private final StatsManager manager;
    public GlobalResetCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.STATS_GLOBAL_RESET;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 1 || args.length > 2) {
            Utils.info(sender, "global_stats", "error.usage.reset");
            return;
        }



        String id = args[0].toLowerCase();

        if (!manager.getGlobalStats().containsKey(id)) {
            Utils.error(sender, "global_stats", "error.not-found");
            return;
        }

        manager.modifyGlobalStat(id, 0, RESET);
        if (sender instanceof Player) {
            Utils.success(sender, "global_stats", "reset", "%stat%", id);
        }
    }
}
