package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Tooltip extends SubCommand {
    @Override
    public String getName() {
        return "tooltip";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_TOOLTIP;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        if (args.length != 1 ) {
            Utils.info(sender, "citems", "info.usage.tooltip");
            return;
        }

        String parameter = args[0];
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (parameter.equals("hide")) {
            if (meta.isHideTooltip()) {
                meta.setHideTooltip(false);
                item.setItemMeta(meta);
                Utils.success(p, "citems", "tooltip.shown");
            } else {
                meta.setHideTooltip(true);
                item.setItemMeta(meta);
                Utils.success(p, "citems", "tooltip.hidden");
            }
            return;
        }
        NamespacedKey tooltip = new NamespacedKey("minecraft", parameter);
        meta.setTooltipStyle(tooltip);
        item.setItemMeta(meta);
        Utils.success(p, "citems", "tooltip.set", "%tooltip_id%", parameter);
    }
}
