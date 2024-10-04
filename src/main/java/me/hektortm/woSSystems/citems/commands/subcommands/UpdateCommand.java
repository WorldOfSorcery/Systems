package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.citems.commands.SubCommand;
import me.hektortm.woSSystems.citems.core.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class UpdateCommand extends SubCommand {

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
        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if(!sender.hasPermission("citem.update")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        }

        if (args.length != 1) {
            p.sendMessage(ChatColor.RED + "Usage: /citem update [id]");
            return;
        }

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            p.sendMessage(ChatColor.RED + "You must be holding an item to use this command.");
            return;
        }

        if (meta == null) {
            p.sendMessage(ChatColor.RED + "This item has no metadata.");
            return;
        }

        String id = args[0];
        if (!citem.citemsFolder.exists()) {
            p.sendMessage(ChatColor.RED + "No saved custom items found.");
            return;
        }

        File itemFile = new File(citem.citemsFolder, id + ".json");
        if (!itemFile.exists()) {
            p.sendMessage(ChatColor.RED + "No custom item found with this ID.");
            return;
        }

        data.updateItemInFile(itemInHand, itemFile);
        p.sendMessage(ChatColor.GREEN + "Item updated successfully.");
    }
}
