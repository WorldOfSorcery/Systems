package me.hektortm.woSSystems.systems.commands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.model.BasicCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Bukkit {@link org.bukkit.command.CommandExecutor} for dynamically-registered
 * {@link BasicCommand} entries.
 *
 * <p>Each instance is bound to a single {@link BasicCommand} record loaded from
 * the database.  When the command is executed by an online player, it delegates
 * directly to {@link me.hektortm.woSSystems.systems.interactions.InteractionManager#triggerInteraction}
 * using the interaction ID configured in that record.</p>
 *
 * <p>Console senders and other non-player callers receive an error message and
 * the command is silently ignored.</p>
 */
public class BasicCommandExecutor implements org.bukkit.command.CommandExecutor {
    private final WoSSystems plugin = WoSSystems.getInstance();
    private final BasicCommand basicCommand;

    /**
     * Creates an executor bound to the given {@link BasicCommand}.
     *
     * @param command the command definition to execute
     */
    public BasicCommandExecutor(BasicCommand command) {
        this.basicCommand = command;
    }

    /**
     * Validates that the sender is an online player, then triggers the
     * interaction linked to this command.
     *
     * @param sender the command sender
     * @param command the command that was executed
     * @param label   the alias that was used
     * @param args    any additional arguments (currently ignored)
     * @return always {@code true}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        plugin.writeLog("[CommandExecutor]", Level.INFO, "Trying to find interaction: "+basicCommand.getInteraction());
        if (basicCommand.getCommand() == null) {
            plugin.writeLog("[CommandExecutor]", Level.INFO, "Command not found.");
            return true;
        }
        plugin.getInteractionManager().triggerInteraction(basicCommand.getInteraction(), player, null);
        return true;
    }
}
