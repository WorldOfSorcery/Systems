package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class HologramHandler implements Listener {

    private final WoSSystems plugin;
    private final ConditionHandler conditionHandler;
    private final DAOHub hub;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger(-100000);
    private final Map<String, Map<UUID, Integer>> hologramMap = new ConcurrentHashMap<>();
    private final Set<Integer> usedEntityIds = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<String, List<Integer>>> playerHologramEntities = new HashMap<>();

    public HologramHandler(DAOHub hub) {
        this.plugin = WoSSystems.getPlugin(WoSSystems.class);
        this.conditionHandler = plugin.getConditionHandler();
        this.hub = hub;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void handleHolograms(Player player, Interaction inter, Location location, boolean isNPC) {
        List<InteractionHologram> hologramList = inter.getHolograms();
        if (hologramList == null || hologramList.isEmpty()) return;

        String interactionId = inter.getInteractionId();
        String hologramKey = isNPC ? interactionId + ":npc" : interactionId + ":loc_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        Location spawnLoc = calculateHologramLocation(location, isNPC);

        for (InteractionHologram hologram : hologramList) {
            String matchType = hologram.getMatchType();

            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.HOLOGRAM,
                    inter.getInteractionId() + ":" + hologram.getHologramID()
            );


            boolean shouldRun = conditionList.isEmpty();
            if (!shouldRun) {
                if ("one".equalsIgnoreCase(matchType)) {
                    shouldRun = conditionList.stream().anyMatch(cond -> conditionHandler.evaluate(player, cond));
                } else {
                    shouldRun = conditionHandler.checkConditions(player, conditionList);
                }
            }

            if (!shouldRun) continue;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {

                spawnPacketHologram(player, spawnLoc, hologram.getHologram(), hologramKey);
            }, 20L);


            if (!"continue".equals(hologram.getBehaviour())) break;
        }
    }

    private Location calculateHologramLocation(Location original, boolean isNPC) {
        Location loc = original.clone();
        return isNPC ? loc.add(0, 2, 0) : loc.add(0.5, 1.2, 0.5);
    }

    private void spawnPacketHologram(Player player, Location location, List<String> hologramLines, String hologramKey) {
        List<Integer> entityIds = new ArrayList<>();
        double yOffset = 0.25 * (hologramLines.size() - 1);

        for (String line : hologramLines) {
            int entityId = ENTITY_ID_COUNTER.decrementAndGet(); // Use proper ID
            UUID entityUUID = UUID.randomUUID();

            Location lineLoc = location.clone().add(0, yOffset, 0);
            yOffset -= 0.25;

            try {

                entityIds.add(entityId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Store IDs for cleanup
        playerHologramEntities
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(hologramKey, entityIds);
    }





    private int generateUniqueEntityId() {
        int id;
        do {
            id = ThreadLocalRandom.current().nextInt(100000, Integer.MAX_VALUE - 100000);
        } while (usedEntityIds.contains(id));
        return id;
    }



}
