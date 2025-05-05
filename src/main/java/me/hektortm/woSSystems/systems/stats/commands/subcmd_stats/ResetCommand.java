package me.hektortm.woSSystems.systems.stats.commands.subcmd_stats;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.hektortm.woSSystems.utils.Operations.RESET;


public class ResetCommand extends SubCommand {

    private final StatsManager manager;
    public ResetCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.STATS_RESET;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 1 || args.length > 2) {
            Utils.info(sender, "stats", "error.usage.reset", "%type%", "stats");
            return;
        }



        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1].toLowerCase();

        if (!manager.getStats().containsKey(id)) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        if (!manager.getStats().containsKey(id)) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        manager.modifyStat(p.getUniqueId(), id, 0, RESET);
        if (sender instanceof Player) {
            Utils.success(sender, "stats", "reset", "%player%", p.getName(),"%stat%", id);
        }
    }
}
