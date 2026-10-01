package me.hektortm.woSSystems.systems.economy;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.model.Currency;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Service layer for the economy system.
 *
 * <p>Provides high-level currency operations (give, take, set, reset) by
 * computing the new balance and delegating persistence to
 * {@link me.hektortm.woSSystems.database.dao.EconomyDAO} via the
 * {@link DAOHub}.  Only online players can have their currency modified;
 * calls for offline players are silently ignored.</p>
 */
public class EcoManager {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    /**
     * @param hub the DAO hub used to access economy persistence
     */
    public EcoManager(DAOHub hub) {
        this.hub = hub;
    }

    /**
     * Modifies a currency balance for an online player.  The operation is
     * computed locally and then persisted via the DAO.  {@link Operations#TAKE}
     * will floor the balance at zero rather than allowing negatives.
     *
     * @param uuid      the player's UUID (must be online)
     * @param currency  the currency ID to modify
     * @param amount    the amount to use for the operation
     * @param operation the arithmetic operation to apply
     */
    public long modifyCurrency(UUID uuid, String currency, long amount, Operations operation) {
        return modifyCurrency(uuid, currency, amount, operation, "plugin", null);
    }

    /**
     * Modifies a currency balance for an online player and records where the
     * change came from in the economy log (one atomic wos-api transaction).
     * {@link Operations#TAKE} floors the balance at zero, and nothing takes a
     * balance above the currency's maximum (a reward that doesn't fit is cut
     * to what does).
     *
     * @param sourceType category of the source (e.g. {@code "command"}, {@code "interaction"})
     * @param source     specific source (e.g. the command sender or interaction id); may be null
     * @return by how much the balance really changed (negative when it went down, 0 when nothing changed)
     */
    public long modifyCurrency(UUID uuid, String currency, long amount, Operations operation, String sourceType, String source) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return 0; // online players only

        long current = getCurrencyBalance(uuid, currency);
        long newAmount = newBalance(getCurrencies().get(currency), current, amount, operation);
        hub.getEconomyDAO().updatePlayerCurrency(uuid, currency, newAmount, sourceType, source);
        return newAmount - current;
    }

    /** The balance after an operation, within the currency's limits (an unknown currency has none). */
    static long newBalance(Currency currency, long current, long amount, Operations operation) {
        long wanted = switch (operation) {
            case GIVE -> current + amount;
            case TAKE -> current - amount;
            case SET -> amount;
            case RESET -> 0;
        };
        return currency == null ? Math.max(0, wanted) : currency.clamp(wanted);
    }

    /**
     * Moves {@code amount} of a currency from one online player to another as
     * one atomic transfer (see {@link me.hektortm.woSSystems.database.dao.EconomyDAO#transfer}).
     *
     * Nothing moves unless all of it can: the sender must afford it, and the
     * receiver must be able to hold it (the currency's maximum).
     */
    public Transfer transfer(UUID from, UUID to, String currency, long amount, String sourceType, String source) {
        if (amount <= 0 || from.equals(to)) return Transfer.FAILED;
        if (!hasEnoughCurrency(from, currency, amount)) return Transfer.NO_FUNDS;
        Currency def = getCurrencies().get(currency);
        long receiving = getCurrencyBalance(to, currency) + amount;
        if (def != null && def.clamp(receiving) != receiving) return Transfer.RECEIVER_FULL;
        return hub.getEconomyDAO().transfer(from, to, currency, amount, sourceType, source) ? Transfer.DONE : Transfer.FAILED;
    }

    /** How a {@link #transfer} went. */
    public enum Transfer { DONE, NO_FUNDS, RECEIVER_FULL, FAILED }

    /**
     * The currency a player means: by id, else by name, in any case ("gold",
     * "Gold", "Gold_Coins" for "Gold Coins"). Null if there is none.
     */
    public Currency findCurrency(String typed) {
        return findCurrency(getCurrencies(), typed);
    }

    static Currency findCurrency(Map<String, Currency> currencies, String typed) {
        if (typed == null || typed.isBlank()) return null;
        String wanted = typed.trim();
        Currency exact = currencies.get(wanted);
        if (exact != null) return exact;
        for (Map.Entry<String, Currency> e : currencies.entrySet()) {
            if (e.getKey().equalsIgnoreCase(wanted)) return e.getValue();
        }
        String spaced = wanted.replace('_', ' ');
        for (Currency c : currencies.values()) {
            if (spaced.equalsIgnoreCase(c.getName())) return c;
        }
        return null;
    }

    /** The uuid of a player by name, online or not; null if unknown. Blocking — call off the main thread. */
    public UUID findPlayerUuid(String name) {
        return hub.getEconomyDAO().findPlayerUuid(name);
    }

    /**
     * Returns all currency definitions keyed by currency ID.
     *
     * @return map of currency ID to {@link Currency}
     */
    public Map<String, Currency> getCurrencies() {
        return hub.getEconomyDAO().getCurrencies(); // Fetch currencies from the database
    }

    /**
     * Returns the current balance for a player and currency.
     * Served from cache for online players; falls back to a DB query for offline players.
     *
     * @param uuid     the player's UUID
     * @param currency the currency ID
     * @return the current balance, or {@code 0} if the player has no entry
     */
    public long getCurrencyBalance(UUID uuid, String currency) {
        return hub.getEconomyDAO().getPlayerCurrency(uuid, currency);

    }

    /**
     * Returns {@code true} if a currency with the given ID is defined.
     *
     * @param id the currency ID to check
     * @return {@code true} if the currency exists
     */
    public boolean currencyExists(String id) {
        return hub.getEconomyDAO().currencyExists(id);
    }

    /**
     * Returns {@code true} if the player's balance in the given currency is at
     * least {@code amount}.
     *
     * @param uuid     the player's UUID
     * @param currency the currency ID
     * @param amount   the required minimum balance
     * @return {@code true} if the player can afford the cost
     */
    public boolean hasEnoughCurrency(UUID uuid, String currency, long amount) {
        return getCurrencyBalance(uuid, currency) >= amount;
    }

}
