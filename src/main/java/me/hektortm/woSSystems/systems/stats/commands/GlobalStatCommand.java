package me.hektortm.woSSystems.systems.stats.commands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.subcommands.GlobalCreateCommand;
import me.hektortm.woSSystems.systems.stats.commands.subcommands.GlobalDeleteCommand;
import me.hektortm.woSSystems.systems.stats.commands.subcommands.GlobalGiveCommand;
import me.hektortm.woSSystems.systems.stats.commands.subcommands.GlobalTakeCommand;
import me.hektortm.woSSystems.utils.PermissionUtil;
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

        subCommands.put("create", new GlobalCreateCommand(manager));
        subCommands.put("give", new GlobalGiveCommand(manager));
        subCommands.put("delete", new GlobalDeleteCommand(manager));
        subCommands.put("take", new GlobalTakeCommand(manager));
    }
    // TODO: GlobalStats Messages
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.error(sender, "stats", "error.usage.stats");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);



        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "stats", "error.usage.stats");
        }


        return true;
    }
}
