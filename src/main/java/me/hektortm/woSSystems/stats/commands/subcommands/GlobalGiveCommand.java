package me.hektortm.woSSystems.stats.commands.subcommands;

import me.hektortm.woSSystems.stats.StatsManager;
import me.hektortm.woSSystems.stats.commands.StatsSubCommand;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

import static me.hektortm.woSSystems.stats.utils.Operation.GIVE;

public class GlobalGiveCommand extends StatsSubCommand {

    private final StatsManager manager;
    public GlobalGiveCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermission(sender, Permissions.STATS_GLOBAL_GIVE)) return;


        if (args.length < 2 || args.length > 3) {
            Utils.error(sender, "stats", "error.usage.give");
            return;
        }

        String id = args[0].toLowerCase();
        long amount = 0L;

        File statFile = new File(manager.globalStatsFolder, id + ".yml");
        if (!statFile.exists()) {
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

        if (!manager.getStats().containsKey(id)) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        manager.modifyGlobalStat(id, amount, GIVE);
        if (sender instanceof Player) {
            Utils.successMsg2Values(sender, "stats", "give","%stat%", id, "%amount%", String.valueOf(amount));
        }
    }
}
