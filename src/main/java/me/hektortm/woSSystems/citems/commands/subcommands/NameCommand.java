package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NameCommand extends SubCommand {


    @Override
    public String getName() {
        return "name";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if(!sender.hasPermission("citem.name")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /citem name <NAME>");
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
        // Concatenate all arguments from index 1 to the end to form the name
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(args[i]);
        }
        String name = ChatColor.translateAlternateColorCodes('&', nameBuilder.toString());
        meta.setDisplayName(name);
        itemInHand.setItemMeta(meta);
        p.sendMessage(ChatColor.GREEN + "Item name set to: " + name);
    }
}
