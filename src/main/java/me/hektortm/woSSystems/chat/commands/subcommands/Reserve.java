package me.hektortm.woSSystems.chat.commands.subcommands;

import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reserve extends SubCommand {
    private final NicknameManager manager;

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
        if (args.length > 2) return;
        Player p = (Player) sender;
        String nick = args[0];

        if (args.length == 0) {
            String reserved = manager.getPlayersReservedNick(p);
            if (reserved != null) p.sendMessage("You have a reserved nick: "+reserved);
            else p.sendMessage("You have no reserved nick");
            return;
        }
        else if (args.length == 2) {
            if (manager.getPlayersReservedNick(p) != null) {
                p.sendMessage("You can only reserve one nickname."); //TODO
                return;
            }
            manager.reserveNickname(p.getUniqueId(), nick);
        }


    }
}
