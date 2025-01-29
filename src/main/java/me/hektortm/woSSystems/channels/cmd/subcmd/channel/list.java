package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class list extends SubCommand {
    private final ChannelManager channelManager;

    public list(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_LIST;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        player.sendMessage("Available channels:");
        for (Channel channel : channelManager.getChannels()) {

            String status;
            if (channelManager.getChannelDAO().isInChannel(player.getUniqueId(), channel.getName())) {
                if (channelManager.getFocusedChannel(player) == channel) {
                    status = "§bFocused";
                } else {
                    status = "§aJoined";
                }
            } else {
                status = "§cLeft";
            }

            if (!channel.isHidden()) {
                player.sendMessage(channel.getColor() + channel.getName()+": §7"+channel.getShortName() + " §7["+status+"§7]");
            } else {
                if (channel.getPermission() != null) {
                    if(player.hasPermission(channel.getPermission())) {
                        player.sendMessage(channel.getColor() + channel.getName()+": §7"+channel.getShortName() + " §7["+status+"§7] §f§ohidden");
                    }
                }
            }
        }
    }
}
