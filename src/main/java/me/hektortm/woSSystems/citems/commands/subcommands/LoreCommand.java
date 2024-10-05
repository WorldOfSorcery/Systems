package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.SubCommand;
import me.hektortm.wosCore.Utils;
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
            Utils.error(sender, "general", "error.notplayer");
            return;
        }

        Player p = (Player) sender;

        if(!sender.hasPermission("citem.lore")) {
            Utils.error(sender, "general", "error.perms");
        }

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            Utils.error(sender, "citems", "error.holding-item");
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null) {
            Utils.error(sender, "citems", "error.no-meta");
            return;
        }

        if (args.length < 1) {
            Utils.error(sender, "citems", "error.usage.lore");
            return;
        }

        String loreCmd = args[0];
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        switch (loreCmd.toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    Utils.error(sender, "citems", "error.usage.lore-add");
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
                Utils.successMsg(p, "citems", "lore.added");
                break;

            case "edit":
                if (args.length < 3) {
                    Utils.error(sender, "citems", "error.usage.lore-edit");
                    return;
                }

                int row;
                try {
                    row = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    Utils.error(sender, "citems", "error.lore.invalid-row");
                    return;
                }

                if (row < 0 || row >= lore.size()) {
                    Utils.error(sender, "citems", "error.lore.out-of-bounds");
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
                Utils.successMsg(p, "citems", "lore.edited");
                break;

            case "remove":
                if (args.length < 2) {
                    Utils.error(sender, "citems", "error.usage.lore-remove");
                    return;
                }

                try {
                    row = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    Utils.error(sender, "citems", "error.lore.invalid-row");
                    return;
                }

                if (row < 0 || row >= lore.size()) {
                    Utils.error(sender, "citems", "error.lore.out-of-bounds");
                    return;
                }

                lore.remove(row);
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                Utils.successMsg(p, "citems", "lore.removed");
                break;

            default:
                Utils.successMsg(p, "citems", "error.usage.lore");
                break;
        }
    }
}
