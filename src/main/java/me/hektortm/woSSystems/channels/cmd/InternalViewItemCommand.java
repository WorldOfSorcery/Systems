package me.hektortm.woSSystems.channels.cmd;

import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InternalViewItemCommand implements CommandExecutor {
    private final WoSSystems plugin;

    public InternalViewItemCommand(WoSSystems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Check if the UUID argument is provided
            if (args.length < 1) {
                player.sendMessage("§cUsage: /internalviewitem <clickId>");
                return true;
            }

            // Parse the UUID from the command argument
            UUID clickId;
            try {
                clickId = UUID.fromString(args[0]);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid click ID.");
                return true;
            }

            // Retrieve the action from the clickActions map
            Inventory inv = plugin.getClickActions().get(clickId);
            if (inv != null) {
                // Execute the action
                ((Player) sender).openInventory(inv);
            } else {
                player.sendMessage("§cInvalid click ID.");
            }

            return true;
        }
        return false;
    }
}