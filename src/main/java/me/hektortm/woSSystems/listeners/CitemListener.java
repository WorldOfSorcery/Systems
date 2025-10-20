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

public class CitemListener implements Listener {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final CitemManager citemManager = plugin.getCitemManager();
    private final InteractionManager interactionManager = plugin.getInteractionManager();
    private final CitemDisplays citemDisplays = plugin.getCitemDisplays();
    private final DAOHub hub;
    private final Map<Location, Long> blockCooldowns = new HashMap<>();
    private final Map<String, Long> npcCooldowns = new HashMap<>();
    private final Map<UUID, Long> displayCooldowns = new HashMap<>();

    public CitemListener(DAOHub hub) {
        this.hub = hub;
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
                        if (hub.getCitemDAO().isCreativePlaced(locClicked) && p.getGameMode() != GameMode.CREATIVE) return;
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

                            citemDisplays.rotateItemDisplay(locClicked);
                            p.playSound(locClicked, Sound.ITEM_SPYGLASS_USE, 1L, 1L);
                        }
                    }
                }
                if ((action.isRightClick() && p.isSneaking()) && p.getInventory().getItemInMainHand().isEmpty()) {
                    if (hub.getCitemDAO().isItemDisplay(locClicked)) {
                        if (hub.getCitemDAO().isCreativePlaced(locClicked) && p.getGameMode() != GameMode.CREATIVE) return;
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

                            citemDisplays.removeEntityAtLocation(hub.getCitemDAO().getDisplayLocation(locClicked));
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
                            citemDisplays.spawnPickupParticle(locClicked);
                            clickedBlock.setType(Material.AIR);
                            giveCitem.setAmount(1);
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
            String id = data.get(Keys.ID.get(), PersistentDataType.STRING);
            int dataKey = data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER) != null ? data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER) : 0;


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
                        citemDisplays.spawnItemDisplay(spawnLocation, id, p); // Set the transformation (scale, rotation, etc.)
                        // Spawn a Barrier block
                        if (data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER) == 1) {
                            Material setMat = Material.DEAD_TUBE_CORAL_FAN;

                            Block tBlock = targetBlock.getWorld().getBlockAt(spawnLocation);
                            tBlock.setType(setMat);
                            if (tBlock.getBlockData() instanceof Waterlogged wL) {
                                wL.setWaterlogged(false);
                                tBlock.setBlockData(wL);
                            }
                        } else if (data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER) == 2) {
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
                    rightClickAction(p);
                    break;
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                    leftClickAction(p);
                    break;
            }

            if (Boolean.TRUE.equals(data.get(Keys.UNUSABLE.get(), PersistentDataType.BOOLEAN))) {
                e.setCancelled(true); // Prevent the item from being dropped
            }
        }
    }

    public void leftClickAction(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String actionId = data.get(Keys.LEFT_ACTION.get(), PersistentDataType.STRING);
        if (actionId != null) {
            interactionManager.triggerInteraction(actionId, p);
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
            interactionManager.triggerInteraction(actionId, p);
        }
    }


}
