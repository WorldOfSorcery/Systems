package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class setRadius extends SubCommand {
    private final ChannelManager channelManager;

    public setRadius(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "setradius";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /ch setradius <Channel> <Radius>");
            return;
        }

        String name = args[0];
        int radius;
        try {
            radius = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        Channel channel = channelManager.getChannel(name);
        channel.setRadius(radius);
        sender.sendMessage("Radius set to " + channel.getRadius());
    }
}
