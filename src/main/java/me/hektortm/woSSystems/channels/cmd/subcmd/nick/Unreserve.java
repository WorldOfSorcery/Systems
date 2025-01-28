package me.hektortm.woSSystems.channels.cmd.subcmd.nick;

import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Unreserve extends SubCommand {
    private final NicknameManager manager;

    public Unreserve(NicknameManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "unreserve";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.NICK_RESERVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        String nick = manager.getPlayersReservedNick(p.getUniqueId());

        manager.unreserveNickname(p.getUniqueId(), nick);
        p.sendMessage("Unreserved nickname: " + nick);
    }
}
