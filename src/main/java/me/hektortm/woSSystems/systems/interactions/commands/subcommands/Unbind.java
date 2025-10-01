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

public class Unbind extends SubCommand {
    private final DAOHub hub;

    public Unbind(DAOHub hub) {
        this.hub = hub;
    }

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

        Location loc = BlockChecks.getTargetBlock(p);
        if (BlockChecks.isBlockAir(loc.getBlock(), p)) return;

        if (hub.getInteractionDAO().unbindBlock(loc))
            Utils.success(sender, "interactions", "unbind");
        else
            Utils.error(sender, "interactions", "error.failed", "%param%", "unbind interaction from Block");
    }
}
