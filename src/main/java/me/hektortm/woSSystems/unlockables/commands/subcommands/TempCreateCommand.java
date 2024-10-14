package me.hektortm.woSSystems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.UnlockableSubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempCreateCommand extends UnlockableSubCommand {

    private final UnlockableManager manager;

    public TempCreateCommand(UnlockableManager manager) {
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
        if (!sender.hasPermission("tempunlockables.create")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        String id = args[0];

        manager.addTempUnlockable(sender, id);
    }
}
