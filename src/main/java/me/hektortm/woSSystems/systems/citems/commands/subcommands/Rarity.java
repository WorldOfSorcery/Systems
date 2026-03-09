package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.Rarities;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Rarity extends SubCommand {

    @Override
    public String getName() {
        return "rarity";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_RARITY;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        if (args.length != 1) {
            Utils.info(p, "citems", "info.usage.rarity");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        if (args[0].equalsIgnoreCase("reset")) {
            data.remove(Keys.CUSTOM_RARITY.get());
            item.setItemMeta(meta);
            Utils.success(p, "citems", "rarity.reset");
            return;
        }

        Rarities rarity = Rarities.fromName(args[0]);
        if (rarity == null) {
            Utils.error(p, "citems", "error.rarity.invalid");
            return;
        }

        data.set(Keys.CUSTOM_RARITY.get(), PersistentDataType.STRING, rarity.name());
        item.setItemMeta(meta);
        Utils.success(p, "citems", "rarity.set",
                "%rarity%", rarity.getColor() + rarity.getName());
    }
}
