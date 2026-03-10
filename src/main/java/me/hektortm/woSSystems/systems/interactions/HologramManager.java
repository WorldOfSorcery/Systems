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
    private static final double BLOCK_Y_OFFSET = 1.5;
    private static final double NPC_Y_OFFSET = 2.5;

    // ── Metadata index constants for Display / TextDisplay entities (MC 1.21.x) ──
    private enum Meta {
        // Display base
        INTERPOLATION_DELAY(8),
        INTERPOLATION_DURATION(9),
        TELEPORT_DURATION(10),
        TRANSLATION(11),
        SCALE(12),
        LEFT_ROTATION(13),
        RIGHT_ROTATION(14),
        BILLBOARD(15),
        BRIGHTNESS(16),
        VIEW_RANGE(17),
        SHADOW_RADIUS(18),
        SHADOW_STRENGTH(19),
        WIDTH(20),
        HEIGHT(21),
        GLOW_COLOR(22),
        // TextDisplay specific
        TEXT(23),
        LINE_WIDTH(24),
        BACKGROUND(25),
        TEXT_OPACITY(26),
        STYLE_FLAGS(27);

        final int index;
        Meta(int index) { this.index = index; }
    }

    /** Controls which axis the TextDisplay rotates around to face the player. */
    public enum Billboard {
        /** No rotation — fixed in world space. */
        FIXED((byte) 0),
        /** Rotates around the vertical (Y) axis only. */
        VERTICAL((byte) 1),
        /** Rotates around the horizontal axis only. */
        HORIZONTAL((byte) 2),
        /** Always faces the player (full billboard). */
        CENTER((byte) 3);

        private final byte value;
        Billboard(byte value) { this.value = value; }
        public byte value() { return value; }
    }

    /** Horizontal text alignment for a TextDisplay. */
    public enum TextAlignment {
        CENTER((byte) 0),
        LEFT((byte) 1),
        RIGHT((byte) 2);

        private final byte value;
        TextAlignment(byte value) { this.value = value; }
        /** Returns the alignment encoded into bits 3–4 of the style flags byte. */
        public byte styleFlags() { return (byte) (value << 3); }
    }

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

        double yBase = location.getY() + (npc ? NPC_Y_OFFSET : BLOCK_Y_OFFSET);
        double x = location.getX() + 0.5;
        double z = location.getZ() + 0.5;

        // Collect every line from all visible holograms into one component
        List<Component> lineComponents = new ArrayList<>();
        for (InteractionHologram hologram : holograms) {
            for (String line : hologram.getHologram()) {
                lineComponents.add(buildLineComponent(line, player));
            }
        }
        if (lineComponents.isEmpty()) return;

        Component combined = lineComponents.get(0);
        for (int i = 1; i < lineComponents.size(); i++) {
            combined = combined.append(Component.newline()).append(lineComponents.get(i));
        }

        int entityId = entityIdCounter.incrementAndGet();
        sendSpawnTextDisplay(player, entityId, x, yBase, z, combined);

        activeHolograms
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(hologramKey, new HologramState(fingerprint, List.of(entityId)));
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

    private Component buildLineComponent(String text, Player player) {
        return LegacyComponentSerializer.legacySection()
                .deserialize(parseColors(replacePlaceholders(text, player)));
    }

    private void sendSpawnTextDisplay(Player player, int entityId, double x, double y, double z, Component component) {
        var api = PacketEvents.getAPI();
        if (api == null) return;

        var user = api.getPlayerManager().getUser(player);
        if (user == null) return;

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
            metadata.add(new EntityData(Meta.TEXT.index, EntityDataTypes.ADV_COMPONENT, component));
            metadata.add(new EntityData(Meta.BILLBOARD.index, EntityDataTypes.BYTE, Billboard.VERTICAL.value()));

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
