package me.hektortm.woSSystems.content;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTest {
    private static JsonObject obj(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void missingAndNullAreBothAbsent() {
        JsonObject o = obj("{\"a\":null}");
        assertThat(Json.str(o, "a")).isNull();
        assertThat(Json.str(o, "b")).isNull();
        assertThat(Json.str(o, "a", "fallback")).isEqualTo("fallback");
        assertThat(Json.integer(o, "a", 7)).isEqualTo(7);
    }

    @Test
    void nonPrimitivesComeBackAsJsonText() {
        JsonObject o = obj("{\"data\":{\"material\":\"STONE\"},\"lore\":[\"a\",\"b\"]}");
        assertThat(Json.str(o, "data")).isEqualTo("{\"material\":\"STONE\"}");
        assertThat(Json.str(o, "lore")).isEqualTo("[\"a\",\"b\"]");
    }

    @Test
    void booleansAcceptNumbers() {
        JsonObject o = obj("{\"t\":true,\"one\":1,\"zero\":0}");
        assertThat(Json.bool(o, "t", false)).isTrue();
        assertThat(Json.bool(o, "one", false)).isTrue();
        assertThat(Json.bool(o, "zero", true)).isFalse();
    }

    @Test
    void stringsReadArraysAndArraysInsideStrings() {
        // A jsonb array, and a legacy text column that contains a JSON array.
        assertThat(Json.strings(obj("{\"x\":[\"a, with comma\",\"b\"]}"), "x")).containsExactly("a, with comma", "b");
        assertThat(Json.strings(obj("{\"x\":\"[\\\"a\\\",\\\"b\\\"]\"}"), "x")).containsExactly("a", "b");
        assertThat(Json.strings(obj("{\"x\":\"plain\"}"), "x")).containsExactly("plain");
        assertThat(Json.strings(obj("{}"), "x")).isEmpty();
    }
}
