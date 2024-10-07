package me.hektortm.woSSystems.stats.commands.subcommands;

import me.hektortm.woSSystems.stats.StatsManager;
import me.hektortm.woSSystems.stats.commands.StatsCommand;
import me.hektortm.woSSystems.stats.commands.StatsSubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class CreateCommand extends StatsSubCommand {

    private final StatsManager manager;

    public CreateCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return;
        }

        if(!sender.hasPermission("stats.create")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        // /stats create <ID> <Max>

        Player p = (Player) sender;
        String id = args[0].toLowerCase();
        int max = Integer.parseInt(args[1]);

        manager.addStat(p, id, max);
        Utils.successMsg2Values(sender, "stats", "create", "%stat%", id, "%max%", String.valueOf(max));

    }
}
