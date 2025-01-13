package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InterListener implements Listener {

    private final InteractionManager interManager;
    private final CitemManager citemManager;
    private final Map<Location, Long> blockCooldowns = new HashMap<>();
    private final Map<String, Long> npcCooldowns = new HashMap<>();
    private final NamespacedKey unusableKey;

    public InterListener(InteractionManager manager, CitemManager citemManager) {
        this.interManager = manager;
        this.citemManager = citemManager;
        unusableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "unusable");
    }

    @EventHandler
    public void onNPCInteract(NPCRightClickEvent event) {
        Player p = event.getClicker();
        int clickedNPCid = event.getNPC().getId();
        String npcIdString = String.valueOf(clickedNPCid);
        Bukkit.getLogger().info("Interacted with NPC: " + clickedNPCid);

        for (File file : interManager.interactionFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                String id = file.getName().replace(".json", "");
                InteractionData inter = interManager.interactionMap.get(id);
                if (inter == null) {
                    Bukkit.getLogger().info("Interaction " + id + " is null.");
                    continue; // Skip invalid interaction files
                }
                List<String> ids = inter.getNpcIDs();

                for (String npcid : ids) {
                    if (Objects.equals(npcid, npcIdString)) {
                        Bukkit.getLogger().info("NPC Interaction triggered for ID: " + id);

                        // Trigger interaction based on action
                        interManager.triggerInteraction(p, id);
                        return; // Exit after handling interaction
                    }
                }
            }
        }
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
                }
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if (e.isCancelled()) {
                return; // Event already canceled
            }
            if (block == null) return;

            Location blockLocation = block.getLocation();
            long currentTime = System.currentTimeMillis();
            long cooldownTime = 250; // 250 ms cooldown

            if (blockCooldowns.containsKey(blockLocation)) {
                long lastInteractionTime = blockCooldowns.get(blockLocation);
                long elapsedTime = currentTime - lastInteractionTime;

                if (elapsedTime < cooldownTime) {
                    return; // Skip processing if block is on cooldown
                }
            }

            // Update cooldown
            blockCooldowns.put(blockLocation, currentTime);

            // Process interactions
            for (File file : interManager.interactionFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String id = file.getName().replace(".json", "");
                    InteractionData inter = interManager.interactionMap.get(id);

                    if (inter == null) {
                        Bukkit.getLogger().info("Interaction " + id + " is null.");
                        continue; // Skip invalid interaction files
                    }

                    List<Location> locs = inter.getLocations();
                    for (Location loc : locs) {
                        if (isSameLocation(blockLocation, loc)) {
                            e.setCancelled(true); // Cancel default behavior

                            Bukkit.getLogger().info("Interaction triggered for ID: " + id);

                            // Trigger interaction based on action
                            switch (e.getAction()) {
                                case RIGHT_CLICK_BLOCK:
                                case LEFT_CLICK_BLOCK:
                                    interManager.triggerInteraction(p, id);
                                    break;
                            }
                            return; // Exit after handling interaction
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
