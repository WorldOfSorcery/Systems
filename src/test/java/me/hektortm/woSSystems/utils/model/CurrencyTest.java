package me.hektortm.woSSystems.utils.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A currency's colour, as staff type it, ready for messages. */
class CurrencyTest {

    @Test
    void ampersandCodesBecomeSectionCodes() {
        assertThat(Currency.colorCode("&6")).isEqualTo("§6");
        assertThat(Currency.colorCode("&e&l")).isEqualTo("§e§l");
        assertThat(Currency.colorCode(" &A ")).isEqualTo("§A");
    }

    @Test
    void hexColorsWork_withOrWithoutAmpersand() {
        assertThat(Currency.colorCode("#FFAA00")).isEqualTo("#FFAA00");
        assertThat(Currency.colorCode("&#FFAA00")).isEqualTo("#FFAA00");
    }

    @Test
    void sectionCodesAndOtherTextAreKept() {
        assertThat(Currency.colorCode("§6")).isEqualTo("§6");
        assertThat(Currency.colorCode("&z")).isEqualTo("&z"); // not a colour code
        assertThat(Currency.colorCode("&")).isEqualTo("&");
    }

    @Test
    void noColorIsEmpty_neverNull() {
        assertThat(Currency.colorCode(null)).isEmpty();
        assertThat(new Currency("gold", "Gold", null, null, false).getColor()).isEmpty();
        assertThat(new Currency("gold", "Gold", null, "&6", false).getColor()).isEqualTo("§6");
    }
}
