package me.hektortm.woSSystems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.UnlockableSubCommand;
import me.hektortm.woSSystems.unlockables.utils.Action;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveTempCommand extends UnlockableSubCommand {

    private final UnlockableManager manager;
    public GiveTempCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "givetemp";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("unlockables.givetemp")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        String id = args[1];

        manager.modifyTempUnlockable(p.getUniqueId(), id, Action.GIVE);
        if (sender instanceof Player P) {
            Utils.successMsg2Values(P, "unlockables", "give.tempp", "%id%", id, "%player%", p.getName());
        }

    }
}
