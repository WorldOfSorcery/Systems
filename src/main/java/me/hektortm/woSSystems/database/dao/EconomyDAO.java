package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.player.ApiServices;
import me.hektortm.woSSystems.player.ApiWriter;
import me.hektortm.woSSystems.player.PlayerSession;
import me.hektortm.woSSystems.player.PlayerSessions;
import me.hektortm.woSSystems.utils.model.Currency;
import me.hektortm.wosCore.api.ApiException;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static me.hektortm.woSSystems.player.ApiWriter.body;

/**
 * Currencies and balances. Definitions come from {@code /v1/content/currencies};
 * balances live in each player's {@link PlayerSession}. Every balance change is
 * persisted as one wos-api transaction, which updates the balance and writes the
 * economy log atomically (the legacy code wrote the two separately).
 */
public class EconomyDAO {
    private final ContentStore<Currency> currencies;
    private final PlayerSessions sessions;
    private final ApiWriter writer;
    private final Logger log;

    public EconomyDAO(ApiServices s) {
        this.sessions = s.sessions();
        this.writer = s.writer();
        this.log = s.log();
        this.currencies = s.content().register(new ContentStore<>("currencies", "Currency",
                ApiSource.flat(s.api(), "/v1/content/currencies", "id", j -> new Currency(
                        Json.str(j, "id"), Json.str(j, "name"), Json.str(j, "short_name"),
                        Json.str(j, "icon"), Json.str(j, "color"), Json.bool(j, "hidden_if_zero", false)), log)));
    }

    public long getPlayerCurrency(UUID uuid, String currency) {
        PlayerSession s = sessions.getOrFetch(uuid);
        return s == null ? 0 : s.balances.getOrDefault(currency, 0L);
    }

    /**
     * Sets a balance to {@code newAmount}, recording where the change came from
     * (e.g. {@code "command"}/sender, {@code "interaction"}/id) in the economy log.
     */
    public void updatePlayerCurrency(UUID uuid, String currency, long newAmount, String sourceType, String source) {
        PlayerSession s = sessions.getOrFetch(uuid);
        if (s == null) {
            log.warning("[Economy] cannot change " + currency + " for unknown player " + uuid);
            return;
        }
        long previous = s.balances.getOrDefault(currency, 0L);
        long delta = newAmount - previous;
        if (delta == 0) return;
        s.balances.put(currency, newAmount);
        writer.postOnce("/v1/economy/transactions", body(
                "uuid", uuid.toString(), "currency", currency, "delta", delta,
                "source_type", sourceType, "source", source == null ? "" : source));
    }

    // ─── Currency definitions ───────────────────────────────────────────────────

    /** Every currency definition, keyed by id (read-only view). */
    public Map<String, Currency> getCurrencies() {
        return currencies.asMap();
    }

    public boolean currencyExists(String id) {
        return currencies.exists(id);
    }

    /** Force-refreshes the currency definitions from the API. Blocking — call off the main thread. */
    public void reloadCurrencies() {
        try {
            currencies.preload();
        } catch (ApiException e) {
            log.severe("[Economy] reloading currencies failed: " + e.getMessage());
        }
    }
}
