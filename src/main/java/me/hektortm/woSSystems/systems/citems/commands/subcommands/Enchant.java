package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static io.papermc.paper.datacomponent.item.DyedItemColor.dyedItemColor;
import static me.hektortm.woSSystems.utils.Permissions.CITEM_ENCHANT;

public class Enchant extends SubCommand {
    @Override
    public String getName() {
        return "enchant";
    }

    @Override
    public Permissions getPermission() {
        return CITEM_ENCHANT;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        boolean hasEnchant = meta.getEnchantmentGlintOverride();
        meta.setEnchantmentGlintOverride(!hasEnchant);
        item.setItemMeta(meta);

        Utils.success(p, "citems", "enchant");
    }
}
