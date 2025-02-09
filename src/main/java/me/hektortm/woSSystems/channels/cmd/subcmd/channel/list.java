package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.Channel;
import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class list extends SubCommand {
    private final ChannelManager channelManager;
    private final WoSCore core = WoSCore.getPlugin(WoSCore.class);
    private final LangManager lang = core.getLang();

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

        Utils.success(player, "channel", "list.header");
        for (Channel channel : channelManager.getChannels()) {

            String status;
            if (channelManager.getChannelDAO().isInChannel(player.getUniqueId(), channel.getName())) {
                if (channelManager.getFocusedChannel(player) == channel) {
                    status = lang.getMessage("channel", "list.status.focused");
                } else {
                    status = lang.getMessage("channel", "list.status.joined");
                }
            } else {
                status = lang.getMessage("channel", "list.status.left");
            }

            if (!channel.isHidden()) {
                Utils.noPrefix(player, "channel", "list.entry", "%channel%", channel.getColor()+channel.getName(), "%short%", channel.getShortName(), "%status%", status);
            } else {
                if (channel.getPermission() != null) {
                    if(player.hasPermission(channel.getPermission())) {
                        Utils.noPrefix(player, "channel", "list.entry-hidden", "%channel%", channel.getColor()+channel.getName(), "%short%", channel.getShortName(), "%status%", status);;
                    }
                }
            }
        }
    }
}
