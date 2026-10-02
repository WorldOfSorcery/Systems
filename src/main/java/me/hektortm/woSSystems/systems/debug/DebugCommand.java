package me.hektortm.woSSystems.systems.debug;

import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** {@code /wosdebug}: switches debug mode on or off for the staff member running it. */
public class DebugCommand implements CommandExecutor {

    private final DebugMode debug;

    public DebugCommand(DebugMode debug) {
        this.debug = debug;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return true;
        if (!PermissionUtil.hasPermission(sender, Permissions.DEBUG_USE)) return true;
        Utils.info(sender, "debug", debug.toggle((Player) sender) ? "enabled" : "disabled");
        return true;
    }
}
