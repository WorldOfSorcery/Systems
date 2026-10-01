package me.hektortm.woSSystems.systems.economy;

import org.junit.jupiter.api.Test;

import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;

/** Money amounts as typed and as shown. */
class AmountsTest {

    @Test
    void amountsAreShownWithThousandsSeparators() {
        assertThat(Amounts.format(0)).isEqualTo("0");
        assertThat(Amounts.format(999)).isEqualTo("999");
        assertThat(Amounts.format(1_250_000)).isEqualTo("1,250,000");
        assertThat(Amounts.format(5_000_000_000L)).isEqualTo("5,000,000,000");
    }

    @Test
    void plainAndGroupedNumbersAreRead() {
        assertThat(Amounts.parse("1250", -1)).isEqualTo(OptionalLong.of(1250));
        assertThat(Amounts.parse("1,250,000", -1)).isEqualTo(OptionalLong.of(1_250_000));
        assertThat(Amounts.parse(" 42 ", -1)).isEqualTo(OptionalLong.of(42));
        assertThat(Amounts.parse("5000000000", -1)).isEqualTo(OptionalLong.of(5_000_000_000L)); // past the int limit
    }

    @Test
    void shortFormsAreRead() {
        assertThat(Amounts.parse("10k", -1)).isEqualTo(OptionalLong.of(10_000));
        assertThat(Amounts.parse("2.5M", -1)).isEqualTo(OptionalLong.of(2_500_000));
        assertThat(Amounts.parse("1b", -1)).isEqualTo(OptionalLong.of(1_000_000_000));
    }

    @Test
    void allIsTheBalance_whereThatMakesSense() {
        assertThat(Amounts.parse("all", 750)).isEqualTo(OptionalLong.of(750));
        assertThat(Amounts.parse("MAX", 0)).isEqualTo(OptionalLong.of(0));
        assertThat(Amounts.parse("all", -1)).isEmpty();
    }

    @Test
    void whatIsNotAWholeAmountIsRefused() {
        for (String bad : new String[]{"", "abc", "2.5", "1.2345k", "k", "1kk", "99999999999999999999", null}) {
            assertThat(Amounts.parse(bad, -1)).as("%s", bad).isEmpty();
        }
        assertThat(Amounts.parse("-5", -1)).isEqualTo(OptionalLong.of(-5)); // the command decides about the sign
    }
}
