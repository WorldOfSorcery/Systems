package me.hektortm.woSSystems.systems.citems;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns a custom item's data components (as the portal stores them, JSON) into
 * Minecraft's own component syntax: the {@code [id=value,…]} part of
 * {@code /give @s stick[minecraft:equippable={slot:"head"}]}.
 *
 * <p>{@link CitemBuilder} applies a handful of components itself through
 * {@code ItemMeta} ({@link #TYPED}); every other one goes through here and the
 * server's item parser, so any vanilla component works — also ones added in
 * later versions — without code for each.</p>
 *
 * <p>A few portal editors store a friendlier shape than vanilla's; those are
 * translated in {@link #snbt(String, JsonElement)}.</p>
 */
public final class CitemComponents {

    /** Applied by CitemBuilder through ItemMeta, not through the item parser. */
    static final Set<String> TYPED = Set.of(
            "minecraft:rarity", "minecraft:enchantment_glint_override", "minecraft:hide_tooltip",
            "minecraft:hide_additional_tooltip", "minecraft:item_name", "minecraft:damage",
            "minecraft:max_damage", "minecraft:unbreakable", "minecraft:repair_cost",
            "minecraft:max_stack_size", "minecraft:enchantments", "minecraft:stored_enchantments",
            "minecraft:food", "minecraft:tool", "minecraft:potion_contents", "minecraft:trim",
            "minecraft:banner_patterns", "minecraft:map_color", "minecraft:map_id",
            "minecraft:written_book_content", "minecraft:fire_resistant");

    private static final Pattern COMPONENT_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private CitemComponents() {}

    /** {@code equippable} → {@code minecraft:equippable}. */
    static String namespaced(String id) {
        return id.contains(":") ? id : "minecraft:" + id;
    }

    /**
     * The components the item parser applies: id → value in component syntax,
     * in the order given. Leaves out the {@link #TYPED} ones, components
     * switched off ({@code false} / null) and ids that aren't component ids.
     */
    public static Map<String, String> vanilla(JsonObject components) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
            String id = namespaced(entry.getKey().trim());
            if (TYPED.contains(id) || !COMPONENT_ID.matcher(id).matches()) continue;
            String value = snbt(id, entry.getValue());
            if (value != null) out.put(id, value);
        }
        return out;
    }

    /** {@code minecraft:stick[a=1,b={}]}: what {@code ItemFactory#createItemStack} parses. */
    public static String itemString(String itemId, Map<String, String> components) {
        if (components.isEmpty()) return itemId;
        StringBuilder sb = new StringBuilder(itemId).append('[');
        boolean first = true;
        for (Map.Entry<String, String> c : components.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(c.getKey()).append('=').append(c.getValue());
        }
        return sb.append(']').toString();
    }

    /** One component's value in component syntax; null when the component is off. */
    static String snbt(String id, JsonElement value) {
        if (value == null || value.isJsonNull()) return null;
        // A component without settings ("glider", "intangible_projectile"): the portal stores true.
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean() ? "{}" : null;
        }
        return switch (id) {
            case "minecraft:lodestone_tracker" -> lodestoneTracker(value);
            case "minecraft:death_protection" -> deathProtection(value);
            case "minecraft:dyed_color" -> color(value);
            case "minecraft:custom_model_data" -> customModelData(value);
            default -> snbt(value);
        };
    }

    // ── portal shapes that differ from vanilla's ──────────────────────────────

    /** Portal: {x, y, z, dimension, tracked}. Vanilla: {target: {pos: [I; x, y, z], dimension}, tracked}. */
    private static String lodestoneTracker(JsonElement value) {
        if (!value.isJsonObject() || !value.getAsJsonObject().has("x")) return snbt(value);
        JsonObject o = value.getAsJsonObject();
        String dimension = o.has("dimension") && o.get("dimension").isJsonPrimitive()
                ? o.get("dimension").getAsString() : "minecraft:overworld";
        boolean tracked = !o.has("tracked") || !o.get("tracked").isJsonPrimitive() || o.get("tracked").getAsBoolean();
        return "{target:{pos:[I;" + whole(o, "x") + "," + whole(o, "y") + "," + whole(o, "z") + "],dimension:"
                + quote(dimension) + "},tracked:" + tracked + "}";
    }

    private static long whole(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsLong() : 0;
    }

    /** Portal (an old default): {death_effects: {death_effects: […]}}. Vanilla: {death_effects: […]}. */
    private static String deathProtection(JsonElement value) {
        if (!value.isJsonObject()) return "{}";
        JsonElement effects = value.getAsJsonObject().get("death_effects");
        if (effects != null && effects.isJsonObject()) effects = effects.getAsJsonObject().get("death_effects");
        return effects != null && effects.isJsonArray() ? "{death_effects:" + snbt(effects) + "}" : "{}";
    }

    /** "#RRGGBB" → the decimal RGB vanilla wants; numbers pass through. */
    private static String color(JsonElement value) {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            Integer rgb = hex(value.getAsString());
            if (rgb != null) return rgb.toString();
        }
        return snbt(value);
    }

    /** custom_model_data's colors may be "#RRGGBB" strings. */
    private static String customModelData(JsonElement value) {
        if (!value.isJsonObject()) return snbt(value);
        JsonObject copy = value.getAsJsonObject().deepCopy();
        JsonElement colors = copy.get("colors");
        if (colors != null && colors.isJsonArray()) {
            JsonArray rgb = new JsonArray();
            for (JsonElement c : colors.getAsJsonArray()) {
                Integer parsed = c.isJsonPrimitive() && c.getAsJsonPrimitive().isString() ? hex(c.getAsString()) : null;
                rgb.add(parsed != null ? new JsonPrimitive(parsed) : c);
            }
            copy.add("colors", rgb);
        }
        return snbt(copy);
    }

    private static Integer hex(String s) {
        String h = s.trim();
        if (!h.startsWith("#") || h.length() != 7) return null;
        try {
            return Integer.parseInt(h.substring(1), 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── JSON → SNBT ───────────────────────────────────────────────────────────

    /** Any JSON value as SNBT: objects and lists as they are, strings quoted, null members left out. */
    static String snbt(JsonElement value) {
        if (value.isJsonObject()) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
                if (e.getValue() == null || e.getValue().isJsonNull()) continue;
                if (!first) sb.append(',');
                first = false;
                sb.append(quote(e.getKey())).append(':').append(snbt(e.getValue()));
            }
            return sb.append('}').toString();
        }
        if (value.isJsonArray()) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (JsonElement e : value.getAsJsonArray()) {
                if (e == null || e.isJsonNull()) continue;
                if (!first) sb.append(',');
                first = false;
                sb.append(snbt(e));
            }
            return sb.append(']').toString();
        }
        JsonPrimitive p = value.getAsJsonPrimitive();
        if (p.isBoolean()) return String.valueOf(p.getAsBoolean());
        if (p.isNumber()) return number(p.getAsBigDecimal());
        return quote(p.getAsString());
    }

    /** Whole numbers as ints (longs get an L), others as plain decimals (never 1.0E-4). */
    private static String number(BigDecimal n) {
        BigDecimal stripped = n.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            long whole = stripped.longValue();
            return whole >= Integer.MIN_VALUE && whole <= Integer.MAX_VALUE ? Long.toString(whole) : whole + "L";
        }
        return stripped.toPlainString();
    }

    private static String quote(String s) {
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
