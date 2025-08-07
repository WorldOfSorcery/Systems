package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.material.Colorable;

import java.util.ArrayList;
import java.util.List;

import static io.papermc.paper.datacomponent.item.DyedItemColor.dyedItemColor;

public class Color extends SubCommand {
    @Override
    public String getName() {
        return "color";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_COLOR;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        if (args.length != 1 ) {
            Utils.info(sender, "citems", "info.usage.color");
            return;
        }

        String color = args[0];
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        ItemMeta meta = item.getItemMeta();

        DyedItemColor dyedColor = dyedItemColor(hexToBukkitColor(color, p));


        item.setData(DataComponentTypes.DYED_COLOR, dyedColor);
        Utils.success(p, "citems", "color", "%color%", color);
    }

    public static org.bukkit.Color hexToBukkitColor(String hex, Player p) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() != 6) {
            Utils.error(p, "citems", "error.invalid-color");
            return null;
        }

        try {
            int red = Integer.parseInt(hex.substring(0, 2), 16);
            int green = Integer.parseInt(hex.substring(2, 4), 16);
            int blue = Integer.parseInt(hex.substring(4, 6), 16);

            return org.bukkit.Color.fromRGB(red, green, blue);
        } catch (NumberFormatException e) {
            Utils.error(p, "citems", "error.invalid-color-format", "%color%", hex);
            return null;
        }
    }
}
