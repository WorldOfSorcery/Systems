package me.hektortm.woSSystems.systems.interactions.commands.subcommands;

import me.hektortm.woSSystems.systems.interactions.commands.InterSubCommand;
import me.hektortm.woSSystems.systems.interactions.core.BindManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbindCommand extends InterSubCommand {

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
        if (!PermissionUtil.hasPermission(sender, Permissions.INTER_UNBIND)) return;

        Player p = (Player) sender;

        if (args.length == 1) {
            String interactionId = args[0];

            Location loc = manager.getTargetBlockLocation(p);

            manager.unbindInteractionFromBlock(interactionId, loc);


        } else {
            sender.sendMessage("/interaction unbind <id>");
        }
    }
}
