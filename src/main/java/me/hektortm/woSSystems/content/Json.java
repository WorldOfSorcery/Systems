package me.hektortm.woSSystems.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Null-safe field access for wos-api JSON (missing and JSON null are both "absent"). */
public final class Json {
    private Json() {}

    @Nullable
    public static JsonElement field(JsonObject o, String name) {
        JsonElement e = o.get(name);
        return e == null || e.isJsonNull() ? null : e;
    }

    /** String value; arrays/objects are returned as their JSON text (like a MySQL JSON column). */
    @Nullable
    public static String str(JsonObject o, String name) {
        JsonElement e = field(o, name);
        if (e == null) return null;
        return e.isJsonPrimitive() ? e.getAsString() : e.toString();
    }

    public static String str(JsonObject o, String name, String fallback) {
        String s = str(o, name);
        return s == null ? fallback : s;
    }

    public static int integer(JsonObject o, String name, int fallback) {
        JsonElement e = field(o, name);
        return e == null ? fallback : e.getAsInt();
    }

    public static long lng(JsonObject o, String name, long fallback) {
        JsonElement e = field(o, name);
        return e == null ? fallback : e.getAsLong();
    }

    public static boolean bool(JsonObject o, String name, boolean fallback) {
        JsonElement e = field(o, name);
        if (e == null) return fallback;
        if (e.getAsJsonPrimitive().isBoolean()) return e.getAsBoolean();
        return e.getAsInt() != 0;
    }

    /**
     * A list of strings from a JSON array — or, tolerantly, from a string that
     * itself contains a JSON array (legacy text columns).
     */
    public static List<String> strings(JsonObject o, String name) {
        List<String> out = new ArrayList<>();
        JsonElement e = field(o, name);
        if (e == null) return out;
        if (e.isJsonPrimitive()) {
            String s = e.getAsString().trim();
            if (s.isEmpty()) return out;
            try {
                e = JsonParser.parseString(s);
            } catch (Exception ex) {
                out.add(s);
                return out;
            }
            if (!e.isJsonArray()) {
                out.add(s);
                return out;
            }
        }
        if (!e.isJsonArray()) return out;
        for (JsonElement item : e.getAsJsonArray()) {
            if (!item.isJsonNull()) out.add(item.isJsonPrimitive() ? item.getAsString() : item.toString());
        }
        return out;
    }

    public static JsonArray array(JsonObject o, String name) {
        JsonElement e = field(o, name);
        return e != null && e.isJsonArray() ? e.getAsJsonArray() : new JsonArray();
    }

    public static JsonObject object(JsonObject o, String name) {
        JsonElement e = field(o, name);
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : new JsonObject();
    }
}
