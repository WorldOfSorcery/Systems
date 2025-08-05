package me.hektortm.woSSystems.utils;

import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpUtil {

    public static void sendHelp(Map<Permissions, String> permCmds, CommandSender sender, String fileName) {

        List<Permissions> permissions = new ArrayList<>(permCmds.keySet());

        if (PermissionUtil.hasAnyPermission(sender, permissions.toArray(new Permissions[0]))) {
            Utils.info(sender, fileName, "help.header");
            for (Permissions perm : permissions) {
                if (PermissionUtil.hasPermission(sender, perm)) {
                    Utils.noPrefix(sender, fileName, "help." + permCmds.get(perm));
                }
            }
            Utils.noPrefix(sender, fileName, "help.help");
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }
}
