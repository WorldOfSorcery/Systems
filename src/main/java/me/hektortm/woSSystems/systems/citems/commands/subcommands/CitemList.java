package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CitemList extends SubCommand {

    private final DAOHub hub;

    public CitemList(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_LIST;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<String> ids = hub.getCitemDAO().getCitemIds();

        if (ids.isEmpty()) {
            Utils.error(sender, "citems", "error.no-items");
            return;
        }

        sender.sendMessage("§aSaved Citems §7(" + ids.size() + ")§a:");
        StringBuilder sb = new StringBuilder("§e");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i));
            if (i < ids.size() - 1) sb.append("§7, §e");
        }
        sender.sendMessage(sb.toString());
    }
}
