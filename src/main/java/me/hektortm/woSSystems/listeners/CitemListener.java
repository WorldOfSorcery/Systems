package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemDisplays;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.Keys;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

import static me.hektortm.woSSystems.listeners.InterListener.buildKey;

public class CitemListener implements Listener {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final CitemManager citemManager = plugin.getCitemManager();
    private final InteractionManager interactionManager = plugin.getInteractionManager();
    private final CitemDisplays citemDisplays = plugin.getCitemDisplays();
    private final DAOHub hub;
    private final Map<UUID, Long> displayCooldowns = new HashMap<>();


    public CitemListener(DAOHub hub) {
        this.hub = hub;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();
        ItemStack item = e.getItem();

        Location locClicked = getClickedLocation(e);

        if (locClicked != null &&
                isEmptyHandInteraction(e, p) &&
                isDisplayBlock(e.getClickedBlock())) {

            handleDisplayInteraction(e, p, action, locClicked);
            return;
        }

        if (citemManager.isCitem(item)) {
            handleCitemPlacement(e, p, item, action);
            handleCitemActions(p, action, item);
        }
    }

    private Location getClickedLocation(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return null;
        return e.getClickedBlock().getLocation();
    }

    private boolean isEmptyHandInteraction(PlayerInteractEvent e, Player p) {
        return p.getInventory().getItemInMainHand().getType().isAir();
    }

    private boolean isDisplayBlock(Block block) {
        if (block == null) return false;
        return block.getType() == Material.BARRIER || block.getType() == Material.DEAD_TUBE_CORAL_FAN;
    }

    private void handleDisplayInteraction(PlayerInteractEvent e, Player p, Action action, Location loc) {
        if (!hub.getCitemDAO().isItemDisplay(loc)) return;

        if (action.isRightClick() && !p.isSneaking()) handleRightClickDisplay(e, p, loc);
        if (action.isRightClick() && p.isSneaking()) handleSneakRightClickDisplay(e, p, loc);
    }

    private void handleRightClickDisplay(PlayerInteractEvent e, Player p, Location loc) {
        if (canEdit(p, loc) && !isOnCooldown(p)) {
            updateCooldown(p);

            citemDisplays.rotateItemDisplay(loc);
            p.playSound(loc, Sound.ITEM_SPYGLASS_USE, 1L, 1L);
        }

        triggerInteractionForDisplay(e,p,loc);
    }

    private void triggerInteractionForDisplay(PlayerInteractEvent e, Player p, Location loc) {
        if (!isOnCooldown(p)) {
            updateCooldown(p);

            String id = hub.getCitemDAO().getItemDisplayID(loc);
            ItemStack citem = hub.getCitemDAO().getCitem(id);

            PersistentDataContainer data = citem.getItemMeta().getPersistentDataContainer();
            String interId = data.get(Keys.PLACED_ACTION.get(), PersistentDataType.STRING);

            if (interId == null) {
                return;
            }

            plugin.getInteractionManager().triggerInteraction(interId, p, buildKey(loc));
        }
    }

    private void handleSneakRightClickDisplay(PlayerInteractEvent e, Player p, Location loc) {

        if (!hub.getCitemDAO().isItemDisplay(loc)) return;
        if (hub.getCitemDAO().isCreativePlaced(loc) && p.getGameMode() != GameMode.CREATIVE) return;

        if (!hub.getCitemDAO().isItemDisplayOwner(loc, p.getUniqueId()) &&
                p.getGameMode() != GameMode.CREATIVE) {

            p.sendMessage("You are not the owner of this display.");
            return;
        }

        if (isOnCooldown(p)) return;
        updateCooldown(p);

        String id = hub.getCitemDAO().getItemDisplayID(loc);
        ItemStack give = hub.getCitemDAO().getCitem(id);
        give.setAmount(1);

        citemDisplays.removeEntityAtLocation(hub.getCitemDAO().getDisplayLocation(loc));

        if (p.getGameMode() == GameMode.CREATIVE) {
            hub.getCitemDAO().removeItemDisplay(hub.getCitemDAO().getUUID(loc), loc);
        } else {
            hub.getCitemDAO().removeItemDisplay(p.getUniqueId(), loc);
        }

        e.getClickedBlock().setType(Material.AIR);
        citemDisplays.spawnPickupParticle(loc);
        p.playSound(loc, Sound.BLOCK_CHISELED_BOOKSHELF_PICKUP, 1, 1);
        p.getInventory().addItem(give);
    }

    private void handleCitemPlacement(PlayerInteractEvent e, Player p, ItemStack item, Action action) {

        if (!action.isRightClick()) return;

        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        int placeable = data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER) != null
                ? data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER)
                : 0;

        if (placeable <= 0) return;
        if (e.getClickedBlock() == null) return;

        Block target = e.getClickedBlock();
        Block above = target.getRelative(BlockFace.UP);

        if (!above.getType().isAir()) return;

        Location spawnLoc = target.getLocation().add(0, 1, 0);

        if (hub.getCitemDAO().isItemDisplay(spawnLoc)) return;
        if (isOnCooldown(p)) return;
        updateCooldown(p);

        placeCitemDisplay(p, spawnLoc, data, item);

        if (p.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    private void placeCitemDisplay(Player p, Location loc, PersistentDataContainer data, ItemStack item) {

        String id = data.get(Keys.ID.get(), PersistentDataType.STRING);

        citemDisplays.spawnItemDisplay(loc, id, p);

        int type = data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER);

        Material mat = type == 1 ? Material.DEAD_TUBE_CORAL_FAN :
                type == 2 ? Material.BARRIER : Material.AIR;

        Block block = loc.getBlock();
        block.setType(mat);

        if (block.getBlockData() instanceof Waterlogged wL) {
            wL.setWaterlogged(false);
            block.setBlockData(wL);
        }

        p.playSound(loc, Sound.BLOCK_CANDLE_PLACE, 1, 1);
    }

    private void handleCitemActions(Player p, Action action, ItemStack item) {

        switch (action) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> rightClickAction(p);
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> leftClickAction(p);
        }

        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        if (Boolean.TRUE.equals(data.get(Keys.UNUSABLE.get(), PersistentDataType.BOOLEAN))) {
            p.getInventory().getItemInMainHand().setAmount(
                    p.getInventory().getItemInMainHand().getAmount()); // Prevent drop
        }
    }

    private boolean isOnCooldown(Player p) {
        long current = System.currentTimeMillis();
        long last = displayCooldowns.getOrDefault(p.getUniqueId(), 0L);
        return current - last < 250;
    }

    private void updateCooldown(Player p) {
        displayCooldowns.put(p.getUniqueId(), System.currentTimeMillis());
    }

    private boolean canEdit(Player player, Location loc) {
        boolean isOwner = hub.getCitemDAO().isItemDisplayOwner(loc, player.getUniqueId());
        boolean isCreativePlaced = hub.getCitemDAO().isCreativePlaced(loc);
        boolean isCreative = player.getGameMode() == GameMode.CREATIVE;

        return (isOwner && !isCreativePlaced) || (isCreative && isCreativePlaced);

    }

    public void leftClickAction(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String actionId = data.get(Keys.LEFT_ACTION.get(), PersistentDataType.STRING);
        if (actionId != null) {
            interactionManager.triggerInteraction(actionId, p, null);
        }
    }

    public void rightClickAction(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String actionId = data.get(Keys.RIGHT_ACTION.get(), PersistentDataType.STRING);
        if (actionId != null) {
            interactionManager.triggerInteraction(actionId, p, null);
        }
    }


}
