package me.hektortm.woSSystems.systems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempTakeCommand extends SubCommand {

    private final UnlockableManager manager;

    public TempTakeCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.UNLOCKABLE_TEMP_TAKE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

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

        if (sender instanceof Player P) {
            Utils.successMsg2Values(P, "unlockables", "take.temp", "%id%", id, "%player%", p.getName());
        }
    }
}
