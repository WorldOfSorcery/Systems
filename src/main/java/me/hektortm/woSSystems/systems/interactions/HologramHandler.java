package me.hektortm.woSSystems.systems.interactions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
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
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
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

        cleanupPlayerHolograms(player, hologramKey);

        for (InteractionHologram hologram : hologramList) {
            String matchType = hologram.getMatchType();

            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.HOLOGRAM,
                    inter.getInteractionId() + ":" + hologram.getHologramID()
            );

            cleanupPlayerHolograms(player, hologramKey);

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
                PacketContainer spawnPacket = createSpawnPacket(entityId, entityUUID, lineLoc);
                PacketContainer metadataPacket = createMetadataPacket(entityId, line);

                protocolManager.sendServerPacket(player, spawnPacket);
                protocolManager.sendServerPacket(player, metadataPacket);

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

    private PacketContainer createSpawnPacket(int entityId, UUID entityUUID, Location location) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        // For 1.21.1 SPAWN_ENTITY packet structure
        packet.getIntegers()
                .write(0, entityId)            // Entity ID
                .write(1, 83);                 // Entity Type (TEXT_DISPLAY)

        packet.getUUIDs().write(0, entityUUID);
        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        // Set yaw/pitch as radians
        packet.getBytes()
                .write(0, (byte) 0)   // Yaw
                .write(1, (byte) 0);   // Pitch

        // Set velocity (important!)
        packet.getShorts()
                .write(0, (short) 0)  // Velocity X
                .write(1, (short) 0)  // Velocity Y
                .write(2, (short) 0); // Velocity Z

        return packet;
    }

    private PacketContainer createMetadataPacket(int entityId, String text) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        List<WrappedDataValue> dataValues = new ArrayList<>();

        // Text content (1.21+ uses data value 25)
        dataValues.add(new WrappedDataValue(
                25,
                WrappedDataWatcher.Registry.getChatComponentSerializer(true),
                WrappedChatComponent.fromText(text).getHandle()
        ));

        // Billboard type (center)
        dataValues.add(new WrappedDataValue(
                27,
                WrappedDataWatcher.Registry.get(Integer.class),
                3
        ));

        packet.getIntegers().write(0, entityId);
        packet.getDataValueCollectionModifier().write(0, dataValues);

        return packet;
    }


    private void cleanupPlayerHolograms(Player player, String hologramKey) {
        Map<String, List<Integer>> playerHolograms = playerHologramEntities.get(player.getUniqueId());
        if (playerHolograms == null) return;

        List<Integer> entities = playerHolograms.remove(hologramKey);
        if (entities == null || entities.isEmpty()) return;

        try {
            PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntLists().write(0, entities);
            protocolManager.sendServerPacket(player, destroyPacket);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error cleaning up holograms: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private int generateUniqueEntityId() {
        int id;
        do {
            id = ThreadLocalRandom.current().nextInt(100000, Integer.MAX_VALUE - 100000);
        } while (usedEntityIds.contains(id));
        return id;
    }


    private void sendDestroyPacket(Player player, int entityId) {
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, Collections.singletonList(entityId));

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to destroy hologram", e);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Map<String, List<Integer>> holograms = playerHologramEntities.remove(playerId);
        if (holograms != null) {
            holograms.values().forEach(entities -> {
                PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
                destroyPacket.getIntLists().write(0, entities);
                try {
                    protocolManager.sendServerPacket(event.getPlayer(), destroyPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == plugin) {
            forceCleanupAllHolograms();
        }
    }

    public void cleanupPlayer(Player player) {
        hologramMap.values().forEach(map -> {
            Integer entityId = map.remove(player.getUniqueId());
            if (entityId != null) {
                sendDestroyPacket(player, entityId);
                usedEntityIds.remove(entityId);
            }
        });
    }

    public void forceCleanupAllHolograms() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            hologramMap.values().forEach(map -> {
                Integer entityId = map.get(player.getUniqueId());
                if (entityId != null) {
                    sendDestroyPacket(player, entityId);
                }
            });
        });
        hologramMap.clear();
        usedEntityIds.clear();
    }
}
