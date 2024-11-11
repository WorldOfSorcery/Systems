package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.citems.commands.CitemSubCommand;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;

public class DeleteCommand extends CitemSubCommand implements Listener {

    private final CitemCommand citem;
    private final LogManager log;

    public DeleteCommand(CitemCommand citem, LogManager log) {
        this.citem = citem;
        this.log = log;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return;
        }

        if (!PermissionUtil.hasPermission(sender, Permissions.CITEM_DELETE)) return;

        Player p = (Player) sender;
        String id = args[0];

        File itemFile = new File(citem.citemsFolder, id + ".json");
        if (!itemFile.exists()) {
            Utils.error(p, "citems", "error.not-found");
            return;
        }
        if (args.length == 1) {
            Utils.successMsg1Value(p, "citems", "delete.confirm", "%id%", id);
            return;
        }

        if(args.length == 2 && args[1].equals("confirm")) {
            deleteCitem(id);
            Utils.successMsg1Value(p, "citems", "delete.success", "%id%", id);
            log.sendWarning(p.getName()+ "-> deleted Citem: "+id);
            log.writeLog(p, "deleted Citem: "+id);
        }
    }
    private void deleteCitem(String id) {
        File itemFile = new File(citem.citemsFolder, id + ".json");
        itemFile.delete();

    }



}
