package me.hektortm.woSSystems.chat.commands.subcommands;

import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reset extends SubCommand {

    private final NicknameManager manager;

    public Reset(NicknameManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.NICK_RESET_SELF;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        if (args.length == 0) {
            if (manager.getNickname(p) == null) {
                p.sendMessage("You do not have a nickname");
                return;
            }
            manager.resetNickname(p);
            p.sendMessage("Reset your own Nickname.");
            return;
        }
        if (args[0].equals(p.getName())) {
            if (manager.getNickname(p) == null) {
                p.sendMessage("You do not have a nickname");
                return;
            }
            manager.resetNickname(p);
            p.sendMessage("Reset your own Nickname.");
        } else {
            if (!PermissionUtil.hasPermission(p, Permissions.NICK_RESET_OTHERS)) return;
            OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
            if (manager.getNickname((Player) t) == null) {
                p.sendMessage("Has no nickname");
                return;
            }
            manager.resetNickname((Player) t);
            p.sendMessage("reset Nickname");


        }


    }
}
