package me.hektortm.woSSystems.citems.commands.subcommands;

import me.hektortm.woSSystems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.citems.commands.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Arrays;

public class DeleteCommand extends SubCommand implements Listener {

    static Inventory inventory;
    private final CitemCommand citem;

    public DeleteCommand(CitemCommand citem) {
        this.citem = citem;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("not a player");
        }

        if (!sender.hasPermission("citem.delete")) {
            Utils.error(sender, "general", "error.perms");
        }

        Player p = (Player) sender;
        String id = args[0];

        File itemFile = new File(citem.citemsFolder, id + ".json");
        if (!itemFile.exists()) {
            Utils.error(p, "citems", "error.not-found");
            return;
        }
        if (args.length == 1) {
            Utils.successMsg1Value(p, "citems", "delete.confirm", "%id%", id);
        }

        if(args.length == 2 && args[1].equals("confirm")) {
            deleteCitem(id);
            Utils.successMsg1Value(p, "citems", "delete.success", "%id%", id);
        }
    }
    private void deleteCitem(String id) {
        File itemFile = new File(citem.citemsFolder, id + ".json");
        itemFile.delete();

    }



}
