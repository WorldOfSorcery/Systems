package me.hektortm.woSSystems.systems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand extends SubCommand {

    private final UnlockableManager manager;
    private final LogManager logManager;

    public DeleteCommand(UnlockableManager manager, LogManager logManager) {
        this.manager = manager;
        this.logManager = logManager;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.UNLOCKABLE_DELETE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        if (args.length != 1) {
            Utils.error(sender, "unlockables", "usage.perm.delete");
            return;
        }

        String id = args[0];

        if(!manager.unlockables.containsKey(id)) {
            Utils.error(sender, "unlockables", "error.not-found");
            return;
        }

        manager.deleteUnlockable(id);
        Utils.successMsg1Value(sender, "unlockables", "delete.perm", "%id%", id);
        logManager.sendWarning("Unlockable "+id+" deleted.");
    }
}
