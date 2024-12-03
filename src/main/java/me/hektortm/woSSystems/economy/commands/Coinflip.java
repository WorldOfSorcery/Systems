package me.hektortm.woSSystems.economy.commands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.dataclasses.Challenge;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Coinflip implements CommandExecutor {

    private final EcoManager ecoManager;
    private final WoSSystems plugin;

    // Map to store active challenges
    private final Map<UUID, Challenge> challengeQueue = new HashMap<>();

    public Coinflip(EcoManager ecoManager, WoSSystems plugin) {
        this.ecoManager = ecoManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return true;
        if (!PermissionUtil.hasPermission(sender, Permissions.ECONOMY_COINFLIP)) return true;

        Player p = (Player) sender;

        if (args.length == 0) {
            // No arguments: Show active challenges
            openChallengeMenu(p);
            return true;
        }

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

        // Add the player to the challenge queue
        Challenge challenge = new Challenge(p.getUniqueId(), amount, choice);
        challengeQueue.put(p.getUniqueId(), challenge);
        Utils.successMsg2Values(p, "economy", "coinflip.created", "%amount%", String.valueOf(amount), "%choice%", choice);

        return true;
    }

    private void openChallengeMenu(Player p) {
        if (challengeQueue.isEmpty()) {
            Utils.error(p, "economy", "error.no-challenges");
            return;
        }

        Inventory menu = Bukkit.createInventory(null, 27, "Coinflip Challenges");
        challengeQueue.forEach((uuid, challenge) -> {
            Player challenger = Bukkit.getPlayer(uuid);
            if (challenger == null) return;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            assert meta != null;

            meta.setOwningPlayer(challenger); // Use the Bukkit API to set the player
            meta.setDisplayName(challenger.getName());
            meta.setLore(List.of(
                    "Bet: " + challenge.getAmount() + " gold",
                    "Choice: " + challenge.getChoice()
            ));
            head.setItemMeta(meta);

            menu.addItem(head);
        });

        p.openInventory(menu);
    }
    public void acceptChallenge(Player acceptor, Player challenger) {
        if (!challengeQueue.containsKey(challenger.getUniqueId())) {
            Utils.error(acceptor, "economy", "error.challenge-not-found");
            return;
        }

        Challenge challenge = challengeQueue.get(challenger.getUniqueId());
        int amount = challenge.getAmount();

        if (!ecoManager.hasEnoughCurrency(acceptor.getUniqueId(), "gold", amount)) {
            Utils.error(acceptor, "economy", "error.funds");
            return;
        }

        Utils.successMsg(acceptor, "economy", "coinflip.flipping");
        Bukkit.getScheduler().runTaskLater(plugin, () -> resolveChallenge(acceptor, challenger, challenge), 20L);
    }

    private void resolveChallenge(Player acceptor, Player challenger, Challenge challenge) {
        challengeQueue.remove(challenger.getUniqueId()); // Remove from queue

        // Deduct gold from both players
        int amount = challenge.getAmount();
        ecoManager.modifyCurrency(acceptor.getUniqueId(), "gold", amount, EcoManager.Operation.TAKE);
        ecoManager.modifyCurrency(challenger.getUniqueId(), "gold", amount, EcoManager.Operation.TAKE);

        // Determine winner
        String result = randomInt(2, 1) == 1 ? "heads" : "tails";
        Player winner = result.equals(challenge.getChoice()) ? challenger : acceptor;
        Player loser = winner.equals(challenger) ? acceptor : challenger;

        // Transfer gold
        int winnings = amount * 2;
        ecoManager.modifyCurrency(winner.getUniqueId(), "gold", winnings, EcoManager.Operation.GIVE);

        // Notify players
        Utils.successMsg2Values(winner, "economy", "coinflip.win", "%result%", result, "%amount%", String.valueOf(winnings));
        Utils.successMsg1Value(loser, "economy", "coinflip.loss", "%result%", result);
    }

    private int randomInt(int max, int min) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }
}
