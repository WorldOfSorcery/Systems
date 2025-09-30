package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;

public class Equippable extends SubCommand {
    @Override
    public String getName() {
        return "equippable";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_EQUIPPABLE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        if (args.length != 1 ) {
            Utils.info(sender, "citems", "info.usage.equippable");
            return;
        }
        String slotArg = args[0];
        EquipmentSlot slot;


        switch (slotArg) {
            case "head" -> slot = EquipmentSlot.HEAD;
            case "chest" -> slot = EquipmentSlot.CHEST;
            case "legs" -> slot = EquipmentSlot.LEGS;
            case "feet" -> slot = EquipmentSlot.FEET;
            default -> {
                Utils.info(sender, "citems", "info.usage.equippable");
                return;
            }
        }



        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        EquippableComponent comp = item.getItemMeta().getEquippable();
        comp.setSlot(slot);
        meta.setEquippable(comp);
        item.setItemMeta(meta);
        Utils.success(p, "citems", "equippable", "%slot%", slotArg);
    }
}
