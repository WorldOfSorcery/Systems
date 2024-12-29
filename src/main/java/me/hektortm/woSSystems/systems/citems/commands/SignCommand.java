package me.hektortm.woSSystems.systems.citems.commands;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SignCommand implements CommandExecutor {

    private final CitemManager manager;
    private final StatsManager stats;

    //TODO Currency instead of stat maybe

    public SignCommand(CitemManager manager, StatsManager stats) {
        this.manager = manager;
        this.stats = stats;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player target = (Player) sender;

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            sender.sendMessage("§cNot valid item!");
            return true;
        }

        if (stats.getPlayerStat(target.getUniqueId(), "core_sign") == 0) {
            sender.sendMessage("§cNot enough currency.");
            return true;
        }

        manager.createStamp(target, item);
        sender.sendMessage("§aItem stamped successfully for " + target.getName() + "!");

        return true;
    }
}
