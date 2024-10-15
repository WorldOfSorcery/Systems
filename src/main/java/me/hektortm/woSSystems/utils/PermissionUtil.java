package me.hektortm.woSSystems.utils;

import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

public class PermissionUtil {


    public static boolean hasPermission(CommandSender sender, Permissions permission) {
        if (sender.hasPermission(permission.getPermission())) {
            return true;
        } else {
            Utils.error(sender, "general", "error.perms");
            return false;
        }
    }


}
