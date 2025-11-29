package me.hektortm.woSSystems.systems.stats.commands.subcmd_globalstats;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.hektortm.woSSystems.utils.Operations.SET;


public class GlobalSetCommand extends SubCommand {
    private final StatsManager manager;

    public GlobalSetCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.STATS_GLOBAL_SET;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            Utils.info(sender, "global_stats", "error.usage.take");
            return;
        }



        String id = args[0].toLowerCase();
        long amount = 0;

        if (!manager.getGlobalStats().containsKey(id)) {
            Utils.error(sender, "global_stats", "error.not-found");
            return;
        }

        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            Utils.error(sender, "global_stats", "error.invalid-amount");
            return;
        }

        if (amount < 0) {
            Utils.error(sender, "global_stats", "error.invalid-amount");
            return;
        }

        manager.modifyGlobalStat(id, amount, SET);
        if (sender instanceof Player) {
            Utils.success(sender, "global_stats", "set","%stat%", id, "%amount%", String.valueOf(amount));
        }
    }
}
