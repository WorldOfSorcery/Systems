package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.citems.commands.CitemSubCommand;
import me.hektortm.woSSystems.citems.DataManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class UpdateCommand extends CitemSubCommand {

    private final CitemCommand citem;
    private final DataManager data;
    public UpdateCommand(CitemCommand citem, DataManager data) {
        this.citem = citem;
        this.data = data;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return;
        }
        if(!sender.hasPermission("citem.update")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if (args.length != 1) {
            Utils.error(p, "citems", "error.usage.update");
            return;
        }

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        if (meta == null) {
            Utils.error(p, "citems", "error.no-meta");
            return;
        }

        String id = args[0];
        if (!citem.citemsFolder.exists()) {
            Utils.error(p, "citems", "error.no-items");
            return;
        }

        File itemFile = new File(citem.citemsFolder, id + ".json");
        if (!itemFile.exists()) {
            Utils.error(p, "citems", "error.not-found");
            return;
        }

        data.updateItemInFile(itemInHand, itemFile);
        Utils.successMsg(p, "citems", "updated");
    }
}
