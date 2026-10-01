package me.hektortm.woSSystems.systems.economy;

import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.model.Currency;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Balances stay within a currency's limits whatever the operation. */
class EcoManagerLimitsTest {

    private static final Currency CAPPED = new Currency("gems", "Gems", null, "&b", false, false, 100);
    private static final Currency FREE = new Currency("gold", "Gold", null, "&6", false);

    @Test
    void aGiveIsCutToTheMaximum() {
        assertThat(EcoManager.newBalance(CAPPED, 80, 50, Operations.GIVE)).isEqualTo(100);
        assertThat(EcoManager.newBalance(CAPPED, 100, 1, Operations.GIVE)).isEqualTo(100);
        assertThat(EcoManager.newBalance(FREE, 80, 50, Operations.GIVE)).isEqualTo(130);
    }

    @Test
    void aSetIsKeptWithinZeroAndTheMaximum() {
        assertThat(EcoManager.newBalance(CAPPED, 0, 5000, Operations.SET)).isEqualTo(100);
        assertThat(EcoManager.newBalance(CAPPED, 50, -5, Operations.SET)).isEqualTo(0);
    }

    @Test
    void aTakeStopsAtZero_andResetIsZero() {
        assertThat(EcoManager.newBalance(FREE, 30, 50, Operations.TAKE)).isEqualTo(0);
        assertThat(EcoManager.newBalance(FREE, 30, 0, Operations.RESET)).isEqualTo(0);
    }

    @Test
    void anUnknownCurrencyHasNoMaximum() {
        assertThat(EcoManager.newBalance(null, 10, 1_000_000, Operations.GIVE)).isEqualTo(1_000_010);
        assertThat(EcoManager.newBalance(null, 10, 50, Operations.TAKE)).isEqualTo(0);
    }

    @Test
    void theRulesComeWithTheCurrency() {
        assertThat(CAPPED.isPayable()).isFalse();
        assertThat(CAPPED.getMaxBalance()).isEqualTo(100);
        assertThat(FREE.isPayable()).isTrue(); // the default
        assertThat(FREE.getMaxBalance()).isZero();
    }
}
