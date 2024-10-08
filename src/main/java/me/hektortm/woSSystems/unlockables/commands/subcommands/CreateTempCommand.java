package me.hektortm.woSSystems.unlockables.commands.subcommands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.UnlockableSubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateTempCommand extends UnlockableSubCommand {

    private final UnlockableManager manager;

    public CreateTempCommand(UnlockableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "createtemp";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return;
        }
        if (!sender.hasPermission("unlockables.create")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        String id = args[0];

        manager.addUnlockable(sender, id);
    }
}
