package me.hektortm.woSSystems.systems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.commands.UnlockableSubCommand;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempTakeCommand extends UnlockableSubCommand {

    private final UnlockableManager manager;

    public TempTakeCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermission(sender, Permissions.UNLOCKABLE_TEMP_TAKE)) return;

        if (args.length == 0 || args.length > 2) {
            Utils.error(sender, "unlockables", "usage.temp.take");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1].toLowerCase();

        if(!manager.tempUnlockables.containsKey(id)) {
            Utils.error(sender, "unlockables", "error.not-found");
            return;
        }

        manager.modifyTempUnlockable(p.getUniqueId(), id, Action.TAKE);
        if (sender instanceof Player P) {
            Utils.successMsg2Values(P, "unlockables", "take.temp", "%id%", id, "%player%", p.getName());
        }
    }
}
