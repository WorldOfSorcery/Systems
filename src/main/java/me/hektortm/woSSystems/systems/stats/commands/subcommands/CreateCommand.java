package me.hektortm.woSSystems.systems.stats.commands.subcommands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.StatsSubCommand;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (!PermissionUtil.hasPermission(sender, Permissions.STATS_CREATE)) return;

        if (args.length == 0) {
            Utils.error(sender, "stats", "usage");
        }

        // /stats create <ID> <Max>

        Player p = (Player) sender;
        String id = args[0].toLowerCase();
        boolean capped = Boolean.parseBoolean(args[1]);
        long max = 0L;
        try {
            max = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            Utils.error(sender, "general", "error.invalidnumber");
        }



        manager.addStat(p, id, max, capped, false);
        Utils.successMsg2Values(sender, "stats", "create", "%stat%", id, "%max%", String.valueOf(max));

    }
}
