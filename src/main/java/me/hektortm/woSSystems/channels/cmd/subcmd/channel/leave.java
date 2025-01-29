package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class leave extends SubCommand {
    private final ChannelManager channelManager;

    public leave(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_LEAVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /ch leave <name>");
            return;
        }

        Channel leaveChannel = channelManager.getChannel(args[0]);
        if (leaveChannel == null) {
            player.sendMessage("Channel not found.");
        } else if (leaveChannel.isForceJoin()) {
            player.sendMessage("You cannot leave this channel.");
        } else {
            channelManager.leaveChannel(player, leaveChannel.getName());
        }
    }
}
