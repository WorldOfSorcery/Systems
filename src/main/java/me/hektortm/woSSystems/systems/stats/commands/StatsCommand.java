package me.hektortm.woSSystems.systems.stats.commands;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.commands.subcommands.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class StatsCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final StatsManager manager;

    public StatsCommand(StatsManager manager) {
        this.manager = manager;

        subCommands.put("give", new GiveCommand(manager));
        subCommands.put("take", new TakeCommand(manager));
        subCommands.put("set", new SetCommand(manager));
        //subCommands.put("help", new HelpCommand());
        subCommands.put("reset", new ResetCommand(manager));
        subCommands.put("reload", new ReloadCommand(manager));
        subCommands.put("create", new CreateCommand(manager));
        subCommands.put("delete", new DeleteCommand(manager));
        subCommands.put("view", new ViewCommand(manager));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.error(sender, "stats", "error.usage.stats");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "stats", "error.usage.stats");
        }


        return true;
    }
}
