package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Stamp extends SubCommand {

    private final CitemManager manager;
    public Stamp(CitemManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "stamp";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cPlease specify a player!");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            sender.sendMessage("§cThe specified player is not holding a valid item!");
            return;
        }

        manager.createStamp(target, item);
        sender.sendMessage("§aItem stamped successfully for " + target.getName() + "!");
    }
}
