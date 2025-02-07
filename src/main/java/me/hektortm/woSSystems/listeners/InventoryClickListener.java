package me.hektortm.woSSystems.listeners;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.utils.dataclasses.Challenge;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final EcoManager ecoManager;
    private final Coinflip coinflipCommand;
    private final LangManager lang;
    private final NicknameManager nickManager;
    private final Map<UUID, String> nickRequests;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;


    public InventoryClickListener(EcoManager ecoManager, Coinflip coinflipCommand, LangManager lang, Map<UUID, String> nickRequests, NicknameManager nickManager, DAOHub hub) {
        this.ecoManager = ecoManager;
        this.coinflipCommand = coinflipCommand;
        this.lang = lang;
        this.nickManager = nickManager;
        this.nickRequests = nickRequests;
        this.hub = hub;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getClickedInventory();
        if (inv == null) return;
        if (inv == plugin.getCosmeticManager().mainPage) {
            int slot = event.getSlot();
            switch (slot) {
                case 2:
                    plugin.getCosmeticManager().openTitlesPage(player);
                    break;
                case 4:
                    plugin.getCosmeticManager().openPrefixPage(player);
                    break;
                case 6:
                    plugin.getCosmeticManager().openBadgePage(player);
                default:
                    break;
            }
            event.setCancelled(true);

        }
        if (inv == plugin.getCosmeticManager().titlesPage) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;
            ItemMeta clickedItemMeta = clickedItem.getItemMeta();
            PersistentDataContainer data = clickedItemMeta.getPersistentDataContainer();
            String titleID = data.get(new NamespacedKey(plugin, "titleID"), PersistentDataType.STRING);

            if (hub.getTitlesDAO().getCurrentTitleID(player) != null && Objects.equals(hub.getTitlesDAO().getCurrentTitleID(player), titleID)) {
                event.setCancelled(true);
                return;
            }

            hub.getTitlesDAO().equipTitle(player, titleID);
            player.sendMessage("Equipped Title: " + hub.getTitlesDAO().getTitleText(titleID));
            event.setCancelled(true);
            player.closeInventory();
        }
        if (inv == plugin.getCosmeticManager().prefixPage) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;
            ItemMeta clickedItemMeta = clickedItem.getItemMeta();
            PersistentDataContainer data = clickedItemMeta.getPersistentDataContainer();
            String prefixID = data.get(new NamespacedKey(plugin, "prefixID"), PersistentDataType.STRING);

            if (hub.getPrefixDAO().getCurrentPrefixID(player) != null && hub.getPrefixDAO().getCurrentPrefixID(player).equals(prefixID)) {
                event.setCancelled(true);
                return;
            }

            hub.getPrefixDAO().equipPrefix(player, prefixID);
            player.sendMessage("Equipped Prefix: " + hub.getPrefixDAO().getPrefixText(prefixID));
            event.setCancelled(true);
            player.closeInventory();
        }
        if (inv == plugin.getCosmeticManager().badgesPage) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;
            ItemMeta clickedItemMeta = clickedItem.getItemMeta();
            PersistentDataContainer data = clickedItemMeta.getPersistentDataContainer();
            String badgeID = data.get(new NamespacedKey(plugin, "badgeID"), PersistentDataType.STRING);

            if (hub.getBadgeDAO().getCurrentBadgeID(player) != null && hub.getBadgeDAO().getCurrentBadgeID(player).equals(badgeID)) {
                event.setCancelled(true);
                return;
            }

            hub.getBadgeDAO().equipBadge(player, badgeID);
            player.sendMessage("Equipped Badge: " + hub.getBadgeDAO().getBadgeText(badgeID));
            event.setCancelled(true);
            player.closeInventory();
        }
        if (inv.getType().equals(InventoryType.DISPENSER) && event.getView().getTitle().equals("Viewing Item")) {
            event.setCancelled(true);
        }
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

            if(player.getUniqueId().equals(challengerUUID)) {
                coinflipCommand.cancelChallenge(player);
                return;
            }

            if (!coinflipCommand.challengeQueue.containsKey(challengerUUID)) {
                Utils.error(player, "economy", "error.challenge-not-found");
                return;
            }

            Player challenger = Bukkit.getPlayer(challengerUUID);
            if (challenger == null) {
                Utils.error(player, "economy", "error.challenger-offline");
                return;
            }
            Challenge challenge = coinflipCommand.challengeQueue.get(challengerUUID);

            if (!ecoManager.hasEnoughCurrency(player.getUniqueId(), "gold", challenge.getAmount())) {
                Utils.error(player, "economy", "error.funds");
                return;
            }

            player.closeInventory();
            coinflipCommand.acceptChallenge(player, challenger);
        }
        if (event.getView().getTitle().equalsIgnoreCase(lang.getMessage("chat", "nick.gui.title"))) {
            event.setCancelled(true); // Cancel all inventory actions in this GUI

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() != Material.PLAYER_HEAD) {
                return; // If the clicked item is null or not a player head, exit
            }

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) {
                return; // If metadata or owning player is invalid, exit
            }

            UUID requesterUUID = meta.getOwningPlayer().getUniqueId();




            OfflinePlayer p = Bukkit.getOfflinePlayer(requesterUUID);
            Player p1 = Bukkit.getPlayer(requesterUUID);
            ClickType ct = event.getClick();

            String nick = nickRequests.get(requesterUUID);

            boolean isReserved = nickManager.reservedNicks.values().stream()
                    .anyMatch(reservedNick -> reservedNick.equalsIgnoreCase(nick));

            if (isReserved) {
                UUID reservedBy = nickManager.reservedNicks.entrySet().stream()
                        .filter(entry -> entry.getValue().equalsIgnoreCase(nick))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                // If the nickname is reserved by someone else, show a warning
                if (reservedBy != null && !reservedBy.equals(requesterUUID)) {
                    nickManager.denyNicknameChange(p);
                }
            }

            // Handle left and right clicks
            if (ct == ClickType.LEFT || ct == ClickType.SHIFT_LEFT) {
                nickManager.approveNicknameChange(p.getUniqueId());
                if (nick.equals("reset")) {
                    Utils.successMsg1Value(p1, "chat", "nick.approved-reset", "%nick%", nick);
                    Utils.successMsg1Value(player, "chat", "nick.approved-reset-staff", "%player%", p1.getName());
                    player.closeInventory();
                    return;
                }
                Utils.successMsg1Value(p1, "chat", "nick.approved", "%nick%", nick);
                Utils.successMsg1Value(player, "chat", "nick.approved-staff", "%player%", p1.getName());
            } else if (ct == ClickType.RIGHT || ct == ClickType.SHIFT_RIGHT) {
                nickManager.denyNicknameChange(p);
                Utils.successMsg1Value(p1, "chat", "nick.declined", "%nick%", nick);
                Utils.successMsg1Value(player, "chat", "nick.declined-staff", "%player%", p1.getName());

            }

            // Close the player's inventory after the action
            player.closeInventory();
        }

    }
}
