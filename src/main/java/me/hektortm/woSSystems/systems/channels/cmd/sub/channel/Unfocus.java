package me.hektortm.woSSystems.systems.channels.cmd.sub.channel;

import me.hektortm.woSSystems.utils.model.Channel;
import me.hektortm.woSSystems.systems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Unfocus extends SubCommand {
    private final ChannelManager channelManager;

    public Unfocus(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "unfocus";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_UNFOCUS;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Channel focusChannel = channelManager.getChannel(args[0]);
        if (focusChannel == null) {
            player.sendMessage("Channel not found.");
        } else {
            for (Channel channel : channelManager.getChannels()) {
                if (channel.isDefaultChannel()) {
                    //channelManager.setFocus(player, channel);
                }
            }

        }
    }
}
