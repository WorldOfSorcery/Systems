package me.hektortm.woSSystems.systems.citems;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.CitemDAO;
import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.ErrorHandler;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Sounds;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static me.hektortm.woSSystems.utils.Icons.SIGNED_BY;
import static me.hektortm.woSSystems.utils.Icons.TIME;
import static me.hektortm.woSSystems.utils.Letters.*;


public class CitemManager {

    private final NamespacedKey undroppableKey;
    private final NamespacedKey unusableKey;
    private final NamespacedKey idKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "id");
    private final NamespacedKey leftActionKey;
    private final NamespacedKey rightActionKey;
    private final NamespacedKey timeKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey quoteKey;
    public final NamespacedKey ownerKey;
    public final NamespacedKey placeableKey;
    public final NamespacedKey profileBgKey;
    public final NamespacedKey profilePicKey;
    public final File citemFolder;

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    private InteractionManager interactionManager;
    private final LogManager log = new LogManager(new LangManager(WoSCore.getPlugin(WoSCore.class)),WoSCore.getPlugin(WoSCore.class));
    private final LangManager lang = new LangManager(WoSCore.getPlugin(WoSCore.class));
    private final NicknameManager nickManager;
    private final CitemCommand cmd;
    private final Parsers parsers = new Parsers();
    private final ErrorHandler errorHandler = new ErrorHandler();


    public CitemManager(DAOHub hub) {
        this.hub = hub;
        nickManager = new NicknameManager(hub);
        citemFolder = new File(plugin.getDataFolder(), "citems");
        cmd = new CitemCommand(this, interactionManager);
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
        unusableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "unusable");
        leftActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-left");
        rightActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-right");
        timeKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "stamp-time");
        nameKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "stamp-name");
        quoteKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "stamp-quote");
        ownerKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "owner");
        placeableKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "placeable");
        profileBgKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "profile-bg");
        profilePicKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "profile-pic");

    }

    public void setInteractionManager(InteractionManager interactionManager) {
        if (interactionManager == null) {
            throw new IllegalArgumentException("ConditionHandler cannot be null.");
        }
        this.interactionManager = interactionManager;
    }


    public void giveCitem(CommandSender s, Player t, String id, Integer amount) {
        ItemStack itemToGive = hub.getCitemDAO().getCitem(id);

        if (itemToGive == null) {
            s.sendMessage("[Database] Item is §onull");
            return;
        }

        itemToGive.setAmount(amount);
        t.getInventory().addItem(itemToGive);
        t.playSound(t.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1 ,1);
        Utils.success(s, "citems", "given", "%amount%", String.valueOf(amount), "%id%", id, "%player%", t.getName());
    }


    public boolean isCitem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(idKey, PersistentDataType.STRING); // Retrieve the ID
        return itemId != null && !itemId.isEmpty(); // Return true if ID exists and is not empty
    }


    public int citemAmount(Player p, String id) {
        if (p == null || id == null) {
            return 0;
        }

        PlayerInventory inv = p.getInventory();
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item == null || !item.hasItemMeta()) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            PersistentDataContainer data = meta.getPersistentDataContainer();
            String itemId = data.get(idKey, PersistentDataType.STRING);
            if (id.equals(itemId)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public void updateItem(Player p) {
        ItemStack item = p.getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return;

        // Check if lore is null (for items without lore)
        List<String> copyLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // Proceed if there is lore or an empty list
        int length = copyLore.size();

        String nameKey2 = "null";
        String time = "null";
        String quote = "null";

        // Process lore to preserve "Time" and "Obtained by" and remove others
        if (length > 1) {
            String indLast = copyLore.get(length - 1);
            String indSecLast = copyLore.get(length - 2);

            // If "Signed By" is found, capture its value and process the rest
            if (indSecLast.contains(SIGNED_BY.getIcon())) {
                nameKey2 = indSecLast.split(SIGNED_BY.getIcon() + " ")[1];

                // Check the last line for "Time" or "Quote" and capture appropriately
                if (indLast.contains(QUOTE.getLetter())) {
                    quote = indLast;
                } else if (indLast.contains(TIME.getIcon())) {
                    time = indLast.split(TIME.getIcon() + " ")[1];
                }

                // Safely remove the last 3 lore entries containing "Time", "Obtained by", and "Quote"
                int itemsToRemove = Math.min(3, copyLore.size());
                for (int i = 0; i < itemsToRemove; i++) {
                    copyLore.remove(copyLore.size() - 1);
                }
            }
        }

        // Remove "Time" and "Obtained by" entries from the filtered lore list (do not touch these in the update)
        List<String> filteredLore = new ArrayList<>();
        for (String loreLine : copyLore) {
            if (!loreLine.contains(SIGNED_BY.getIcon()) && (!loreLine.contains(TIME.getIcon()) || !loreLine.contains(QUOTE.getLetter()))) {
                filteredLore.add(loreLine);
            }
        }

        // Now check if the filtered lore is different from the original lore
        boolean loreChanged = !filteredLore.equals(copyLore);

        // If the lore was changed, update it on the item
        if (loreChanged) {
            meta.setLore(filteredLore);
            item.setItemMeta(meta);
        }

        // Check if item has valid meta and persistent data
        if (!item.hasItemMeta() || meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(idKey, PersistentDataType.STRING);

        if (itemId != null) {

            if (!hub.getCitemDAO().citemExists(itemId)) {
                // Remove the item from the player's inventory if the file doesn't exist
                p.getInventory().remove(item);
                Utils.successMsg1Value(p, "citems", "update.removed", "%item%", item.getItemMeta().getDisplayName());
                p.playSound(p, Sound.ITEM_SHIELD_BREAK, 1F, 1F);
                return;
            }

            // Load the item data from the file
            ItemStack savedItem = hub.getCitemDAO().getCitem(itemId);

            // Compare the item meta and update if necessary
            if (savedItem != null && !item.isSimilar(savedItem)) {
                ItemMeta newMeta = savedItem.getItemMeta();
                if (newMeta != null) {
                    // Keep the "Time" and "Obtained by" intact, but update other lore entries
                    List<String> newLore = newMeta.getLore();
                    if (newLore == null) {
                        newLore = new ArrayList<>();
                    }

                    // Re-attach "Time" and "Obtained by" to the new lore
                    if (nameKey2 != null && !nameKey2.equals("null")) {
                        newLore.add("§7");
                        newLore.add("§f" + SIGNED_BY.getIcon() + " §e" + nameKey2);

                        // Add "Quote" if present, otherwise add "Time"
                        if (quote != null && !quote.equals("null")) {
                            newLore.add("§e" + quote);
                        } else if (time != null && !time.equals("null")) {
                            newLore.add("§f" + TIME.getIcon() + " §e" + time);
                        }
                    }

                    // Only set lore if it's not null or empty
                    if (!newLore.isEmpty()) {
                        newMeta.setLore(newLore);
                    }
                    savedItem.setItemMeta(newMeta);

                    // Update the player's item to match the saved data
                    if (!newMeta.equals(meta)) {  // Only update if the new meta is different
                        item.setItemMeta(newMeta);
                        Utils.successMsg1Value(p, "citems", "update.updated", "%item%", item.getItemMeta().getDisplayName());
                        Sounds.sucess(p);
                    }
                }
            }
        } else {
            log.sendWarning(p.getName() + ": Item \"" + item.getItemMeta().getDisplayName() + "%red_300%\" -> no valid ID");
        }
    }

    public boolean hasCitemAmount(Player p, String id, int amount) {
        ItemStack citem = hub.getCitemDAO().getCitem(id);

        if (citem == null) return false;

        int found = 0;

        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) continue;

            if (item.isSimilar(citem)) {
                found += item.getAmount();
                if (found >= amount) {
                    return true;
                }
            }
        }

        return false;
    }



    public void leftClickAction(Player p) {
        ItemStack item = p.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey leftActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-left");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String actionId = data.get(leftActionKey, PersistentDataType.STRING);
        if (actionId != null) {
            interactionManager.triggerInteraction(p, actionId);
        }
    }

    public void rightClickAction(Player p) {
        ItemStack item = p.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey rightActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-right");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String actionId = data.get(rightActionKey, PersistentDataType.STRING);
        if (actionId != null) {
            interactionManager.triggerInteraction(p, actionId);
        }
    }

    private void setStamp(ItemStack item) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String time = data.get(timeKey, PersistentDataType.STRING);
        String name = data.get(nameKey, PersistentDataType.STRING);
        String quote = data.get(quoteKey, PersistentDataType.STRING);

        System.out.println("Setting stamp: time=" + time + ", name=" + name);

        if (time == null || name == null) {
            System.out.println("Time or name is null, skipping stamp.");
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        name = Parsers.parseUniStatic(name);
        time = Parsers.parseUniStatic(time);
        lore.add("§7");
        lore.add("§f"+ SIGNED_BY.getIcon() +" §e" + name);
        if (quote.equals("null") || quote == null) {
            lore.add("§f" + TIME.getIcon() + " §e" + time);
        } else {
            lore.add("§e"+Parsers.parseUniStatic(quote));
        }

        meta.setLore(lore);
        item.setItemMeta(meta); // Ensure changes are applied
    }

    public void createStamp(Player player, ItemStack item, @Nullable String message) {
        if (item == null || player == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        String parsedTime = parseTime();
        String playerName = player.getName();
        String quote = "null";
        if (message != null) {
            quote = "\""+ message+ "\"";
        }
        if (nickManager.getNickname(player) != null) {
            playerName = nickManager.getNickname(player).replace("_", " ");
        }

        data.set(quoteKey, PersistentDataType.STRING, quote);
        data.set(timeKey, PersistentDataType.STRING, parsedTime);
        data.set(nameKey, PersistentDataType.STRING, playerName);



        item.setItemMeta(meta); // Ensure changes are applied
        setStamp(item); // Ensure stamping happens after setting data
    }

    private String parseTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return now.format(formatter);
    }

    public NamespacedKey getIdKey() {
        return idKey;
    }
    public NamespacedKey getUndroppableKey() {
        return undroppableKey;
    }
    public NamespacedKey getUnusableKey() {
        return unusableKey;
    }
    public NamespacedKey getLeftActionKey() {
        return leftActionKey;
    }
    public NamespacedKey getRightActionKey() {
        return rightActionKey;
    }
    public NamespacedKey getPlaceableKey() {
        return placeableKey;
    }
    public NamespacedKey getProfileBgKey() {
        return profileBgKey;
    }
    public NamespacedKey getProfilePicKey() {
        return profilePicKey;
    }
    public CitemDAO getCitemDAO() {
        return hub.getCitemDAO();
    }
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}