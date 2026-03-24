package me.hektortm.woSSystems.utils;

import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Static helpers for command-sender permission and type checks.
 *
 * <p>All methods send appropriate error messages to the sender via the
 * {@code wosCore} language file when a check fails, except for
 * {@link #hasPermissionNoMsg(CommandSender, Permissions)} which is silent.</p>
 */
public class PermissionUtil {

    /**
     * Returns {@code true} if the sender is a {@link Player}; sends a
     * localised {@code error.notplayer} message and returns {@code false}
     * otherwise.
     *
     * @param sender the command sender to check
     * @return {@code true} if the sender is a player
     */
    public static boolean isPlayer(CommandSender sender) {
        if(!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns {@code true} if the sender holds the given permission.
     *
     * <p>The console always passes.  A {@code null} permission node is treated
     * as no restriction and also passes.  On failure a localised
     * {@code error.perms} message is sent to the sender.</p>
     *
     * @param sender     the command sender to check
     * @param permission the required permission, or {@code null} for no restriction
     * @return {@code true} if the sender is permitted
     */
    public static boolean hasPermission(CommandSender sender, Permissions permission) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        if (permission == null) {
            return true;
        }

        if (sender.hasPermission(permission.getPermission())) {
            return true;
        } else {
            Utils.error(sender, "general", "error.perms");
            return false;
        }
    }

    /**
     * Returns {@code true} if the sender holds the given permission, without
     * sending any message on failure.
     *
     * @param sender     the command sender to check
     * @param permission the required permission
     * @return {@code true} if the sender is permitted
     */
    public static boolean hasPermissionNoMsg(CommandSender sender, Permissions permission) {
        if (sender.hasPermission(permission.getPermission())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns {@code true} if the sender holds <em>at least one</em> of the
     * supplied permissions.
     *
     * @param sender      the command sender to check
     * @param permissions one or more permissions to test
     * @return {@code true} if any permission is held
     */
    public static boolean hasAnyPermission(CommandSender sender, Permissions... permissions) {
        for (Permissions perm : permissions) {
            if (sender.hasPermission(perm.getPermission())) {
                return true;
            }
        }
        return false;
    }

}
