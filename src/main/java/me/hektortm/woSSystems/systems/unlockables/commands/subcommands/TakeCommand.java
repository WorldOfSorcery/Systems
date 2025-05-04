package me.hektortm.woSSystems.systems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.Actions;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TakeCommand extends SubCommand {

    private final UnlockableManager manager;

    public TakeCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.UNLOCKABLE_TAKE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length > 2) {
            Utils.error(sender, "unlockables", "usage.perm.take");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1];

        if(!manager.unlockables.containsKey(id)) {
            Utils.error(sender, "unlockables", "error.not-found");
            return;
        }

        manager.modifyUnlockable(p.getUniqueId(), id, Actions.TAKE);
        if (sender instanceof Player P) {
            Utils.successMsg2Values(P, "unlockables", "take.perm", "%id%", id, "%player%", p.getName());
        }
    }
}
