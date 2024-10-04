package me.hektortm.woSSystems.interactions.commands.subcommands;


import me.hektortm.woSSystems.interactions.commands.SubCommand;
import me.hektortm.woSSystems.interactions.core.InteractionManager;
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
    public void execute(CommandSender sender, String[] args) {


        if (args.length == 1) {
            if (!sender.hasPermission("interactions.reload.single")) {
                sender.sendMessage("You do not have permission to use this command!");
                return;
            }

            String interactionId = args[0].toLowerCase();
            manager.reloadInteraction(interactionId);
            sender.sendMessage("Reloaded interaction: " + interactionId);
        } else {
            if (!sender.hasPermission("interactions.reload.all")) {
                sender.sendMessage("You do not have permission to use this command!");
            }

            manager.reloadAllInteractions();
            sender.sendMessage("Reloaded all interactions.");
        }
    }
}
