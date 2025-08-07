package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class Lore extends SubCommand {

    @Override
    public String getName() {
        return "lore";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_LORE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        if (args.length < 1) {
            Utils.info(sender, "citems", "info.usage.lore");
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        String loreCmd = args[0];
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        switch (loreCmd.toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    Utils.info(sender, "citems", "info.usage.lore-add");
                    return;
                }

                StringBuilder addLoreText = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i > 1) {
                        addLoreText.append(" ");
                    }
                    addLoreText.append(args[i]);
                }

                lore.add(Utils.parseColorCodeString(addLoreText.toString()));
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                Utils.success(p, "citems", "lore.added");
                break;

            case "edit":
                if (args.length < 3) {
                    Utils.info(sender, "citems", "info.usage.lore-edit");
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

                lore.set(row, Utils.parseColorCodeString(editLoreText.toString()));
                meta.setLore(lore);
                itemInHand.setItemMeta(meta);
                Utils.success(p, "citems", "lore.edited");
                break;

            case "remove":
                if (args.length < 2) {
                    Utils.info(sender, "citems", "info.usage.lore-remove");
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
                Utils.success(p, "citems", "lore.removed");
                break;

            default:
                Utils.info(p, "citems", "info.usage.lore");
                break;
        }
    }
}
