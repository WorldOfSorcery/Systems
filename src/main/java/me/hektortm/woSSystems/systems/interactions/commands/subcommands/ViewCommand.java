package me.hektortm.woSSystems.systems.interactions.commands.subcommands;


import me.hektortm.woSSystems.systems.interactions.commands.InterSubCommand;
import me.hektortm.woSSystems.systems.interactions.core.InteractionConfig;
import me.hektortm.woSSystems.systems.interactions.core.InteractionManager;
import org.bukkit.command.CommandSender;

public class ViewCommand extends InterSubCommand {

    private final InteractionManager manager;
    public ViewCommand(InteractionManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "view";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("interactions.view")) {
            sender.sendMessage("You do not have permission to use this command!");
            return;
        }

        if (args.length == 1) {
            String interactionId = args[0];
            InteractionConfig interaction = manager.getInteractionById(interactionId);
            if (interaction != null) {
                sender.sendMessage("Interaction details:");
                sender.sendMessage("ID: " + interactionId);
                sender.sendMessage("Actions:");
                for (String action : interaction.getActions()) {
                    sender.sendMessage("- " + action);
                }
            } else {
                sender.sendMessage("Interaction not found: " + interactionId);
            }
        } else {
            sender.sendMessage("Usage: /interaction view <id>");
        }
    }
}
