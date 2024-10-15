package me.hektortm.woSSystems.interactions.commands.subcommands;


import me.hektortm.woSSystems.interactions.commands.InterSubCommand;
import me.hektortm.woSSystems.interactions.core.InteractionManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends InterSubCommand {

    private final InteractionManager manager;

    public ReloadCommand(InteractionManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {


        if (args.length == 1) {
            if (!PermissionUtil.hasPermission(sender, Permissions.INTER_RELOAD_SINGLE)) return;

            String interactionId = args[0].toLowerCase();
            manager.reloadInteraction(interactionId);
            sender.sendMessage("Reloaded interaction: " + interactionId);
        } else {
            if (!PermissionUtil.hasPermission(sender, Permissions.INTER_RELOAD_ALL)) return;

            manager.reloadAllInteractions();
            sender.sendMessage("Reloaded all interactions.");
        }
    }
}
