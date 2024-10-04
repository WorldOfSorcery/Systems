package me.hektortm.woSSystems.interactions.commands;


import me.hektortm.woSSystems.interactions.core.InteractionConfig;
import me.hektortm.woSSystems.interactions.core.InteractionManager;
import me.hektortm.woSSystems.interactions.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GUIcommand implements CommandExecutor {
    private final GUIManager manager;
    private final InteractionManager interactionManager;

    public GUIcommand(GUIManager manager, InteractionManager interactionManager) {
        this.manager = manager;
        this.interactionManager = interactionManager;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("openGUI")) {
            // Check if at least two arguments are provided
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /opengui <player> <id>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            String interactionId = args[1];


            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return true;
            }

            InteractionConfig interactionConfig = interactionManager.getInteractionConfig(interactionId);

            if (interactionConfig != null) {
                Player p = Bukkit.getPlayer(args[0]);

                p.closeInventory();
                manager.openGUI(target, interactionConfig);
                sender.sendMessage("Opening the GUI(" + interactionId+") for "+target.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "GUI not found: " + interactionId);
            }

            return true;
        }
        return false;
    }


}
