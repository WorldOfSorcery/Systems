package me.hektortm.woSSystems.systems.loottables.commands;


import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.loottables.LoottableManager;
import me.hektortm.woSSystems.systems.loottables.commands.subcommands.Chest;
import me.hektortm.woSSystems.systems.loottables.commands.subcommands.Trigger;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LoottableCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final DAOHub hub;
    private final LoottableManager loottableManager;

    public LoottableCommand(DAOHub hub, LoottableManager loottableManager) {
        this.hub = hub;
        this.loottableManager = loottableManager;

        subCommands.put("trigger", new Trigger(loottableManager));
        subCommands.put("chest", new Chest(hub));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.info(sender, "loottables", "error.usage.loottable");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.info(sender, "loottables", "error.usage.loottable");
        }

        return true;
    }
}
