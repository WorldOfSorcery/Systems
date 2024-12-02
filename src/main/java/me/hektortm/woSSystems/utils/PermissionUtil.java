package me.hektortm.woSSystems.utils;

import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionUtil {

    public static boolean isPlayer(CommandSender sender) {
        if(!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasPermission(CommandSender sender, Permissions permission) {
        if (sender.hasPermission(permission.getPermission())) {
            return true;
        } else {
            Utils.error(sender, "general", "error.perms");
            return false;
        }
    }

    public static boolean hasPermissionNoMsg(CommandSender sender, Permissions permission) {
        if (sender.hasPermission(permission.getPermission())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasAnyPermission(CommandSender sender, Permissions... permissions) {
        for (Permissions perm : permissions) {
            if (sender.hasPermission(perm.getPermission())) {
                return true;
            }
        }
        return false;
    }

}
