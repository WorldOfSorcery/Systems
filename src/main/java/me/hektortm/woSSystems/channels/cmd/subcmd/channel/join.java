package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class join extends SubCommand {
    private final ChannelManager channelManager;

    public join(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /ch join <name>");
            return;
        }

        Channel joinChannel = channelManager.getChannel(args[0]);
        if (joinChannel.getPermission() != null && !player.hasPermission(joinChannel.getPermission())) {
            player.sendMessage("You do not have permission to join this channel.");
            return;
        }
        if (joinChannel == null) {
            player.sendMessage("Channel not found.");
        } else {
            channelManager.joinChannel(player, joinChannel.getName());
            player.sendMessage("Joined channel: " + joinChannel.getName());
        }
    }
}
