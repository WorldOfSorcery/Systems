package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
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
        return Permissions.CHANNEL_JOIN;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            Utils.info(player, "channel", "info.usage.join");
            return;
        }

        Channel joinChannel = channelManager.getChannel(args[0]);
        if (joinChannel == null) {
            Utils.error(player, "channel", "error.not-found");
            return;
        }
        if (joinChannel.getPermission() != null && !player.hasPermission(joinChannel.getPermission()))
            Utils.error(player, "channel", "error.no-perms");

        else channelManager.joinChannel(player, joinChannel.getName());

    }
}
