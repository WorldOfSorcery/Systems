package me.hektortm.woSSystems.systems.guis.command.subcommands;

import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.interactions.config.InteractionConfig;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Open extends SubCommand {

    private final InteractionManager interactionManager;
    private final GUIManager guiManager;

    public Open(InteractionManager interactionManager, GUIManager guiManager) {
        this.interactionManager = interactionManager;
        this.guiManager = guiManager;

    }

    @Override
    public String getName() {
        return "open";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.GUI_OPEN;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check if at least two arguments are provided
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /opengui <player> <id>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        String interactionId = args[1];

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return;
        }

        InteractionConfig interactionConfig = interactionManager.getInteractionById(interactionId);

        if (interactionConfig != null) {
            Player p = Bukkit.getPlayer(args[0]);

            p.closeInventory();
            guiManager.openGUI(target, interactionConfig);
            sender.sendMessage("Opening the GUI(" + interactionId+") for "+target.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "GUI not found: " + interactionId);
        }

    }
}
