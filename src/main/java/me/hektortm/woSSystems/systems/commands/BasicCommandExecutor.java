package me.hektortm.woSSystems.systems.commands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.BasicCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BasicCommandExecutor implements org.bukkit.command.CommandExecutor {
    private final WoSSystems plugin = WoSSystems.getInstance();
    private final BasicCommand basicCommand;

    public BasicCommandExecutor(BasicCommand command) {
        this.basicCommand = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        plugin.getInteractionManager().triggerInteraction(basicCommand.getInteraction(), player);
        return true;
    }
}
