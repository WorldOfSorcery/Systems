package me.hektortm.woSSystems.systems.economy;

import me.hektortm.woSSystems.utils.model.Currency;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Which currency a player means by what they typed. */
class EcoManagerCurrencyTest {

    private static final Map<String, Currency> CURRENCIES = new LinkedHashMap<>();
    static {
        CURRENCIES.put("gold", new Currency("gold", "Gold Coins", "⛃", "&6", false));
        CURRENCIES.put("house_points", new Currency("house_points", "House Points", null, null, true));
    }

    @Test
    void byId_inAnyCase() {
        assertThat(EcoManager.findCurrency(CURRENCIES, "gold").getId()).isEqualTo("gold");
        assertThat(EcoManager.findCurrency(CURRENCIES, "GOLD").getId()).isEqualTo("gold");
        assertThat(EcoManager.findCurrency(CURRENCIES, " House_Points ").getId()).isEqualTo("house_points");
    }

    @Test
    void byName_withUnderscoresForSpaces() {
        assertThat(EcoManager.findCurrency(CURRENCIES, "Gold_Coins").getId()).isEqualTo("gold");
        assertThat(EcoManager.findCurrency(CURRENCIES, "house points").getId()).isEqualTo("house_points");
    }

    @Test
    void unknownIsNull() {
        assertThat(EcoManager.findCurrency(CURRENCIES, "silver")).isNull();
        assertThat(EcoManager.findCurrency(CURRENCIES, "")).isNull();
        assertThat(EcoManager.findCurrency(CURRENCIES, null)).isNull();
    }
}
