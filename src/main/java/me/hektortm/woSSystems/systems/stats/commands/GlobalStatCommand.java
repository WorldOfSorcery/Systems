package me.hektortm.woSSystems.systems.stats.commands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.subcmd_globalstats.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GlobalStatCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final StatsManager manager;

    public GlobalStatCommand(StatsManager manager) {
        this.manager = manager;

        subCommands.put("give", new GlobalGiveCommand(manager));
        subCommands.put("take", new GlobalTakeCommand(manager));
        subCommands.put("set", new GlobalSetCommand(manager));
        subCommands.put("reset", new GlobalResetCommand(manager));
        subCommands.put("view", new GlobalViewCommand(manager));
        subCommands.put("help", new GlobalHelpCommand(this));
    }
    // TODO: GlobalStats Messages
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.info(sender, "stats", "error.usage.general", "%type%", "globalstats");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);



        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.info(sender, "stats", "error.usage.general", "%type%", "globalstats");
        }

        return true;
    }

    public void globalStatsHelp(CommandSender sender) {
        if (PermissionUtil.hasAnyPermission(sender, Permissions.STATS_GLOBAL_GIVE, Permissions.STATS_GLOBAL_TAKE, Permissions.STATS_GLOBAL_SET,
                Permissions.STATS_GLOBAL_RESET, Permissions.STATS_GLOBAL_VIEW)) {
            Utils.info(sender, "stats", "help.header", "%type%", "Global Stats");

            if (PermissionUtil.hasPermissionNoMsg(sender, Permissions.STATS_GLOBAL_GIVE))
                Utils.noPrefix(sender, "stats", "help.give", "%type%", "globalstats");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.STATS_GLOBAL_TAKE))
                Utils.noPrefix(sender, "stats", "help.take", "%type%", "globalstats");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.STATS_GLOBAL_SET))
                Utils.noPrefix(sender, "stats", "help.set", "%type%", "globalstats");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.STATS_GLOBAL_RESET))
                Utils.noPrefix(sender, "stats", "help.reset", "%type%", "globalstats");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.STATS_GLOBAL_VIEW))
                Utils.noPrefix(sender, "stats", "help.view", "%type%", "globalstats");

            Utils.noPrefix(sender, "stats", "help.help", "%type%", "globalstats");
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }
}
