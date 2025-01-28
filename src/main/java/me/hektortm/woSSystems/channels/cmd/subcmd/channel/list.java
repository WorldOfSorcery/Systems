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
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        player.sendMessage("Available channels:");
        for (Channel channel : channelManager.getChannels()) {
            if (!channel.isHidden()) {
                player.sendMessage("- " + channel.getName() + " (Short: " + channel.getShortName() + ")");
            }
        }
    }
}
