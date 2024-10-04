package me.hektortm.woSSystems.interactions.commands.subcommands;

import me.hektortm.woSSystems.interactions.commands.SubCommand;
import me.hektortm.woSSystems.interactions.core.BindManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbindCommand extends SubCommand {

    private final BindManager manager;

    public UnbindCommand(BindManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "unbind";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You are not a player.");
        }

        Player p = (Player) sender;

        if (!sender.hasPermission("interactions.unbind")) {
            sender.sendMessage("You do not have permission to use this command!");
            return;
        }

        if (args.length == 1) {
            String interactionId = args[0];

            Location loc = manager.getTargetBlockLocation(p);

            manager.unbindInteractionFromBlock(interactionId, loc);


        } else {
            sender.sendMessage("/interaction unbind <id>");
        }
    }
}
