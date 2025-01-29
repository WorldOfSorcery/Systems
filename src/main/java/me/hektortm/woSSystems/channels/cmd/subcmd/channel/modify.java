package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
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
            sender.sendMessage("§cUsage: /ch setattribute <channel> <attribute> <value>");
            sender.sendMessage("§7Attributes:");
            sender.sendMessage("§f-fm / format§7: String");
            sender.sendMessage("§eExample: {player}: {message}");
            sender.sendMessage("§f-r / radius§7: Integer");
            sender.sendMessage("§eExample: -1 = unlimited, <0 = limited range");
            sender.sendMessage("§f-d / defaultchannel§7: Boolean");
            sender.sendMessage("§f-f / forcejoin§7: Boolean");
            sender.sendMessage("§f-a autojoin§7: Boolean");
            sender.sendMessage("§f-h / hidden§7: Boolean");
            sender.sendMessage("§f-p / permission§7: String");
            sender.sendMessage("§eExample: chat.channel.staff");
            sender.sendMessage("§f-b / broadcastable§7: Boolean");
            sender.sendMessage("§f-c / color§7: Colorcode");

        }

        String channelName = args[0];
        Channel channel = channelManager.getChannel(channelName);
        String attributeName = args[1];

        switch (attributeName) {
            case "fm":
            case "format":
                String format = String.join(" ", Arrays.copyOfRange(args, Arrays.asList(args).indexOf("format") + 1, args.length)).trim();
                // Combine all arguments after the channel name

                // Replace '&' with '§' to support Minecraft color codes
                format = format.replace('&', '§');

                // Get the channel from the ChannelManager


                if (channel == null) {
                    player.sendMessage("§cChannel §f" + channelName + " §cdoes not exist.");
                    return;
                }

                // Set the new format for the channel
                channel.setFormat(format);
                channelManager.saveChannels(); // Save the updated channel data

                player.sendMessage("§aFormat for channel §f" + channelName + " §ahas been updated to:");
                player.sendMessage(format); // Send the formatted message to the player
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
                sender.sendMessage("Radius set to " + channel.getRadius());
                break;
            case "-d":
            case "defaultchannel":
                boolean value = Boolean.parseBoolean(args[2]);
                channel.setDefaultChannel(value);
                channelManager.saveChannels();
                player.sendMessage("§aSet default Channel Value for '" + channelName + "' to §e" + value);
                break;

            case "-f":
            case "forcejoin":
                boolean forcejoin = Boolean.parseBoolean(args[2]);
                channel.setForceJoin(forcejoin);
                channelManager.saveChannels();
                player.sendMessage("§aSet force Join Value for '" + channelName + "' to " + forcejoin);
                break;
            case "-a":
            case "autojoin":
                boolean autojoin = Boolean.parseBoolean(args[2]);
                channel.setAutoJoin(autojoin);
                channelManager.saveChannels();
                player.sendMessage("§aSet AutoJoin Value for '" + channelName + "' to " + autojoin);
                break;
            case "-h":
            case "hidden":
                boolean hidden = Boolean.parseBoolean(args[2]);
                channel.setHidden(hidden);
                channelManager.saveChannels();
                player.sendMessage("§aSet Hidden Value for '" + channelName + "' to " + hidden);
                break;
            case "-p":
            case "permission":
                String permission = args[2];
                channel.setPermission(permission);
                channelManager.saveChannels();
                player.sendMessage("§aSet Permission Value for '" + channelName + "' to " + permission);
                break;
            case "-b":
            case "broadcastable":
                boolean broadcastable = Boolean.parseBoolean(args[2]);
                channel.setBroadcastable(broadcastable);
                channelManager.saveChannels();
                player.sendMessage("§aSet Broadcastable Value for '" + channelName + "' to " + broadcastable);
                break;
            case "-c":
            case "color":
                if (!args[2].contains("&")) {
                    sender.sendMessage("Not a valid colorcode. Use atleast one '&'");
                    return;
                }
                String color = args[2].replace("&", "§");
                channel.setColor(color);
                channelManager.saveChannels();
                player.sendMessage("§aSet Color Value for '" + channelName + "' to " + color + "color");
                break;
            default:
                sender.sendMessage("§cUsage: /ch setattribute <channel> <attribute> <value>");
                sender.sendMessage("§7Attributes:");
                sender.sendMessage("§f-fm / format§7: String");
                sender.sendMessage("§eExample: {player}: {message}");
                sender.sendMessage("§f-r / radius§7: Integer");
                sender.sendMessage("§eExample: -1 = unlimited, <0 = limited range");
                sender.sendMessage("§f-d / defaultchannel§7: Boolean");
                sender.sendMessage("§f-f / forcejoin§7: Boolean");
                sender.sendMessage("§f-a autojoin§7: Boolean");
                sender.sendMessage("§f-h / hidden§7: Boolean");
                sender.sendMessage("§f-p / permission§7: String");
                sender.sendMessage("§eExample: chat.channel.staff");
                sender.sendMessage("§f-b / broadcastable§7: Boolean");
                sender.sendMessage("§f-c / color§7: Colorcode");
        }
    }
}
