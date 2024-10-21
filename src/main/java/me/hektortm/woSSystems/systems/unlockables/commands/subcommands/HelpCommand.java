package me.hektortm.woSSystems.systems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.systems.unlockables.commands.UnlockableCommand;
import me.hektortm.woSSystems.systems.unlockables.commands.UnlockableSubCommand;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

public class HelpCommand extends UnlockableSubCommand {

    private final UnlockableCommand cmd;
    public HelpCommand(UnlockableCommand cmd) {
        this.cmd = cmd;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasAnyPermission(sender, Permissions.UNLOCKABLE_GIVE, Permissions.UNLOCKABLE_TAKE, Permissions.UNLOCKABLE_DELETE, Permissions.UNLOCKABLE_CREATE)) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        cmd.unlockableHelp(sender);
    }
}
