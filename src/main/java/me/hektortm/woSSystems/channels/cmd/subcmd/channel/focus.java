package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class focus extends SubCommand {
    private final ChannelManager channelManager;

    public focus(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "focus";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_FOCUS;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            Utils.info(player, "channel", "info.usage.focus");
            return;
        }
            channelManager.setFocus(player, args[0]);

    }
}
