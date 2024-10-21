package me.hektortm.woSSystems.systems.stats.commands.subcommands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.StatsSubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.io.File;

public class ViewCommand extends StatsSubCommand {

    private final StatsManager manager;

    public ViewCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "view";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("stats.view")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }
        if (args.length < 2 || args.length > 3) {
            Utils.error(sender, "stats", "error.usage.take");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1];

        File statFile = new File(manager.statsFolder, id + ".yml");
        if (!statFile.exists()) {
            Utils.error(sender, "stats", "error.not-found");
            return;
        }

        long value = manager.getPlayerStat(p.getUniqueId(), id);

        Utils.successMsg3Values(sender, "stats", "view", "%player%", p.getName(), "%value%", String.valueOf(value), "%id%", id);

    }
}
