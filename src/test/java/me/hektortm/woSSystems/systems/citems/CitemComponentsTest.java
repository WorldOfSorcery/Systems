package me.hektortm.woSSystems.systems.citems;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Custom item components (portal JSON) as the item parser's component syntax. */
class CitemComponentsTest {

    private static Map<String, String> vanilla(String json) {
        return CitemComponents.vanilla(JsonParser.parseString(json).getAsJsonObject());
    }

    @Test
    void anyComponentBecomesComponentSyntax() {
        Map<String, String> c = vanilla("""
                {"minecraft:equippable": {"slot": "head", "asset_id": "wos:wizard_hat", "swappable": false},
                 "minecraft:jukebox_playable": "minecraft:pigstep",
                 "minecraft:potion_duration_scale": 0.5,
                 "minecraft:custom_data": {"tags": ["a", "b"], "level": 3}}""");
        assertThat(c).containsExactly(
                Map.entry("minecraft:equippable", "{\"slot\":\"head\",\"asset_id\":\"wos:wizard_hat\",\"swappable\":false}"),
                Map.entry("minecraft:jukebox_playable", "\"minecraft:pigstep\""),
                Map.entry("minecraft:potion_duration_scale", "0.5"),
                Map.entry("minecraft:custom_data", "{\"tags\":[\"a\",\"b\"],\"level\":3}"));
    }

    @Test
    void componentsTheBuilderAppliesItselfAreLeftOut() {
        assertThat(vanilla("""
                {"minecraft:rarity": "epic", "minecraft:food": {"nutrition": 4}, "minecraft:glider": true}"""))
                .containsOnlyKeys("minecraft:glider");
    }

    @Test
    void aComponentWithoutSettingsIsOnOrOff() {
        assertThat(vanilla("{\"minecraft:glider\": true, \"minecraft:intangible_projectile\": false, \"minecraft:lock\": null}"))
                .containsExactly(Map.entry("minecraft:glider", "{}"));
    }

    @Test
    void namesWithoutNamespaceAreMinecrafts_andBadIdsAreDropped() {
        assertThat(vanilla("{\"glider\": true, \"not a component]\": 1, \"wos:Bad\": 1}"))
                .containsOnlyKeys("minecraft:glider");
    }

    @Test
    void thePortalsLodestoneTrackerBecomesVanillas() {
        assertThat(vanilla("""
                {"minecraft:lodestone_tracker": {"x": 10, "y": 64, "z": -20, "dimension": "minecraft:the_nether", "tracked": false}}"""))
                .containsEntry("minecraft:lodestone_tracker",
                        "{target:{pos:[I;10,64,-20],dimension:\"minecraft:the_nether\"},tracked:false}");
    }

    @Test
    void deathProtectionIsUnnested() {
        assertThat(vanilla("""
                {"minecraft:death_protection": {"death_effects": {"death_effects": [{"type": "minecraft:clear_all_effects"}]}}}"""))
                .containsEntry("minecraft:death_protection", "{death_effects:[{\"type\":\"minecraft:clear_all_effects\"}]}");
        assertThat(vanilla("{\"minecraft:death_protection\": {}}"))
                .containsEntry("minecraft:death_protection", "{}");
    }

    @Test
    void hexColorsBecomeNumbers() {
        assertThat(vanilla("{\"minecraft:dyed_color\": \"#ff0000\"}")).containsEntry("minecraft:dyed_color", "16711680");
        assertThat(vanilla("{\"minecraft:custom_model_data\": {\"floats\": [1.5], \"colors\": [\"#0000ff\", 255]}}"))
                .containsEntry("minecraft:custom_model_data", "{\"floats\":[1.5],\"colors\":[255,255]}");
    }

    @Test
    void potionContentsKeepEveryOption_withTheColorAsANumber() {
        assertThat(vanilla("""
                {"minecraft:potion_contents": {"potion": "minecraft:healing", "custom_color": "#ff0000", "custom_name": "tea",
                 "custom_effects": [{"id": "minecraft:speed", "amplifier": 1, "duration": 200}]}}"""))
                .containsEntry("minecraft:potion_contents", "{\"potion\":\"minecraft:healing\",\"custom_color\":16711680,"
                        + "\"custom_name\":\"tea\",\"custom_effects\":[{\"id\":\"minecraft:speed\",\"amplifier\":1,\"duration\":200}]}");
        assertThat(vanilla("{\"minecraft:potion_contents\": {\"potion\": \"minecraft:water\", \"custom_color\": \"red\"}}"))
                .containsEntry("minecraft:potion_contents", "{\"potion\":\"minecraft:water\"}");
    }

    @Test
    void aToolKeepsEveryOption_andDropsRulesWithoutBlocks() {
        assertThat(vanilla("""
                {"minecraft:tool": {"default_mining_speed": 2, "can_destroy_blocks_in_creative": false,
                 "rules": [{"blocks": "#minecraft:mineable/pickaxe", "speed": 8, "correct_for_drops": true}, {"blocks": " "}]}}"""))
                .containsEntry("minecraft:tool", "{\"default_mining_speed\":2,\"can_destroy_blocks_in_creative\":false,"
                        + "\"rules\":[{\"blocks\":\"#minecraft:mineable/pickaxe\",\"speed\":8,\"correct_for_drops\":true}]}");
    }

    @Test
    void numbersStringsAndNullsAreWrittenSafely() {
        JsonObject o = JsonParser.parseString("""
                {"a": 3.0, "b": 0.0001, "c": 9999999999, "d": "say \\"hi\\" \\\\ bye", "e": null, "f": [1, null, 2]}""").getAsJsonObject();
        assertThat(CitemComponents.snbt(o))
                .isEqualTo("{\"a\":3,\"b\":0.0001,\"c\":9999999999L,\"d\":\"say \\\"hi\\\" \\\\ bye\",\"f\":[1,2]}");
    }

    @Test
    void theItemStringIsWhatGiveTakes() {
        assertThat(CitemComponents.itemString("minecraft:stick", Map.of())).isEqualTo("minecraft:stick");
        assertThat(CitemComponents.itemString("minecraft:stick", vanilla("{\"minecraft:glider\": true, \"minecraft:max_damage_x\": 5}")))
                .isEqualTo("minecraft:stick[minecraft:glider={},minecraft:max_damage_x=5]");
    }
}
