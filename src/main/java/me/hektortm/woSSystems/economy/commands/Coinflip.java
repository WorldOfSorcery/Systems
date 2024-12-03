package me.hektortm.woSSystems.economy.commands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.EcoManager;
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

        if (!(sender instanceof Player p)) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        if (args.length < 2) {
            p.sendMessage("Usage: /coinflip <amount> <heads|tails>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            p.sendMessage("Invalid number.");
            return true;
        }

        if (amount <= 0) {
            p.sendMessage("The amount must be a positive number.");
            return true;
        }

        if (!ecoManager.hasEnoughCurrency(p.getUniqueId(), "gold", amount)) {
            p.sendMessage("You do not have enough gold.");
            return true;
        }

        String choice = args[1].toLowerCase();
        if (!choice.equals("heads") && !choice.equals("tails")) {
            p.sendMessage("Invalid argument. Use heads/tails.");
            return true;
        }

        p.sendMessage("Flipping Coin...");
        Bukkit.getScheduler().runTaskLater(plugin, () -> calculateResult(choice, amount, p), 20L);

        return true;
    }

    private void calculateResult(String choice, int amount, Player p) {
        ecoManager.modifyCurrency(p.getUniqueId(), "gold", amount, EcoManager.Operation.TAKE);

        String result = randomInt(2, 1) == 1 ? "heads" : "tails";

        if (choice.equals(result)) {
            int winnings = amount * 2;
            p.sendMessage(result.substring(0, 1).toUpperCase() + result.substring(1) + "! You win and received " + winnings + " gold!");
            ecoManager.modifyCurrency(p.getUniqueId(), "gold", winnings, EcoManager.Operation.GIVE);
        } else {
            p.sendMessage(result.substring(0, 1).toUpperCase() + result.substring(1) + "! You lose...");
        }
    }

    private int randomInt(int max, int min) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }
}
