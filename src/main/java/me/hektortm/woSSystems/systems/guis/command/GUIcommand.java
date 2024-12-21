package me.hektortm.woSSystems.systems.guis.command;


import me.hektortm.woSSystems.systems.guis.command.subcommands.Open;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class GUIcommand implements CommandExecutor {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public GUIcommand(GUIManager manager) {

        subCommands.put("open", new Open(manager));

    }

    //TODO: Rework
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            Utils.error(sender, "citems", "error.usage.citem");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "citems", "error.usage.citem");
        }

        return true;
    }
}
