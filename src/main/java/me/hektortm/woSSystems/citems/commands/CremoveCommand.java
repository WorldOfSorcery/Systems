package me.hektortm.woSSystems.citems.commands;


import me.hektortm.woSSystems.citems.core.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CremoveCommand implements CommandExecutor {

    private final DataManager data;
    public CremoveCommand(DataManager data) {
        this.data = data;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("cremove")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /cremove <NAME> <ID> <amount>");
            }

            Player t = Bukkit.getPlayer(args[0]);
            String id = args[1];
            Integer amount = 1;
            if (args.length == 3) {
                amount = Integer.parseInt(args[2]);
            }

            File dir = new File(Bukkit.getServer().getPluginManager().getPlugin("WoSCitems").getDataFolder(), "citems");
            if (!dir.exists()) {
                sender.sendMessage(ChatColor.RED + "No custom items found.");
                return true;
            }

            File itemFile = new File(dir, id + ".json");
            if (!itemFile.exists()) {
                sender.sendMessage(ChatColor.RED + "No custom items found.");
                return true;
            }
            ItemStack savedItem = data.loadItemFromFile(itemFile);
            ItemStack item = savedItem.clone();

            item.setAmount(amount);
            if (savedItem == null) {
                sender.sendMessage(ChatColor.RED + "No custom items found.");
                return true;
            }
            t.getInventory().removeItem(item);
            sender.sendMessage(ChatColor.GREEN + amount.toString() + " Custom item(s) removed.");

        }
        return true;
    }
}
