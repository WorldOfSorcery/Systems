package me.hektortm.woSSystems.channels.cmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.channels.cmd.subcmd.channel.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class ChannelCommand implements CommandExecutor {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ChannelManager channelManager = plugin.getChannelManager();
    private final Map<String, SubCommand> subCommands = new HashMap<>();


    public ChannelCommand() {

        subCommands.put("create", new create());
        subCommands.put("focus", new focus());
        //subCommands.put("unfocus", new unfocus(channelManager));
        subCommands.put("join", new join());
        subCommands.put("leave", new leave());
        subCommands.put("list", new list());
        subCommands.put("modify", new modify());
        subCommands.put("broadcast", new broadcast());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(PermissionUtil.hasPermission(sender, subCommand.getPermission())) {
                subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
            } else {
                return true;
            }
        } else {
            sender.sendMessage("Unknown subcommand: " + subCommandName);
        }

        return true;
    }
}