package me.hektortm.woSSystems.chat.commands.subcommands;

import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class Request extends SubCommand {
    private final NicknameManager manager;

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
    }
}
