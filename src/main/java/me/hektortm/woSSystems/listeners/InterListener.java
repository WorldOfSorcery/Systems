package me.hektortm.woSSystems.listeners;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemCustomModelData;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.maximde.hologramlib.hologram.ItemHologram;
import com.maximde.hologramlib.hologram.RenderMode;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager_new;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
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
import java.util.*;
import java.util.logging.Level;

public class InterListener implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager interManager = plugin.getInteractionManager();
    private final InteractionManager_new interactionManager = plugin.getInteractionManager_new();
    private final CitemManager citemManager = plugin.getCitemManager();
    private final DAOHub hub;
    private final Map<Location, Long> blockCooldowns = new HashMap<>();
    private final Map<String, Long> npcCooldowns = new HashMap<>();
    private final Map<UUID, Long> displayCooldowns = new HashMap<>();
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
        Location locClicked = null;
        if (e.getClickedBlock() != null) {
            locClicked = Objects.requireNonNull(e.getClickedBlock()).getLocation(e.getClickedBlock().getLocation());
        }
        if (locClicked != null) {
            if ((e.getClickedBlock().getType() == Material.BARRIER || e.getClickedBlock().getType() == Material.DEAD_TUBE_CORAL_FAN) && p.getInventory().getItemInMainHand().isEmpty()) {
                if (action.isRightClick() && !p.isSneaking()) {
                    if (hub.getCitemDAO().isItemDisplay(locClicked)) {
                        if (hub.getCitemDAO().isItemDisplayOwner(locClicked, p.getUniqueId()) || p.getGameMode() == GameMode.CREATIVE) {
                            long currentTime = System.currentTimeMillis();
                            long cooldownTime = 250; // 250 ms cooldown

                            if (displayCooldowns.containsKey(p.getUniqueId())) {
                                long lastInteractionTime = displayCooldowns.get(p.getUniqueId());
                                long elapsedTime = currentTime - lastInteractionTime;

                                if (elapsedTime < cooldownTime) {
                                    return; // Skip processing if block is on cooldown
                                }
                            }
                            displayCooldowns.put(p.getUniqueId(), currentTime);

                            rotateItemDisplay(locClicked);
                            p.playSound(locClicked, Sound.ITEM_SPYGLASS_USE, 1L, 1L);
                        }
                    }
                }
                if ((action.isRightClick() && p.isSneaking()) && p.getInventory().getItemInMainHand().isEmpty()) {
                    if (hub.getCitemDAO().isItemDisplay(locClicked)) {
                        if (hub.getCitemDAO().isItemDisplayOwner(locClicked, p.getUniqueId()) || p.getGameMode() == GameMode.CREATIVE) {

                            long currentTime = System.currentTimeMillis();
                            long cooldownTime = 250; // 250 ms cooldown

                            if (displayCooldowns.containsKey(p.getUniqueId())) {
                                long lastInteractionTime = displayCooldowns.get(p.getUniqueId());
                                long elapsedTime = currentTime - lastInteractionTime;

                                if (elapsedTime < cooldownTime) {
                                    return; // Skip processing if block is on cooldown
                                }
                            }

                            // Update cooldown
                            displayCooldowns.put(p.getUniqueId(), currentTime);

                            ItemStack giveCitem = hub.getCitemDAO().getCitem(hub.getCitemDAO().getItemDisplayID(locClicked));

                            removeEntityAtLocation(hub.getCitemDAO().getDisplayLocation(locClicked));
                            if (p.getGameMode() == GameMode.CREATIVE) {
                                hub.getCitemDAO().removeItemDisplay(hub.getCitemDAO().getUUID(locClicked), locClicked);
                            } else if (hub.getCitemDAO().isItemDisplayOwner(locClicked, p.getUniqueId())) {
                                hub.getCitemDAO().removeItemDisplay(p.getUniqueId(), locClicked);
                            } else {
                                p.sendMessage("You are not the owner of this display.");
                                return;
                            }


                            Block clickedBlock = e.getClickedBlock();
                            p.playSound(locClicked, Sound.BLOCK_CHISELED_BOOKSHELF_PICKUP, 1L, 1L);
                            spawnPickupParticle(locClicked);
                            clickedBlock.setType(Material.AIR);
                            p.getInventory().addItem(giveCitem);
                        } else {
                            p.sendMessage("You are not the owner of this display.");
                            return;
                        }
                    }
                }
            }
        }


        if (citemManager.isCitem(item)) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            String id = data.get(citemManager.getIdKey(), PersistentDataType.STRING);
            int dataKey = data.get(placeableKey, PersistentDataType.INTEGER);

            if (dataKey > 0) {

                if (action.isRightClick()) {

                    if (e.getClickedBlock() != null) {
                        Block targetBlock = e.getClickedBlock();
                        Block above = targetBlock.getRelative(BlockFace.UP);
                        if(above.getType() != Material.AIR) {
                            return;
                        }
                        Location spawnLocation = targetBlock.getLocation().add(0, 1, 0);

                        if(hub.getCitemDAO().isItemDisplay(spawnLocation)) {
                            return;
                        }

                        long currentTime = System.currentTimeMillis();
                        long cooldownTime = 250; // 250 ms cooldown

                        if (displayCooldowns.containsKey(p.getUniqueId())) {
                            long lastInteractionTime = displayCooldowns.get(p.getUniqueId());
                            long elapsedTime = currentTime - lastInteractionTime;

                            if (elapsedTime < cooldownTime) {
                                return; // Skip processing if block is on cooldown
                            }
                        }

                        // Update cooldown
                        displayCooldowns.put(p.getUniqueId(), currentTime);

                        // Spawn an ItemDisplay entity
                        spawnItemDisplay(spawnLocation, id, p.getUniqueId()); // Set the transformation (scale, rotation, etc.)
                        // Spawn a Barrier block
                        if (data.get(placeableKey, PersistentDataType.INTEGER) == 1) {
                            Material setMat = Material.DEAD_TUBE_CORAL_FAN;

                            Block tBlock = targetBlock.getWorld().getBlockAt(spawnLocation);
                            tBlock.setType(setMat);
                            if (tBlock.getBlockData() instanceof Waterlogged wL) {
                                wL.setWaterlogged(false);
                                tBlock.setBlockData(wL);
                            }
                        } else if (data.get(placeableKey, PersistentDataType.INTEGER) == 2) {
                            targetBlock.getWorld().getBlockAt(spawnLocation).setType(Material.BARRIER);
                        }
                        p.playSound(locClicked, Sound.BLOCK_CANDLE_PLACE, 1L, 1L);


                        // Optionally, consume the item in hand (e.g., remove one stick)
                        if (p.getGameMode() != GameMode.CREATIVE) {
                            ItemStack itemToRemove = hub.getCitemDAO().getCitem(id);
                            itemToRemove.setAmount(1);
                            p.getInventory().removeItem(itemToRemove);
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

                if (data.get(unusableKey, PersistentDataType.BOOLEAN) == true) {
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
            if (hub.getInteractionDAO().getInterOnBlock(blockLocation) !=null) {

                switch (e.getAction()) {
                    case RIGHT_CLICK_BLOCK:
                    case LEFT_CLICK_BLOCK:
                        interactionManager.triggerInteraction(hub.getInteractionDAO().getInterOnBlock(blockLocation), p);
                        break;
                }
            }

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (hub.getCitemDAO().isItemDisplay(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (hub.getInteractionDAO().getAllBlockLocations().contains(block.getLocation())) {
            event.setCancelled(true);
            return;
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

    private void removeEntityAtLocationSafely(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.getNearbyEntities(location, 0.5, 0.5, 0.5).stream()
                .filter(entity -> entity instanceof ItemDisplay)
                .forEach(Entity::remove);
    }


    private boolean isSameLocation(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getWorld().equals(loc2.getWorld()) &&
                Math.abs(loc1.getX() - loc2.getX()) < 0.5 &&  // Tolerance for x-coordinate
                Math.abs(loc1.getY() - loc2.getY()) < 0.5 &&  // Tolerance for y-coordinate
                Math.abs(loc1.getZ() - loc2.getZ()) < 0.5;    // Tolerance for z-coordinate
    }

    private void rotateItemDisplay(Location blockLocation) {
        Location displayLocation = hub.getCitemDAO().getDisplayLocation(blockLocation);
        if (displayLocation == null) return; // Prevent null errors

        // 🔹 Remove old display using a safer method
        removeEntityAtLocationSafely(displayLocation);

        // 🔹 Ensure yaw stays between 0-360 degrees
        float oldYaw = displayLocation.getYaw();
        float newYaw = (oldYaw + 45) % 360; // Keeps yaw in range

        // 🔹 Create a new rotated location
        Location newDisplayLocation = displayLocation.clone();
        newDisplayLocation.setYaw(newYaw);

        // 🔹 Get item & spawn the new display entity
        ItemStack item = hub.getCitemDAO().getCitem(hub.getCitemDAO().getItemDisplayID(blockLocation));
        ItemDisplay display = blockLocation.getWorld().spawn(newDisplayLocation, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
        });

        // 🔹 Update database with the new location
        hub.getCitemDAO().changeDisplay(displayLocation, newDisplayLocation);
    }


    private void spawnItemDisplay(Location blockLocation, String id, UUID uuid) {
        ItemStack item = hub.getCitemDAO().getCitem(id);
        if (blockLocation == null || item == null) {
            System.out.println("Spawn location or item is null!");
            return;
        }

        World world = blockLocation.getWorld();
        if (world == null) {
            System.out.println("World is null!");
            return;
        }
        Location displayLocation = blockLocation.clone().add(0.5, 0.51, 0.5);
        // Spawn the ItemDisplay entity
        ItemDisplay itemDisplay = blockLocation.getWorld().spawn(displayLocation, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
        });

        itemDisplay.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(),
                new Vector3f(1,1,1),
                new AxisAngle4f()
        ));

        // Ensure item display spawned correctly
        if (itemDisplay == null) {
            System.out.println("Failed to spawn ItemDisplay!");
            return;
        }

        hub.getCitemDAO().createItemDisplay(id, uuid, blockLocation, displayLocation);

    }

    private void spawnPickupParticle(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // 🔹 Number of particles & effect radius
        int particleCount = 30;
        double radius = 0.7;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double xOffset = Math.cos(angle) * radius + 0.5;
            double zOffset = Math.sin(angle) * radius + 0.5;

            Location particleLoc = location.clone().add(xOffset, i * 0.04, zOffset);

            // 🔥 Use Redstone particle with color
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f); // Gold effect

            world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
        }

        // 🌟 Extra sparkle effect
        world.spawnParticle(Particle.CRIT, location.clone().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.1);
    }


}
