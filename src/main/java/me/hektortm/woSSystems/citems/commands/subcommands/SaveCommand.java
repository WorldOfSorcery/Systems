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
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
        }

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if(!sender.hasPermission("citem.save")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        }

        if (args.length != 1) {
            p.sendMessage(ChatColor.RED + "Usage: /citem save [id]");
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

        if (!cmd.citemsFolder.exists()) {
            cmd.citemsFolder.mkdirs();
        }
        File itemFile = new File(cmd.citemsFolder, id + ".json");
        if (itemFile.exists()) {
            p.sendMessage(ChatColor.RED + "An item with this ID already exists.");
            return;
        }
        data.saveItemToFile(itemInHand, itemFile, id);
        p.sendMessage(ChatColor.GREEN + "Item saved successfully as " + id + ".json");
    }
}
