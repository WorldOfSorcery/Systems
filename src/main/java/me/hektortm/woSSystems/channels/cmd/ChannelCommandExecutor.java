package me.hektortm.woSSystems.channels.cmd;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelCommandExecutor implements CommandExecutor {
    private final ChannelManager channelManager;
    private final Channel channel;

    public ChannelCommandExecutor(ChannelManager channelManager, Channel channel) {
        this.channelManager = channelManager;
        this.channel = channel;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (channel.getPermission() != null && !sender.hasPermission(channel.getPermission())) {
            player.sendMessage("No permission");
            return true;
        }

        // Check if the player is joined to the channel
        if (!channelManager.getChannelDAO().isInChannel(player.getUniqueId(), channel.getName())) {
            player.sendMessage("§cYou are not joined to the channel: " + channel.getName());
            return true;
        }

        // Check if the player has provided a message
        if (args.length == 0) {
            player.sendMessage("§cUsage: /" + label + " <message>");
            return true;
        }

        // Send the message to the channel
        String message = String.join(" ", args);
        channelManager.sendMessagePerCommand(player, channel.getName(), message);
        return true;
    }
}