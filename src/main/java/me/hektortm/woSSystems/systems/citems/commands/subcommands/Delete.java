package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

public class Delete extends SubCommand {

    private final DAOHub hub;

    public Delete(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_DELETE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Utils.info(sender, "citems", "info.usage.delete");
            return;
        }

        String id = args[0];

        if (!hub.getCitemDAO().citemExists(id)) {
            Utils.error(sender, "citems", "error.not-found");
            return;
        }

        hub.getCitemDAO().deleteCitem(id);
        Utils.success(sender, "citems", "deleted", "%id%", id);
    }
}
