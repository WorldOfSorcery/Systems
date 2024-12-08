package me.hektortm.woSSystems.systems.interactions.commands.subcommands;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BindCommand extends SubCommand {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager manager = plugin.getInteractionManager();


    @Override
    public String getName() {
        return "bind";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.INTER_BIND;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        if (args.length == 1) {
            String interactionId = args[0];
            Location loc = manager.getTargetBlock(p);
            manager.bindLocation(p, interactionId, loc);
        } else {
            sender.sendMessage("/interaction bind <id>");
        }
    }
}
