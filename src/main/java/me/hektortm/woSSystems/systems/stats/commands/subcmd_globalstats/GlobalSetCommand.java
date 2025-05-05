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
            Utils.info(sender, "stats", "error.usage.take", "%type%", "globalstats");
            return;
        }



        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1].toLowerCase();
        long amount = 0;

        if (!manager.getStats().containsKey(id)) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            Utils.error(sender, "stats", "error.invalid-amount");
            return;
        }

        if (amount < 0) {
            Utils.error(sender, "stats", "error.invalid-amount");
            return;
        }

        if (!manager.getStats().containsKey(id)) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        manager.modifyGlobalStat(id, amount, SET);
        if (sender instanceof Player) {
            Utils.success(sender, "stats", "global.set", "%player%", p.getName(),"%stat%", id, "%amount%", String.valueOf(amount));
        }
    }
}
