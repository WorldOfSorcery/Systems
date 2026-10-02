package me.hektortm.woSSystems.systems.debug;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.hektortm.woSSystems.utils.model.InteractionKey;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The labels debug mode shows above every block and NPC an interaction is
 * bound to: the interaction's id and the binding. Packets only, like the
 * holograms, and only for players with debug mode on. Drawn by the interaction
 * task's pass: a label that pass no longer asks for (out of range, unbound,
 * debug mode switched off) is removed at its end.
 */
public final class DebugLabels implements Listener {

    private static final double RANGE_SQUARED = 16.0 * 16.0;
    /** Above where the interaction's own hologram starts (1.5 over a block). */
    private static final double BLOCK_Y_OFFSET = 2.3;
    private static final double NPC_Y_OFFSET = 0.8;
    /** An NPC that moved further than this gets its label again at the new place. */
    private static final double MOVED_SQUARED = 0.2 * 0.2;

    // Metadata indexes of a text display (MC 1.21.x), as in HologramManager.
    private static final int META_BILLBOARD = 15, META_TEXT = 23, META_STYLE_FLAGS = 27;
    private static final byte BILLBOARD_CENTER = 3, STYLE_SEE_THROUGH = 0x02;

    /** Apart from the holograms' and the displays' ids. */
    private static final int FIRST_ENTITY_ID = 3_000_000;

    private record Label(int entityId, Location at) {}

    private final DebugMode debug;
    private final AtomicInteger entityIdCounter = new AtomicInteger(FIRST_ENTITY_ID);
    /** player → (interaction and binding → the label they see). */
    private final Map<UUID, Map<String, Label>> shown = new HashMap<>();
    /** player → the labels this pass asked for. */
    private final Map<UUID, Set<String>> wanted = new HashMap<>();

    public DebugLabels(DebugMode debug) {
        this.debug = debug;
    }

    public void beginPass() {
        wanted.clear();
    }

    /**
     * Shows the label of one binding to the player, if they have debug mode on
     * and are near it.
     *
     * @param location     the bound block, or the NPC's feet
     * @param entityHeight the NPC's height (ignored for blocks)
     */
    public void show(Player player, String interactionId, InteractionKey key, Location location, boolean npc, double entityHeight) {
        if (!debug.isOn(player) || !player.getWorld().equals(location.getWorld())) return;
        if (player.getLocation().distanceSquared(location) > RANGE_SQUARED) return;

        String id = interactionId + "@" + key.getKey();
        wanted.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(id);
        Location at = npc ? location.clone().add(0, entityHeight * 0.5 + NPC_Y_OFFSET, 0)
                : location.clone().add(0.5, BLOCK_Y_OFFSET, 0.5);

        Map<String, Label> labels = shown.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        Label current = labels.get(id);
        if (current != null && current.at().distanceSquared(at) <= MOVED_SQUARED) return;
        if (current != null) destroy(player, current.entityId());

        int entityId = entityIdCounter.incrementAndGet();
        spawn(player, entityId, at, DebugFormat.label(interactionId, key.getKey()));
        labels.put(id, new Label(entityId, at));
    }

    /** Removes every label the pass didn't ask for. */
    public void endPass() {
        Iterator<Map.Entry<UUID, Map<String, Label>>> players = shown.entrySet().iterator();
        while (players.hasNext()) {
            Map.Entry<UUID, Map<String, Label>> entry = players.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            Set<String> keep = wanted.getOrDefault(entry.getKey(), Set.of());
            entry.getValue().entrySet().removeIf(label -> {
                if (keep.contains(label.getKey())) return false;
                if (player != null) destroy(player, label.getValue().entityId());
                return true;
            });
            if (entry.getValue().isEmpty()) players.remove();
        }
    }

    /** The client dropped its entities: the next pass shows the labels again. */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        shown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        shown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        shown.remove(event.getPlayer().getUniqueId());
    }

    private void spawn(Player player, int entityId, Location at, String text) {
        var api = PacketEvents.getAPI();
        var user = api == null ? null : api.getPlayerManager().getUser(player);
        if (user == null) return;

        user.sendPacket(new WrapperPlayServerSpawnEntity(
                entityId, Optional.of(UUID.randomUUID()), EntityTypes.TEXT_DISPLAY,
                new Vector3d(at.getX(), at.getY(), at.getZ()), 0f, 0f, 0f, 0, Optional.empty()));
        List<EntityData<?>> metadata = List.of(
                new EntityData<>(META_TEXT, EntityDataTypes.ADV_COMPONENT, LegacyComponentSerializer.legacySection().deserialize(text)),
                new EntityData<>(META_BILLBOARD, EntityDataTypes.BYTE, BILLBOARD_CENTER),
                new EntityData<>(META_STYLE_FLAGS, EntityDataTypes.BYTE, STYLE_SEE_THROUGH));
        user.sendPacket(new WrapperPlayServerEntityMetadata(entityId, clientVersion -> metadata));
    }

    private void destroy(Player player, int entityId) {
        var api = PacketEvents.getAPI();
        var user = api == null ? null : api.getPlayerManager().getUser(player);
        if (user != null) user.sendPacket(new WrapperPlayServerDestroyEntities(entityId));
    }
}
