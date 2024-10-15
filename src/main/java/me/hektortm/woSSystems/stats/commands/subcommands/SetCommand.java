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

import static me.hektortm.woSSystems.stats.utils.Operation.SET;
import static me.hektortm.woSSystems.stats.utils.Operation.TAKE;

public class SetCommand extends StatsSubCommand {
    private final StatsManager manager;

    public SetCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermission(sender, Permissions.STATS_SET)) return;
        if (args.length < 2 || args.length > 3) {
            Utils.error(sender, "stats", "error.usage.take");
            return;
        }



        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1].toLowerCase();
        long amount = 0;

        File statFile = new File(manager.statsFolder, id + ".yml");
        if (!statFile.exists()) {
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

        manager.modifyStat(p.getUniqueId(), id, amount, SET);
        if (sender instanceof Player) {
            Utils.successMsg3Values(sender, "stats", "set", "%player%", p.getName(),"%stat%", id, "%amount%", String.valueOf(amount));
        }
    }
}
