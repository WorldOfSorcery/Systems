package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.citems.commands.SubCommand;
import me.hektortm.woSSystems.citems.core.DataManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class SaveCommand extends SubCommand {

    private final CitemCommand cmd;
    private final DataManager data;

    public SaveCommand(CitemCommand cmd, DataManager data) {
        this.cmd = cmd;
        this.data = data;
    }
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.error(sender, "general", "error.notplayer");
            return;
        }
        if(!sender.hasPermission("citem.save")) {
            Utils.error(sender, "general", "error.perms");
            return;
        }

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if (args.length != 1) {
            Utils.error(p, "citems", "error.usage.save");
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

        if (!cmd.citemsFolder.exists()) {
            cmd.citemsFolder.mkdirs();
        }
        File itemFile = new File(cmd.citemsFolder, id + ".json");
        if (itemFile.exists()) {
            Utils.error(p, "citems", "error.exists");
            return;
        }
        data.saveItemToFile(itemInHand, itemFile, id);
        Utils.successMsg1Value(p, "citems", "saved", "%id%", id);
    }
}
