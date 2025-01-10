package me.hektortm.woSSystems.channels.cmd.subcmd;

import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class Request extends SubCommand {
    private final NicknameManager manager;
    private final LangManager lang = new LangManager(WoSCore.getPlugin(WoSCore.class));

    public Request(NicknameManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "request";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.NICK_REQUEST_SEND;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        OfflinePlayer p = (OfflinePlayer) sender;
        String nick = args[0];


        manager.requestNicknameChange(p, nick);
        if (nick.equals("reset")) {
            Utils.successMsg(sender, "chat", "nick.request-reset-sent");
            //chatManager.serverChat("Staff", lang.getMessage("chat", "nick.request-reset-receive").replace("%player%", p.getName()));
            return;
        }
        Utils.successMsg(sender, "chat", "nick.request-sent");
        //chatManager.serverChat("Staff", lang.getMessage("chat", "nick.request-receive").replace("%player%", p.getName()));
    }
}
