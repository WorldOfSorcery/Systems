package me.hektortm.woSSystems.systems.interactions.actions;

import me.hektortm.woSSystems.utils.PlaceholderResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ActionHandler {

    private Plugin plugin;
    private final PlaceholderResolver resolver;

    public ActionHandler(Plugin plugin, PlaceholderResolver resolver) {
        this.plugin = plugin;
        this.resolver = resolver;
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
                return;
            }
        } if (action.startsWith("close_gui")) {
            player.closeInventory();
        } if (action.startsWith("send_message")) {
            String message = action.replace("send_message", "");
            String finalMessage = resolver.resolvePlaceholders(message, player);
            player.sendMessage(finalMessage.replace("&", "§"));
            return;
        } else {
            // Run any other command
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action);
            return;
        }
    }
}