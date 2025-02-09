package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class modify extends SubCommand {
    private final ChannelManager channelManager;

    public modify(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "modify";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_MODIFY;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length < 3) {
            sendUsage(player);
            return;
        }

        String channelName = args[0];
        Channel channel = channelManager.getChannel(channelName);
        String attributeName = args[1];

        switch (attributeName) {
            case "fm":
            case "format":
                String format = String.join(" ", Arrays.copyOfRange(args, Arrays.asList(args).indexOf("format") + 1, args.length)).trim();
                format = format.replace('&', '§');

                if (channel == null) {
                    player.sendMessage("§cChannel §f" + channelName + " §cdoes not exist.");
                    return;
                }

                // Set the new format for the channel
                channel.setFormat(format);
                channelManager.saveChannels(); // Save the updated channel data

                Utils.success(player, "channel", "modify.format", "%channel%", channel.getColor()+channel.getName(), "%format%", format);
                break;
            case "-r":
            case "radius":
                int radius;
                try {
                    radius = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }

                channel.setRadius(radius);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.radius", "%channel%", channel.getColor()+channel.getName(), "%radius%", Integer.toString(radius));
                break;
            case "-d":
            case "defaultchannel":
                boolean value = Boolean.parseBoolean(args[2]);
                channel.setDefaultChannel(value);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.default", "%channel%", channel.getColor()+channel.getName(), "%value%", Boolean.toString(value));
                break;

            case "-f":
            case "forcejoin":
                boolean forcejoin = Boolean.parseBoolean(args[2]);
                channel.setForceJoin(forcejoin);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.forcejoin", "%channel%", channel.getColor()+channel.getName(), "%value%", Boolean.toString(forcejoin));
                break;
            case "-a":
            case "autojoin":
                boolean autojoin = Boolean.parseBoolean(args[2]);
                channel.setAutoJoin(autojoin);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.autojoin", "%channel%", channel.getColor()+channel.getName(), "%value%", Boolean.toString(autojoin));
                break;
            case "-h":
            case "hidden":
                boolean hidden = Boolean.parseBoolean(args[2]);
                channel.setHidden(hidden);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.hidden", "%channel%", channel.getColor()+channel.getName(), "%value%", Boolean.toString(hidden));
                break;
            case "-p":
            case "permission":
                String permission = args[2];
                channel.setPermission(permission);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.permission", "%channel%", channel.getColor()+channel.getName(), "%permission%", permission);
                break;
            case "-b":
            case "broadcastable":
                boolean broadcastable = Boolean.parseBoolean(args[2]);
                channel.setBroadcastable(broadcastable);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.broadcast", "%channel%", channel.getColor()+channel.getName(), "%value%", Boolean.toString(broadcastable));
                break;
            case "-c":
            case "color":
                if (!args[2].contains("&")) {
                    Utils.error(player, "channel", "error.colorcode");
                    return;
                }
                String color = args[2].replace("&", "§");
                channel.setColor(color);
                channelManager.saveChannels();
                Utils.success(player, "channel", "modify.color", "%channel%", channel.getColor()+channel.getName(), "%color%", color + "color");
                break;
            default:
                sendUsage(player);
        }
    }

    private void sendUsage(Player player) {
        Utils.info(player, "channel", "info.usage.modify.usage");
        Utils.noPrefix(player, "channel", "info.usage.modify.attributes");
        Utils.noPrefix(player, "channel", "info.usage.modify.format");
        Utils.noPrefix(player, "channel", "info.usage.modify.radius");
        Utils.noPrefix(player, "channel", "info.usage.modify.default");
        Utils.noPrefix(player, "channel", "info.usage.modify.forcejoin");
        Utils.noPrefix(player, "channel", "info.usage.modify.autojoin");
        Utils.noPrefix(player, "channel", "info.usage.modify.hidden");
        Utils.noPrefix(player, "channel", "info.usage.modify.permission");
        Utils.noPrefix(player, "channel", "info.usage.modify.broadcast");
        Utils.noPrefix(player, "channel", "info.usage.modify.color");
    }
}
