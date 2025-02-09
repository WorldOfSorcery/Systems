package me.hektortm.woSSystems.channels.cmd.subcmd.nick;

import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Requests extends SubCommand {

    private final NicknameManager manager;

    public Requests(NicknameManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "requests";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.NICK_REQUEST_VIEW;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        manager.openRequestMenu(p);
    }
}
