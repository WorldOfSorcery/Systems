package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class broadcast extends SubCommand {
    private final ChannelManager channelManager;

    public broadcast(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "broadcast";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_BROADCAST;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;

        Player player = (Player) sender;

        String channelName = args[0];
        Channel channel = channelManager.getChannel(channelName);

        if (channel == null) {
            player.sendMessage("This channel does not exist.");
            return;
        }

        if (!channel.isBroadcastable()) {
            player.sendMessage("This channel is not broadcastable");
        }

        String message = String.join(" ", args).substring(args[0].length()).trim();

        for (UUID recipientUUID : channelManager.getChannelDAO().getRecipients(channelName)) {
            Player recipient = Bukkit.getPlayer(recipientUUID);
            if (recipient == null) {
                continue;
            }
            recipient.sendMessage(formattedMessage(channel, message));
        }

    }

    private String formattedMessage(Channel channel, String message) {
        String format = channel.getFormat();
        String newFormat = format.replace("{player}", "§e§lBroadcast").replace("{message}", message);
        return newFormat;
    }
}
