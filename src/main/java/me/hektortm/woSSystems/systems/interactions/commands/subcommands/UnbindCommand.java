package me.hektortm.woSSystems.systems.interactions.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbindCommand extends SubCommand {
    // TODO: Interactions
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    @Override
    public String getName() {
        return "unbind";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.INTER_UNBIND;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        if (args.length == 1) {
            String interactionId = args[0];

            //Location loc = manager.getTargetBlock(p);

            //manager.unbindLocation(p, interactionId, loc);


        } else {
            sender.sendMessage("/interaction unbind <id>");
        }
    }
}
