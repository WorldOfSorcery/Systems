package me.hektortm.woSSystems.listeners;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemCustomModelData;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InterListener implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager interManager = plugin.getInteractionManager();
    private final CitemManager citemManager = plugin.getCitemManager();
    private final DAOHub hub;
    private final Map<Location, Long> blockCooldowns = new HashMap<>();
    private final Map<String, Long> npcCooldowns = new HashMap<>();
    private final NamespacedKey unusableKey;
    private NamespacedKey placeableKey = citemManager.getPlaceableKey();

    public InterListener(DAOHub hub) {
        this.hub = hub;
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
        Location locClicked = e.getClickedBlock().getLocation();

        if(hub.getCitemDAO().isItemDisplay(locClicked)) {
            if (p.isSneaking()) {
                if (e.getClickedBlock().getType() == Material.BARRIER) {
                    if (hub.getCitemDAO().isItemDisplayOwner(locClicked, p.getUniqueId()) || p.getGameMode() == GameMode.CREATIVE) {
                        ItemStack giveCitem = hub.getCitemDAO().getCitem(hub.getCitemDAO().getItemDisplayID(locClicked));

                        if (p.getGameMode() == GameMode.CREATIVE && !hub.getCitemDAO().isItemDisplayOwner(locClicked, p.getUniqueId())) {
                            hub.getCitemDAO().removeItemDisplay(hub.getCitemDAO().getUUID(locClicked), locClicked);
                        } else if (hub.getCitemDAO().isItemDisplayOwner(locClicked, p.getUniqueId())) {
                            hub.getCitemDAO().removeItemDisplay(p.getUniqueId(), locClicked);
                        } else {
                            p.sendMessage("You are not the owner of this display.");
                            return;
                        }

                        removeEntityAtLocation(locClicked);
                        Block clickedBlock = e.getClickedBlock();
                        clickedBlock.setType(null);
                        p.getInventory().addItem(giveCitem);
                    }
                }
            }
        }


        if (citemManager.isCitem(item)) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            String id = data.get(citemManager.getIdKey(), PersistentDataType.STRING);

            if (data.has(placeableKey, PersistentDataType.BOOLEAN)) {
                if (data.get(placeableKey, PersistentDataType.BOOLEAN).equals(Boolean.TRUE)) {
                    if (action.isRightClick()) {

                        if (e.getClickedBlock() != null) {
                            Block targetBlock = e.getClickedBlock();
                            Location spawnLocation = targetBlock.getLocation().add(0, 1, 0);


                            // Spawn an ItemDisplay entity
                            ItemDisplay itemDisplay = (ItemDisplay) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
                            itemDisplay.setItemStack(item); // Set the item to display
                            itemDisplay.setTransformation(new Transformation(new Vector3f(1), new AxisAngle4f(), new Vector3f(), new AxisAngle4f())); // Set the transformation (scale, rotation, etc.)

                            // Spawn a Barrier block
                            targetBlock.getWorld().getBlockAt(spawnLocation).setType(Material.BARRIER);

                            // Optionally, consume the item in hand (e.g., remove one stick)
                            p.getInventory().remove(hub.getCitemDAO().getCitem(id));
                            hub.getCitemDAO().createItemDisplay(id, p.getUniqueId(), spawnLocation);
                        }
                    }
                }

            }

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



            if (meta != null) {

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

    public void removeEntityAtLocation(Location location) {
        // Get all entities in the world
        for (Entity entity : location.getWorld().getEntities()) {
            // Check if the entity is an ItemDisplay and is at the specified location
            if (entity instanceof ItemDisplay && entity.getLocation().equals(location)) {
                entity.remove(); // Remove the entity
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

    private void spawnItemDisplay(Location loc) {
        
    }

}
