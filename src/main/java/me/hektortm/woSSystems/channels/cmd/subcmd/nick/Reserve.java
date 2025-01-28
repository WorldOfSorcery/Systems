package me.hektortm.woSSystems.channels.cmd.subcmd.nick;

import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Reserve extends SubCommand {
    private final NicknameManager manager;
    private final WoSCore core = WoSCore.getPlugin(WoSCore.class);
    private final LangManager lang = new LangManager(core);

    public Reserve(NicknameManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reserve";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.NICK_RESERVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();
        if (args.length == 0) {
            String reserved = manager.getPlayersReservedNick(uuid);
            if (reserved != null) Utils.successMsg1Value(p, "chat", "nick.reserved-currently", "%nick%", reserved);
            else Utils.error(p, "chat", "error.no-reserved");
        }
        else if (args.length == 1) {
            String nick = args[0];
            if (manager.getPlayersReservedNick(uuid) != null) {
                Utils.error(p, "chat", "error.reserved-limit");
                return;
            }
            manager.reserveNickname(uuid, nick);
            Utils.successMsg1Value(p, "chat", "nick.reserved", "%nick%", nick);
        }
    }
}
