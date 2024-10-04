package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.SubCommand;
import me.hektortm.woSSystems.citems.core.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class FlagCommand extends SubCommand {

    private final NamespacedKey undroppableKey;
    public FlagCommand(DataManager data) {
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
    }


    @Override
    public String getName() {
        return "flag";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if(!sender.hasPermission("citem.flag")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        }

        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "Usage: /citem flag <add|remove> <FLAG>");
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
        String flagCmd = args[0];
        String flag = args[1].toLowerCase();
        switch (flagCmd.toLowerCase()) {
            case "add":
                if (flag.equals("undroppable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(undroppableKey, PersistentDataType.BYTE, (byte) 1);
                    p.sendMessage(ChatColor.GREEN + "Undroppable flag added.");
                }
                break;

            case "remove":
                if (flag.equals("undroppable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.remove(undroppableKey);
                    p.sendMessage(ChatColor.GREEN + "Undroppable flag removed.");
                }
                break;

            default:
                p.sendMessage(ChatColor.RED + "Usage: /citem flag <add|remove> <FLAG>");
                return;
        }
        itemInHand.setItemMeta(meta);
    }
}
