package me.hektortm.woSSystems.systems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempDeleteCommand extends SubCommand {

    private final UnlockableManager manager;
    public TempDeleteCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.UNLOCKABLE_TEMP_DELETE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            Utils.error(sender, "unlockables", "usage.temp.delete");
            return;
        }

        String id = args[0].toLowerCase();

        if(!manager.tempUnlockables.containsKey(id)) {
            Utils.error(sender, "unlockables", "error.not-found");
            return;
        }

        manager.deleteUnlockable(id);
        if (sender instanceof Player P) {
            Utils.successMsg1Value(P, "unlockables", "give.perm", "%id%", id);
        }
    }
}
