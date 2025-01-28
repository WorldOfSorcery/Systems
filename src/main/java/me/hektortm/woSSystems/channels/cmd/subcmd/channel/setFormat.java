package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class setFormat extends SubCommand {
    private final ChannelManager channelManager;

    public setFormat(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "setformat";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_SET_FORMAT; // Ensure this permission is defined in your Permissions enum
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return; // Ensure the sender is a player

        Player player = (Player) sender;

        // Check if the command has the correct number of arguments
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ch setformat <channelName> <format>");
            player.sendMessage("§7Example: /ch setformat Global §e{player}: §f{message}");
            return;
        }

        String channelName = args[0];
        String format = String.join(" ", args).substring(args[0].length()).trim(); // Combine all arguments after the channel name

        // Replace '&' with '§' to support Minecraft color codes
        format = format.replace('&', '§');

        // Get the channel from the ChannelManager
        Channel channel = channelManager.getChannel(channelName);

        if (channel == null) {
            player.sendMessage("§cChannel §f" + channelName + " §cdoes not exist.");
            return;
        }

        // Set the new format for the channel
        channel.setFormat(format);
        channelManager.saveChannels(); // Save the updated channel data

        player.sendMessage("§aFormat for channel §f" + channelName + " §ahas been updated to:");
        player.sendMessage(format); // Send the formatted message to the player
    }
}