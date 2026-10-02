package me.hektortm.woSSystems.systems.debug;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for {@link DebugFormat}: the texts debug mode shows. */
class DebugFormatTest {

    @Test
    void aHeaderNamesTheSourceAndItsDetails() {
        assertThat(DebugFormat.header("interaction", "shop_keeper", "row 2", "npc:12"))
                .isEqualTo("§8[debug] §einteraction shop_keeper§7 · row 2 · npc:12");
    }

    @Test
    void emptyDetailsAreLeftOut() {
        assertThat(DebugFormat.header("gui", "bank", null, " ", "LEFT")).isEqualTo("§8[debug] §egui bank§7 · LEFT");
        assertThat(DebugFormat.header("gui", "bank")).isEqualTo("§8[debug] §egui bank§7");
        assertThat(DebugFormat.header("gui", "bank", (String[]) null)).isEqualTo("§8[debug] §egui bank§7");
    }

    @Test
    void aCommandShowsWhatRanOnlyIfItDiffers() {
        assertThat(DebugFormat.command("close_gui", "close_gui")).isEqualTo("§8[debug] §7  close_gui");
        assertThat(DebugFormat.command("eco give @p gold {amount}", "eco give Xyz gold 5"))
                .isEqualTo("§8[debug] §7  eco give @p gold {amount} §8→ §feco give Xyz gold 5");
    }

    @Test
    void colourCodesInACommandAreShownNotApplied() {
        assertThat(DebugFormat.command("send_message §aHi", "send_message §aHi")).isEqualTo("§8[debug] §7  send_message &aHi");
    }

    @Test
    void aConditionSaysWhetherItIsMetAndWhatItFound() {
        assertThat(DebugFormat.condition("has_stats_greater_than", "kills", "10", false, "is 6"))
                .isEqualTo("§8[debug] §c  ✘ has_stats_greater_than kills 10 §7(is 6)");
        assertThat(DebugFormat.condition("is_sneaking", null, " ", true, null)).isEqualTo("§8[debug] §a  ✔ is_sneaking");
    }

    @Test
    void theGuiLineAndTheLabel() {
        assertThat(DebugFormat.guiItem(13, "2")).isEqualTo("§8slot 13 · config 2");
        assertThat(DebugFormat.label("shop_keeper", "npc:12")).isEqualTo("§eshop_keeper\n§7npc:12");
        assertThat(DebugFormat.note("row 1 skipped")).isEqualTo("§8[debug] §7  row 1 skipped");
    }
}
