package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Food extends SubCommand {

    @Override
    public String getName() {
        return "food";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_FOOD;
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

        if (args.length == 1 && args[0].equalsIgnoreCase("remove")) {
            item.resetData(DataComponentTypes.FOOD);
            Utils.success(p, "citems", "food.removed");
            return;
        }

        // /citem food <nutrition> <saturation> [can_always_eat]
        if (args.length < 2) {
            Utils.info(p, "citems", "info.usage.food");
            return;
        }

        int nutrition;
        float saturation;
        try {
            nutrition = Integer.parseInt(args[0]);
            saturation = Float.parseFloat(args[1]);
        } catch (NumberFormatException e) {
            Utils.error(p, "citems", "error.food.invalid-numbers");
            return;
        }

        boolean canAlwaysEat = args.length >= 3 && args[2].equalsIgnoreCase("true");

        FoodProperties food = FoodProperties.food()
                .nutrition(nutrition)
                .saturation(saturation)
                .canAlwaysEat(canAlwaysEat)
                .build();

        item.setData(DataComponentTypes.FOOD, food);
        Utils.success(p, "citems", "food.set", "%nutrition%", String.valueOf(nutrition), "%saturation%", String.valueOf(saturation));
    }
}
