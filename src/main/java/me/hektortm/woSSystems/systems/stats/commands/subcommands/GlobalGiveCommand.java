package me.hektortm.woSSystems.systems.stats.commands.subcommands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

import static me.hektortm.woSSystems.systems.stats.utils.Operation.GIVE;

public class GlobalGiveCommand extends SubCommand {

    private final StatsManager manager;
    public GlobalGiveCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.STATS_GLOBAL_GIVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            Utils.error(sender, "stats", "error.usage.give");
            return;
        }

        String id = args[0].toLowerCase();
        long amount = 0L;

        if (!manager.getGlobalStats().containsKey(id)) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            Utils.error(sender, "stats", "error.invalid-amount");
            return;
        }

        if (amount < 0) {
            Utils.error(sender, "stats", "error.invalid-amount");
            return;
        }

        if (!manager.getGlobalStats().containsKey(id)) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        manager.modifyGlobalStat(id, amount, GIVE);
        if (sender instanceof Player) {
            Utils.successMsg2Values(sender, "stats", "give","%stat%", id, "%amount%", String.valueOf(amount));
        }
    }
}
