package me.hektortm.woSSystems.systems.citems.commands;

import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.stats.utils.Operation;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SignCommand implements CommandExecutor {

    private final CitemManager manager;
    private final EcoManager eco;

    //TODO Currency instead of stat maybe

    public SignCommand(CitemManager manager, EcoManager eco) {
        this.manager = manager;
        this.eco = eco;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player target = (Player) sender;

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            Utils.error(target, "citems", "error.invalid-item");
            return true;
        }

        if (eco.getCurrencyBalance(target.getUniqueId(), "signature_token") == 0) {
            Utils.error(target, "economy", "error.funds");
            return true;
        }

        manager.createStamp(target, item);
        eco.modifyCurrency(target.getUniqueId(), "signature_token", 1, EcoManager.Operation.TAKE);
        Utils.successMsg(target, "citems", "stamp");

        return true;
    }
}
