package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class join extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ChannelManager channelManager = plugin.getChannelManager();

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

        channelManager.joinChannel(player, args[0]);

    }
}
