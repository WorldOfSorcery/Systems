package me.hektortm.woSSystems.interactions.core;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ActionHandler {

    private Plugin plugin;

    public ActionHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    // Processes and triggers a list of actions
    public void triggerActions(List<String> actions, Player player) {
        for (String action : actions) {
            triggerCommand(action, player);
        }
    }

    // Process a single command-based action
    public void triggerCommand(String action, Player player) {
        if (action == null) return;

        action = action.replace("%player%", player.getName());

        // Handle special actions
        if (action.startsWith("playsound")) {
            String[] parts = action.split(" ");
            if (parts.length > 1) {
                String sound = parts[1];
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
            }
        } else if (action.startsWith("send_message")) {
            String message = action.replace("send_message", "");
            player.sendMessage(message.replace("&", "§"));
        }
        else {
            // Run any other command
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action);
        }
    }
}
