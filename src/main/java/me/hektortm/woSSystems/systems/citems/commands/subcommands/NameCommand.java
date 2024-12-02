package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NameCommand extends SubCommand {


    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_RENAME;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if (args.length < 1) {
            Utils.error(p, "citems", "error.usage.rename");
            return;
        }

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }
        if (meta == null) {
            Utils.error(p, "citems", "error.no-meta");
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
        Utils.successMsg1Value(p, "citems", "renamed", "%name%", name);
    }
}
