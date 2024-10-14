package me.hektortm.woSSystems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.UnlockableSubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempDeleteCommand extends UnlockableSubCommand {

    private final UnlockableManager manager;
    public TempDeleteCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("unlockables.give")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1];

        if(!manager.tempUnlockables.containsKey(id)) {
            Utils.error(sender, "unlockables", "error.exists");
        }

        manager.deleteUnlockable(id, true);
        if (sender instanceof Player P) {
            Utils.successMsg2Values(P, "unlockables", "give.perm", "%id%", id, "%player%", p.getName());
        }
    }
}
