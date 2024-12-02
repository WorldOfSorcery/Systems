package me.hektortm.woSSystems.systems.interactions.commands.subcommands.interactions;


import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    private final InteractionManager manager;

    public ReloadCommand(InteractionManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return null;
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
