package me.hektortm.woSSystems.systems.channels.cmd.sub.channel;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.model.Channel;
import me.hektortm.woSSystems.systems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Leave extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ChannelManager channelManager = plugin.getChannelManager();

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
            Utils.info(player, "channel", "info.usage.leave");
            return;
        }

        Channel leaveChannel = channelManager.getChannel(args[0]);
        if (leaveChannel == null) {
            Utils.error(player, "channel", "error.not-found");
        } else if (leaveChannel.isForceJoin()) {
            Utils.info(player, "channel", "info.force-join");
        } else {
            channelManager.leaveChannel(player, leaveChannel.getName());
        }
    }
}
