package me.hektortm.woSSystems.channels.cmd;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChannelCommand implements CommandExecutor {
    private final ChannelManager channelManager;

    public ChannelCommand(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /ch <subcommand> [arguments]");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage("Usage: /ch create <name> <shortName> <settings>");
                    player.sendMessage("Settings:");
                    player.sendMessage("-f: force join");
                    player.sendMessage("-a: auto join");
                    return true;
                }

                String name = args[1];
                String shortName = args[2];
                channelManager.createChannel(name, shortName, "{player}: {message}", null, false, false, -1);
                player.sendMessage("Channel " + name + " created.");
                channelManager.saveChannels();
                break;

            case "join":
                if (args.length < 2) {
                    player.sendMessage("Usage: /ch join <name>");
                    return true;
                }

                Channel joinChannel = channelManager.getChannel(args[1]);
                if (joinChannel == null) {
                    player.sendMessage("Channel not found.");
                } else {
                    channelManager.joinChannel(player, joinChannel.getName());
                    player.sendMessage("Joined channel: " + joinChannel.getName());
                }
                break;

            case "leave":
                if (args.length < 2) {
                    player.sendMessage("Usage: /ch leave <name>");
                    return true;
                }

                Channel leaveChannel = channelManager.getChannel(args[1]);
                if (leaveChannel == null) {
                    player.sendMessage("Channel not found.");
                } else if (leaveChannel.isForceJoin()) {
                    player.sendMessage("You cannot leave this channel.");
                } else {
                    channelManager.leaveChannel(player, leaveChannel.getName());
                    player.sendMessage("Left channel: " + leaveChannel.getName());
                }
                break;

            case "list":
                player.sendMessage("Available channels:");
                for (Channel channel : channelManager.getChannels()) {
                    player.sendMessage("- " + channel.getName() + " (Short: " + channel.getShortName() + ")");
                }
                break;

            case "focus":
                if (args.length < 2) {
                    player.sendMessage("Usage: /ch focus <name>");
                    return true;
                }

                Channel focusChannel = channelManager.getChannel(args[1]);
                if (focusChannel == null) {
                    player.sendMessage("Channel not found.");
                } else {
                    channelManager.setFocus(player, focusChannel);
                    player.sendMessage("Now focused on channel: " + focusChannel.getName());
                }
                break;
            default:
                player.sendMessage("Unknown subcommand.");
                break;
        }

        return true;
    }
}