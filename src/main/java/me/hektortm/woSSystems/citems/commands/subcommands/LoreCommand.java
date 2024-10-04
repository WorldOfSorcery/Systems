package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreCommand extends SubCommand {
    @Override
    public String getName() {
        return "lore";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return;
        }

        Player p = (Player) sender;

        if(!sender.hasPermission("citem.lore")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        }

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            p.sendMessage(ChatColor.RED + "You must be holding an item to use this command.");
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null) {
            p.sendMessage(ChatColor.RED + "This item has no metadata.");
            return;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /citem lore <add|edit|remove> [arguments]");
            return;
        }

        String loreCmd = args[0];
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        switch (loreCmd.toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /citem lore add <TEXT>");
                    return;
                }

                StringBuilder addLoreText = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i > 1) {
                        addLoreText.append(" ");
                    }
                    addLoreText.append(args[i]);
                }

                lore.add(ChatColor.translateAlternateColorCodes('&', addLoreText.toString()));
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Lore added.");
                break;

            case "edit":
                if (args.length < 3) {
                    p.sendMessage(ChatColor.RED + "Usage: /citem lore edit <ROW> <TEXT>");
                    return;
                }

                int row;
                try {
                    row = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Invalid row number.");
                    return;
                }

                if (row < 0 || row >= lore.size()) {
                    p.sendMessage(ChatColor.RED + "Lore row out of bounds.");
                    return;
                }

                StringBuilder editLoreText = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i > 2) {
                        editLoreText.append(" ");
                    }
                    editLoreText.append(args[i]);
                }

                lore.set(row, ChatColor.translateAlternateColorCodes('&', editLoreText.toString()));
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Lore edited.");
                break;

            case "remove":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /citem lore remove <ROW>");
                    return;
                }

                try {
                    row = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Invalid row number.");
                    return;
                }

                if (row < 0 || row >= lore.size()) {
                    p.sendMessage(ChatColor.RED + "Lore row out of bounds.");
                    return;
                }

                lore.remove(row);
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Lore removed.");
                break;

            default:
                p.sendMessage(ChatColor.RED + "Invalid command. Usage: /citem lore <add|edit|remove> [arguments]");
                break;
        }
    }
}
