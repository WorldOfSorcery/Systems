package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.chat.NicknameManager;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.utils.dataclasses.Challenge;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final Map<UUID, Challenge> challengeQueue;
    private final EcoManager ecoManager;
    private final Coinflip coinflipCommand;
    private final LangManager lang;
    private final NicknameManager nickManager;
    private final Map<UUID, String> nickRequests;

    public InventoryClickListener(Map<UUID, Challenge> challengeQueue, EcoManager ecoManager, Coinflip coinflipCommand, LangManager lang, Map<UUID, String> nickRequests, NicknameManager nickManager) {
        this.challengeQueue = challengeQueue;
        this.ecoManager = ecoManager;
        this.coinflipCommand = coinflipCommand;
        this.lang = lang;
        this.nickManager = nickManager;
        this.nickRequests = nickRequests;
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
        if (event.getView().getTitle().equalsIgnoreCase(lang.getMessage("chat", "nick.gui.title"))) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) item.getItemMeta();

            UUID requesterUUID = meta.getOwningPlayer().getUniqueId();
            if (!nickRequests.containsKey(requesterUUID)) {
                // error here
            }

            OfflinePlayer p = Bukkit.getOfflinePlayer(requesterUUID);
            if (event.getAction().equals(ClickType.LEFT)) {
                nickManager.approveNicknameChange(p);
            }
            else if (event.getAction().equals(ClickType.RIGHT)) {
                nickManager.denyNicknameChange(p);
            }

            player.closeInventory();
        }

    }

}
