package me.hektortm.woSSystems.economy.commands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Coinflip implements CommandExecutor {

    private final EcoManager ecoManager;
    private final WoSSystems plugin;

    public Coinflip(EcoManager ecoManager, WoSSystems plugin) {
        this.ecoManager = ecoManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!PermissionUtil.isPlayer(sender)) return true;

        if(!PermissionUtil.hasPermission(sender, Permissions.ECONOMY_COINFLIP)) return true;

        Player p = (Player) sender;

        if (args.length < 2) {
            Utils.error(p, "economy", "error.coinflip-usage");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            Utils.error(p, "economy", "error.invalid-amount");
            return true;
        }

        if (amount <= 0) {
            Utils.error(p, "economy", "error.invalid-amount-positive");
            return true;
        }

        if (!ecoManager.hasEnoughCurrency(p.getUniqueId(), "gold", amount)) {
            Utils.error(p, "economy", "error.funds");
            return true;
        }

        String choice = args[1].toLowerCase();
        if (!choice.equals("heads") && !choice.equals("tails")) {
            Utils.error(p, "economy", "error.coinflip-usage");
            return true;
        }

        Utils.successMsg(p, "economy", "coinflip.flipping");
        Bukkit.getScheduler().runTaskLater(plugin, () -> calculateResult(choice, amount, p), 20L);

        return true;
    }

    private void calculateResult(String choice, int amount, Player p) {
        ecoManager.modifyCurrency(p.getUniqueId(), "gold", amount, EcoManager.Operation.TAKE);

        String result = randomInt(2, 1) == 1 ? "heads" : "tails";

        if (choice.equals(result)) {
            int winnings = amount * 2;
            Utils.successMsg2Values(p, "economy", "coinflip.win", "%result%", result.substring(0, 1).toUpperCase() + result.substring(1), "%amount%", String.valueOf(winnings));
            ecoManager.modifyCurrency(p.getUniqueId(), "gold", winnings, EcoManager.Operation.GIVE);
        } else {
            Utils.successMsg1Value(p, "economy", "coinflip.loss", "%result%", result.substring(0, 1).toUpperCase() + result.substring(1));
        }
    }

    private int randomInt(int max, int min) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }
}
