package me.hektortm.woSSystems.systems.guis.command;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.guis.command.subcmd.Open;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GUICommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final DAOHub hub;

    public GUICommand(DAOHub hub) {
        this.hub = hub;

        subCommands.put("open", new Open(hub));
    }
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
}
