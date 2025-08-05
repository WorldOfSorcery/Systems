package me.hektortm.woSSystems.channels.cmd.subcmd.nick;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Unreserve extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final NicknameManager manager = plugin.getNickManager();


    @Override
    public String getName() {
        return "unreserve";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.NICK_UNRESERVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        String nick = manager.getPlayersReservedNick(p.getUniqueId());

        manager.unreserveNickname(p.getUniqueId());
        p.sendMessage("Unreserved nickname: " + nick);
    }
}
