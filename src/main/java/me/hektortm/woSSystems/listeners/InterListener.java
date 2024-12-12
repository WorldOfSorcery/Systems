package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.List;

public class InterListener implements Listener {

    private final InteractionManager interManager;
    private final CitemManager citemManager;

    private final NamespacedKey unusableKey;

    public InterListener(InteractionManager manager, CitemManager citemManager) {
        this.interManager = manager;
        this.citemManager = citemManager;
        unusableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "unusable");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        Action action = e.getAction();
        Player p = e.getPlayer();

        if (citemManager.isCitem(item)) {
            switch (action) {
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    citemManager.rightClickAction(p);
                    break;
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                    citemManager.leftClickAction(p);
                    break;
            }


            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer data = meta.getPersistentDataContainer();
                if (data.has(unusableKey, PersistentDataType.BOOLEAN)) {
                    e.setCancelled(true); // Prevent the item from being dropped
                    e.getPlayer().sendMessage("§bDEBUG: §cYou cannot Use this item.");
                }
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {

            Block block = e.getClickedBlock();
            if (e.isCancelled()) {
                return;  // If event is already canceled, do nothing
            }

            if (block == null) return;



            for (File file : interManager.interactionFolder.listFiles()) {

                if (file.isFile() && file.getName().endsWith(".json")) {
                    String id = file.getName().replace(".json", "");
                    InteractionData inter = interManager.interactionMap.get(id);
                    if (inter == null) {
                        Bukkit.getLogger().info("Inter "+id+" is null.");
                    }
                    List<Location> locs = inter.getLocations();
                    for (Location loc : locs) {
                        if (isSameLocation(block.getLocation(), loc)) {
                            e.setCancelled(true);
                            switch (action) {
                                case RIGHT_CLICK_BLOCK:
                                    interManager.triggerInteraction(p, id);
                                    break;
                                case LEFT_CLICK_BLOCK:
                                    interManager.triggerInteraction(p, id);
                                    break;
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        for (File file : interManager.interactionFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                String id = file.getName().replace(".json", "");
                InteractionData inter = interManager.interactionMap.get(id);
                System.out.println("Looking for InteractionData with id: " + id);
                if (inter == null) {
                    Bukkit.getLogger().info("Inter "+id+" is null.");
                }
                List<Location> locs = inter.getLocations();
                for (Location loc : locs) {
                    if (isSameLocation(block.getLocation(), loc)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("You cannot break this block!");
                        return;
                    }
                }
            }
        }
    }
    private boolean isSameLocation(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().equals(loc2.getWorld()) &&
                Math.abs(loc1.getX() - loc2.getX()) < 0.5 &&  // Tolerance for x-coordinate
                Math.abs(loc1.getY() - loc2.getY()) < 0.5 &&  // Tolerance for y-coordinate
                Math.abs(loc1.getZ() - loc2.getZ()) < 0.5;    // Tolerance for z-coordinate
    }

}
