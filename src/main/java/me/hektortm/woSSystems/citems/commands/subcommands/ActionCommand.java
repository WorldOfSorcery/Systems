package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ActionCommand extends SubCommand {
    @Override
    public String getName() {
        return "action";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }
        p.sendMessage("TODO: Interactions");
        //TODO add when interactions are done
    }
}
