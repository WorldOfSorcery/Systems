package me.hektortm.woSSystems.systems.interactions;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.model.InteractionKey;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class InterListener implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager interactionManager = plugin.getInteractionManager();
    private final CitemManager citemManager = plugin.getCitemManager();
    private final DAOHub hub;
    private final Map<Location, Long> blockCooldowns = new HashMap<>();
    private final Map<String, Long> npcCooldowns = new HashMap<>();
    private final Map<UUID, Long> displayCooldowns = new HashMap<>();

    public InterListener(DAOHub hub) {
        this.hub = hub;
    }
    

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

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
            if (hub.getInteractionDAO().getBound(blockLocation) != null) {
                e.setCancelled(true);
                InteractionKey key = buildKey(blockLocation);

                switch (e.getAction()) {
                    case RIGHT_CLICK_BLOCK:
                    case LEFT_CLICK_BLOCK:
                        interactionManager.triggerInteraction(hub.getInteractionDAO().getBound(blockLocation), p, key);
                        break;
                }
            }

        }
    }

    @EventHandler
    public void NPCClick(NPCRightClickEvent e) {
        int npcid = e.getNPC().getId();
        String id = hub.getInteractionDAO().getNpcBound(npcid);
        InteractionKey key = new InteractionKey("npc:"+npcid);
        interactionManager.triggerInteraction(id, e.getClicker(), key);
    }

    public static InteractionKey buildKey (Location loc) {
        String world = loc.getWorld().getName();
        String x = String.valueOf(loc.getX());
        String y = String.valueOf(loc.getY());
        String z = String.valueOf(loc.getZ());

        return new InteractionKey("block:" + world + ":" + x + ":" + y + ":" + z);
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
}
