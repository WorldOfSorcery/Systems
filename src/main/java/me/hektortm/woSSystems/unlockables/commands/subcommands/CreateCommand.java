package me.hektortm.woSSystems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.UnlockableSubCommand;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends UnlockableSubCommand {

    private final UnlockableManager manager;
    public CreateCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return;
        }
        if (!PermissionUtil.hasPermission(sender, Permissions.UNLOCKABLE_CREATE)) return;

        String id = args[0];

        manager.addUnlockable(sender, id);
    }
}
