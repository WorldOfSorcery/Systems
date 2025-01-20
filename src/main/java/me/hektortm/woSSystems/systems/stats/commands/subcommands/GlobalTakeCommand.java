package me.hektortm.woSSystems.systems.stats.commands.subcommands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

import static me.hektortm.woSSystems.systems.stats.utils.Operation.TAKE;

public class GlobalTakeCommand extends SubCommand {

    private final StatsManager manager;
    public GlobalTakeCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.STATS_GLOBAL_TAKE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            Utils.error(sender, "stats", "error.usage.take");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1].toLowerCase();
        long amount = 0L;

        if (!manager.getGlobalStats().containsKey(id)) {
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

        manager.modifyGlobalStat(id, amount, TAKE);
        if (sender instanceof Player) {
            Utils.successMsg3Values(sender, "stats", "take", "%player%", p.getName(),"%stat%", id, "%amount%", String.valueOf(amount));
        }

    }
}
