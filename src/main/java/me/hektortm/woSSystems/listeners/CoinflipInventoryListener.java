package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.utils.dataclasses.Challenge;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;

public class CoinflipInventoryListener implements Listener {

    private final Map<UUID, Challenge> challengeQueue;
    private final EcoManager ecoManager;
    private final Coinflip coinflipCommand;
    private final LangManager lang;

    public CoinflipInventoryListener(Map<UUID, Challenge> challengeQueue, EcoManager ecoManager, Coinflip coinflipCommand, LangManager lang) {
        this.challengeQueue = challengeQueue;
        this.ecoManager = ecoManager;
        this.coinflipCommand = coinflipCommand;
        this.lang = lang;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getView().getTitle().equalsIgnoreCase(lang.getMessage("economy", "coinflip.gui.title"))) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) {
                Utils.error(player, "economy", "error.challenge-not-found");
                return;
            }

            UUID challengerUUID = meta.getOwningPlayer().getUniqueId();

            if (!challengeQueue.containsKey(challengerUUID)) {
                Utils.error(player, "economy", "error.challenge-not-found");
                return;
            }

            Player challenger = Bukkit.getPlayer(challengerUUID);
            if (challenger == null) {
                Utils.error(player, "economy", "error.challenger-offline");
                return;
            }
            Challenge challenge = challengeQueue.get(challengerUUID);

            if (!ecoManager.hasEnoughCurrency(player.getUniqueId(), "gold", challenge.getAmount())) {
                Utils.error(player, "economy", "error.funds");
                return;
            }

            player.closeInventory();
            coinflipCommand.acceptChallenge(player, challenger);
        }
    }

}
