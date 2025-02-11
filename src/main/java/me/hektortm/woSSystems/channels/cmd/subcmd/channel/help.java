package me.hektortm.woSSystems.channels.cmd.subcmd.channel;


import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.util.PermUtil;
import org.bukkit.command.CommandSender;


public class help extends SubCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        sendHelp(sender);

    }

    private void sendHelp(CommandSender s) {
        if(PermissionUtil.hasAnyPermission(s, Permissions.CHANNEL_JOIN, Permissions.CHANNEL_CREATE,
                Permissions.CHANNEL_MODIFY, Permissions.CHANNEL_LEAVE, Permissions.CHANNEL_FOCUS, Permissions.CHANNEL_LIST, Permissions.CHANNEL_BROADCAST)) {
            Utils.info(s, "channel", "help.header");
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.CHANNEL_CREATE)) Utils.noPrefix(s, "channel", "help.create");
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.CHANNEL_MODIFY)) Utils.noPrefix(s, "channel", "help.modify");
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.CHANNEL_BROADCAST)) Utils.noPrefix(s, "channel", "help.broadcast");
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.CHANNEL_JOIN)) Utils.noPrefix(s, "channel", "help.join");
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.CHANNEL_LEAVE)) Utils.noPrefix(s, "channel", "help.leave");
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.CHANNEL_FOCUS)) Utils.noPrefix(s, "channel", "help.focus");
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.CHANNEL_LIST)) Utils.noPrefix(s, "channel", "help.list");
            Utils.noPrefix(s, "channel", "help.quick");
            Utils.noPrefix(s, "channel", "help.quick2");
            Utils.noPrefix(s, "channel", "help.help");
        }
    }

}
