package me.hektortm.woSSystems.channels.cmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.channels.cmd.subcmd.channel.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        subCommands.put("help", new help());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return true;
        }
        Player p = (Player) sender;
        for (Channel channel  : channelManager.getChannels()) {
            String name = channel.getName();
            String shortN = channel.getShortName();
            if (args[0].equalsIgnoreCase(name) || args[0].equalsIgnoreCase(shortN)) {
                if (args.length == 1) {
                    if(channelManager.getChannelDAO().isInChannel(p.getUniqueId(), name)) {
                        channelManager.setFocus(p, name);
                        return true;
                    } else {
                        channelManager.joinChannel(p, name);
                        channelManager.setFocus(p, name);
                        return true;
                    }

                } else if (args.length >= 2) {
                    if (channelManager.getChannelDAO().isInChannel(p.getUniqueId(), name)) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            if (i == args.length - 1) {
                                builder.append(args[i]);
                            } else {
                                builder.append(args[i] + " ");
                            }
                        }
                        String message = builder.toString();
                        channelManager.sendMessagePerCommand(p, channel.getName(), message);
                        return true;
                    } else {
                        channelManager.joinChannel(p, name);
                        channelManager.setFocus(p, name);
                        return true;
                    }
                }
            }
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