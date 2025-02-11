package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class UpdateCommand extends SubCommand {

    private final CitemCommand citem;
    private final CitemManager data;
    public UpdateCommand(CitemCommand citem, CitemManager data) {
        this.citem = citem;
        this.data = data;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_UPDATE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;
        ItemStack itemInHand = p.getInventory().getItemInMainHand();

        if (args.length != 1) {
            Utils.info(p, "citems", "info.usage.update");
            return;
        }

        if (!data.getErrorHandler().handleCitemErrors(itemInHand, p)) return;

        String id = args[0];

        if (!data.getCitemDAO().citemExists(id)) {
            Utils.error(p, "citems", "error.not-found");
            return;
        }

        data.getCitemDAO().updateCitem(id, itemInHand);
        Utils.successMsg(p, "citems", "updated");
    }
}
