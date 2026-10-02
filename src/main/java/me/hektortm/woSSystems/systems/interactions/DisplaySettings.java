package me.hektortm.woSSystems.systems.interactions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * What one interaction display shows and how, read from its settings JSON.
 * Reading never fails: a missing or unreadable value gets its default.
 * No server types, so it can be unit-tested.
 */
public record DisplaySettings(
        Kind kind,
        String block,
        String item,
        String itemModel,
        String head,
        String citem,
        String itemDisplay,
        Vec3 offset,
        float yaw,
        float pitch,
        Vec3 translation,
        Vec3 scale,
        Vec3 leftRotationDeg,
        Vec3 rightRotationDeg,
        String billboard,
        Brightness brightness,
        boolean glowing,
        int glowColor,
        float viewRange,
        float shadowRadius,
        float shadowStrength,
        float width,
        float height,
        Bob bob,
        Spin spin,
        Touch touch
) {

    public enum Kind { BLOCK, ITEM, CITEM }

    public record Vec3(double x, double y, double z) {
        public static final Vec3 ZERO = new Vec3(0, 0, 0);
        public static final Vec3 ONE = new Vec3(1, 1, 1);
    }

    /** Light levels 0–15 that replace the world's light on the display. */
    public record Brightness(int block, int sky) {}

    /** Floating up and down: {@code height} blocks from lowest to highest point, one cycle per {@code periodTicks}. */
    public record Bob(boolean enabled, double height, int periodTicks) {}

    /** Turning around an axis ('x', 'y' or 'z'): one full turn per {@code periodTicks}. */
    public record Spin(boolean enabled, int periodTicks, char axis, boolean reverse) {}

    /** The box that runs the interaction: by walking into it, by clicking it, or both. */
    public record Touch(boolean walk, boolean click, double width, double height) {
        public boolean enabled() {
            return walk || click;
        }
    }

    /** The fastest animation: half a second per cycle or turn. */
    static final int MIN_PERIOD_TICKS = 10;

    public boolean animated() {
        return bob.enabled() || spin.enabled();
    }

    public static DisplaySettings parse(String json) {
        JsonObject o = object(json);
        JsonObject transformation = child(o, "transformation");
        JsonObject animation = child(o, "animation");
        JsonObject bob = child(animation, "bob");
        JsonObject spin = child(animation, "spin");
        JsonObject touch = child(o, "touch");
        JsonObject brightness = o.has("brightness") && o.get("brightness").isJsonObject() ? o.getAsJsonObject("brightness") : null;

        return new DisplaySettings(
                kind(text(o, "kind", "item")),
                text(o, "block", "minecraft:stone"),
                text(o, "item", "minecraft:stone"),
                text(o, "item_model", ""),
                text(o, "head", ""),
                text(o, "citem", ""),
                text(o, "item_display", "none"),
                vec3(o, "offset", Vec3.ZERO),
                (float) number(o, "yaw", 0),
                (float) number(o, "pitch", 0),
                vec3(transformation, "translation", Vec3.ZERO),
                vec3(transformation, "scale", Vec3.ONE),
                vec3(transformation, "left_rotation_deg", Vec3.ZERO),
                vec3(transformation, "right_rotation_deg", Vec3.ZERO),
                text(o, "billboard", "fixed"),
                brightness == null ? null : new Brightness(level(brightness, "block"), level(brightness, "sky")),
                flag(o, "glowing"),
                (int) number(o, "glow_color_override", 0),
                (float) number(o, "view_range", 1),
                (float) number(o, "shadow_radius", 0),
                (float) number(o, "shadow_strength", 1),
                (float) number(o, "width", 0),
                (float) number(o, "height", 0),
                new Bob(flag(bob, "enabled"), number(bob, "height", 0.25), periodTicks(bob, 3.0)),
                new Spin(flag(spin, "enabled"), periodTicks(spin, 4.0), axis(text(spin, "axis", "y")), flag(spin, "reverse")),
                // "enabled" is how the first version stored "walk".
                new Touch(flag(touch, "walk") || flag(touch, "enabled"), flag(touch, "click"), Math.max(0, number(touch, "width", 1)), Math.max(0, number(touch, "height", 1)))
        );
    }

    private static JsonObject object(String json) {
        if (json == null || json.isBlank()) return new JsonObject();
        try {
            JsonElement e = JsonParser.parseString(json);
            return e.isJsonObject() ? e.getAsJsonObject() : new JsonObject();
        } catch (RuntimeException e) {
            return new JsonObject();
        }
    }

    private static JsonObject child(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : new JsonObject();
    }

    private static boolean isNumber(JsonElement e) {
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber();
    }

    private static double number(JsonObject o, String key, double fallback) {
        JsonElement e = o.get(key);
        if (!isNumber(e)) return fallback;
        double value = e.getAsDouble();
        return Double.isFinite(value) ? value : fallback;
    }

    private static String text(JsonObject o, String key, String fallback) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) return fallback;
        String value = e.getAsString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private static boolean flag(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean() && e.getAsBoolean();
    }

    private static Vec3 vec3(JsonObject o, String key, Vec3 fallback) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonArray()) return fallback;
        JsonArray a = e.getAsJsonArray();
        if (a.size() != 3 || !isNumber(a.get(0)) || !isNumber(a.get(1)) || !isNumber(a.get(2))) return fallback;
        return new Vec3(a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble());
    }

    private static int level(JsonObject brightness, String key) {
        return (int) Math.max(0, Math.min(15, number(brightness, key, 15)));
    }

    /** Seconds per cycle in the JSON, ticks here; never faster than {@link #MIN_PERIOD_TICKS}. */
    private static int periodTicks(JsonObject o, double fallbackSeconds) {
        return (int) Math.max(MIN_PERIOD_TICKS, Math.round(number(o, "period", fallbackSeconds) * 20));
    }

    private static Kind kind(String kind) {
        if ("block".equalsIgnoreCase(kind)) return Kind.BLOCK;
        if ("citem".equalsIgnoreCase(kind)) return Kind.CITEM;
        return Kind.ITEM;
    }

    private static char axis(String axis) {
        char c = Character.toLowerCase(axis.charAt(0));
        return c == 'x' || c == 'z' ? c : 'y';
    }
}
