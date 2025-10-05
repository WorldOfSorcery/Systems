package me.hektortm.woSSystems.systems.loottables.commands;


import me.hektortm.woSSystems.systems.loottables.LoottableManager;
import me.hektortm.woSSystems.systems.loottables.commands.subcommands.Give;
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
    private final LoottableManager loottableManager;

    public LoottableCommand(LoottableManager loottableManager) {
        this.loottableManager = loottableManager;

        subCommands.put("give", new Give(loottableManager));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.error(sender, "loottables", "error.usage.loottable");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "loottables", "error.usage.loottable");
        }

        return true;
    }
}
