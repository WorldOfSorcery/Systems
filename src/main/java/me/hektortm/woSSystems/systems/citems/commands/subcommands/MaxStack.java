package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MaxStack extends SubCommand {

    @Override
    public String getName() {
        return "maxstack";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_MAXSTACK;
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
            Utils.info(p, "citems", "info.usage.maxstack");
            return;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            item.resetData(DataComponentTypes.MAX_STACK_SIZE);
            Utils.success(p, "citems", "maxstack.reset");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            Utils.error(p, "citems", "error.maxstack.invalid");
            return;
        }

        if (amount < 1 || amount > 99) {
            Utils.error(p, "citems", "error.maxstack.range");
            return;
        }

        item.setData(DataComponentTypes.MAX_STACK_SIZE, amount);
        Utils.success(p, "citems", "maxstack.set", "%amount%", String.valueOf(amount));
    }
}
