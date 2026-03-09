package me.hektortm.woSSystems.systems.interactions;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.woSSystems.utils.dataclasses.Interaction;
import me.hektortm.woSSystems.utils.dataclasses.InteractionHologram;
import me.hektortm.woSSystems.utils.dataclasses.InteractionKey;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HologramManager {

    private static final double RENDER_DISTANCE_SQUARED = 16.0 * 16.0;
    private static final double LINE_GAP = 0.25;
    private static final double BLOCK_Y_OFFSET = 1.5;
    private static final double NPC_Y_OFFSET = 2.5;

    // Metadata indices for TextDisplay in 1.21.x
    private static final int META_TEXT = 23;
    // Billboard: 1 = VERTICAL (rotates on vertical axis only)
    private static final int META_BILLBOARD = 15;

    // Matches both &#RRGGBB and #RRGGBB hex color codes (exactly 6 hex digits)
    private static final Pattern HEX_PATTERN = Pattern.compile("&?#([A-Fa-f0-9]{6})");

    private final DAOHub hub;
    private final ConditionHandler conditionHandler;

    /**
     * Tracks the current state of holograms shown to a player for a given hologram key.
     * fingerprint — which hologram IDs passed conditions last tick (used to detect changes)
     * entityIds   — the packet entity IDs currently spawned
     */
    private record HologramState(String fingerprint, List<Integer> entityIds) {}

    // player UUID -> (hologramKey -> state)
    private final Map<UUID, Map<String, HologramState>> activeHolograms = new HashMap<>();
    private final AtomicInteger entityIdCounter = new AtomicInteger(1_000_000);

    public HologramManager(DAOHub hub) {
        this.hub = hub;
        this.conditionHandler = WoSSystems.getInstance().getConditionHandler();
    }

    /**
     * Called every interaction task tick for each player/location pair.
     * Shows, hides, or refreshes the hologram depending on proximity and live condition results.
     */
    public void handleHolograms(Player player, Interaction inter, Location location, boolean npc, InteractionKey key) {
        List<InteractionHologram> holograms = inter.getHolograms();
        if (holograms == null || holograms.isEmpty()) return;

        if (!player.getWorld().equals(location.getWorld())) return;

        String hologramKey = buildHologramKey(inter.getInteractionId(), location);
        boolean inRange = player.getLocation().distanceSquared(location) <= RENDER_DISTANCE_SQUARED;

        if (inRange) {
            List<InteractionHologram> visible = resolveVisibleHolograms(player, inter, holograms, key);
            String fingerprint = buildFingerprint(visible);


            HologramState current = getState(player, hologramKey);

            if (current == null) {
                spawnHolograms(player, inter.getInteractionId(), visible, location, npc, hologramKey, fingerprint);
            } else if (!current.fingerprint().equals(fingerprint)) {
                destroyEntities(player, current.entityIds());
                removeState(player, hologramKey);
                spawnHolograms(player, inter.getInteractionId(), visible, location, npc, hologramKey, fingerprint);
            }
            // else: same state — nothing to do
        } else {
            HologramState current = getState(player, hologramKey);
            if (current != null) {
                destroyEntities(player, current.entityIds());
                removeState(player, hologramKey);
            }
        }
    }

    /**
     * Evaluates conditions for each hologram and returns the list that should currently be displayed.
     * Respects behaviour (break/continue) in the same order as other interaction systems.
     */
    private List<InteractionHologram> resolveVisibleHolograms(Player player, Interaction inter,
                                                               List<InteractionHologram> holograms, InteractionKey key) {
        List<InteractionHologram> result = new ArrayList<>();

        for (InteractionHologram hologram : holograms) {
            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.HOLOGRAM,
                    inter.getInteractionId() + ":" + hologram.getHologramID()
            );

            if (!conditionList.isEmpty()) {
                boolean passes;
                if ("one".equalsIgnoreCase(hologram.getMatchType())) {
                    passes = conditionList.stream().anyMatch(c -> conditionHandler.evaluate(player, c, key));
                } else {
                    passes = conditionHandler.checkConditions(player, conditionList, key);
                }
                if (!passes) {
                    if (hologram.getBehaviour().equalsIgnoreCase("break")) break;
                    continue;
                }
            }

            result.add(hologram);
            if (hologram.getBehaviour().equalsIgnoreCase("break")) break;
        }

        return result;
    }

    /**
     * Builds a compact string that uniquely represents which holograms + lines are currently visible.
     * Used to detect condition changes without re-spawning unnecessarily.
     */
    private String buildFingerprint(List<InteractionHologram> holograms) {
        StringBuilder sb = new StringBuilder();
        for (InteractionHologram h : holograms) {
            sb.append(h.getHologramID()).append(':').append(h.getHologram()).append(';');
        }
        return sb.toString();
    }

    private void spawnHolograms(Player player, String interactionId, List<InteractionHologram> holograms,
                                 Location location, boolean npc, String hologramKey, String fingerprint) {
        if (holograms.isEmpty()) return;


        List<Integer> spawnedIds = new ArrayList<>();
        double yBase = location.getY() + (npc ? NPC_Y_OFFSET : BLOCK_Y_OFFSET);
        double x = location.getX() + 0.5;
        double z = location.getZ() + 0.5;

        for (InteractionHologram hologram : holograms) {
            List<String> lines = hologram.getHologram();
            // First line in list appears highest
            for (int i = 0; i < lines.size(); i++) {
                double y = yBase + (lines.size() - 1 - i) * LINE_GAP;
                int entityId = entityIdCounter.incrementAndGet();
                sendSpawnTextDisplay(player, entityId, x, y, z, lines.get(i));
                spawnedIds.add(entityId);
            }
        }

        if (!spawnedIds.isEmpty()) {
            activeHolograms
                    .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(hologramKey, new HologramState(fingerprint, spawnedIds));
        }
    }

    /**
     * Resolves all {placeholder} tokens in a text string for the given player.
     * Unknown keys fall back to their raw form or an empty constant.
     */
    private String replacePlaceholders(String text, Player player) {
        PlaceholderResolver resolver = WoSSystems.getInstance().getPlaceholderResolver();
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            int open = text.indexOf('{', i);
            if (open == -1) { result.append(text.substring(i)); break; }
            int close = text.indexOf('}', open + 1);
            if (close == -1) { result.append(text.substring(i)); break; }
            result.append(text, i, open);
            result.append(resolver.resolvePlaceholders(text.substring(open + 1, close), player));
            i = close + 1;
        }
        return result.toString();
    }

    /**
     * Converts &#RRGGBB hex codes to the §x§R§R§G§G§B§B format that
     * LegacyComponentSerializer understands, then converts & to §.
     */
    private String parseColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : matcher.group(1).toCharArray()) replacement.append('§').append(c);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString().replace('&', '§');
    }

    private void sendSpawnTextDisplay(Player player, int entityId, double x, double y, double z, String text) {
        var api = PacketEvents.getAPI();
        if (api == null) return;

        var user = api.getPlayerManager().getUser(player);
        if (user == null) return;

        // 1. Resolve {placeholders} and constants  2. Parse &#hex + & color codes
        Component component = LegacyComponentSerializer.legacySection()
                .deserialize(parseColors(replacePlaceholders(text, player)));

        try {
            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                    entityId,
                    Optional.of(UUID.randomUUID()),
                    EntityTypes.TEXT_DISPLAY,
                    new Vector3d(x, y, z),
                    0f, 0f, 0f,
                    0,
                    Optional.empty()
            );
            user.sendPacket(spawnPacket);

            List<EntityData<?>> metadata = new ArrayList<>();
            metadata.add(new EntityData(META_TEXT, EntityDataTypes.ADV_COMPONENT, component));
            metadata.add(new EntityData(META_BILLBOARD, EntityDataTypes.BYTE, (byte) 1));

            // EntityMetadataProvider is a @FunctionalInterface — lambda ignoring client version
            user.sendPacket(new WrapperPlayServerEntityMetadata(entityId, clientVersion -> metadata));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Remove ALL holograms for a player (e.g. on disconnect or plugin disable). */
    public void removeAllHolograms(Player player) {
        Map<String, HologramState> playerMap = activeHolograms.remove(player.getUniqueId());
        if (playerMap == null) return;

        List<Integer> allIds = playerMap.values().stream()
                .flatMap(s -> s.entityIds().stream())
                .toList();
        destroyEntities(player, allIds);
    }

    private void destroyEntities(Player player, List<Integer> ids) {
        if (ids.isEmpty()) return;

        var api = PacketEvents.getAPI();
        if (api == null) return;

        var user = api.getPlayerManager().getUser(player);
        if (user == null) return;


        int[] arr = ids.stream().mapToInt(Integer::intValue).toArray();
        user.sendPacket(new WrapperPlayServerDestroyEntities(arr));
    }

    private HologramState getState(Player player, String hologramKey) {
        Map<String, HologramState> playerMap = activeHolograms.get(player.getUniqueId());
        return playerMap == null ? null : playerMap.get(hologramKey);
    }

    private void removeState(Player player, String hologramKey) {
        Map<String, HologramState> playerMap = activeHolograms.get(player.getUniqueId());
        if (playerMap != null) playerMap.remove(hologramKey);
    }

    private String buildHologramKey(String interactionId, Location location) {
        return interactionId + ":"
                + location.getWorld().getName() + ":"
                + location.getBlockX() + ":"
                + location.getBlockY() + ":"
                + location.getBlockZ();
    }
}
