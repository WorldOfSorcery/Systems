package me.hektortm.woSSystems.systems.interactions.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TriggerCommand extends SubCommand {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager manager_new = plugin.getInteractionManager();

    @Override
    public String getName() {
        return "trigger";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.INTER_TRIGGER;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length == 2) {
            String playerName = args[0];
            String interactionId = args[1].toLowerCase();

            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found: " + playerName);
                return;
            }



                sender.sendMessage("Triggered interaction " + interactionId + " for player " + playerName);
                manager_new.triggerInteraction(interactionId, targetPlayer);


        } else {
            sender.sendMessage("Usage: /interaction trigger <player> <id>");
        }
    }
}
