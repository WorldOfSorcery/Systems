package me.hektortm.woSSystems.systems.interactions.commands.subcommands;


import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.BlockChecks;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Bind extends SubCommand {
    private final DAOHub hub;

    public Bind(DAOHub hub) {
        this.hub = hub;
    }


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
        if (args.length < 1) {
            Utils.info(p, "interactions", "info.usage.bind");
            return;
        }

        String interactionId = args[0];
        if (!hub.getInteractionDAO().interactionExists(interactionId, p)) return;

        Location loc = BlockChecks.getTargetBlock(p);
        if (BlockChecks.isBlockAir(loc.getBlock(), p)) return;

        if (hub.getInteractionDAO().bindBlock(interactionId, loc))
            Utils.success(p, "interactions", "bind", "%id%", interactionId);
        else
            Utils.error(p, "interactions", "error.failed", "%param%", "bind interaction to Block");
    }



}
